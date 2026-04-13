package com.example.purrsistence.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.example.purrsistence.R
import com.example.purrsistence.focus.DeepFocusConfig

@SuppressLint("AccessibilityPolicy")
class DeepFocusAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "DeepFocusService"
        private const val TRANSIENT_SURFACE_DEBOUNCE_MS = 1200L
        private const val RETURN_TO_APP_GRACE_MS = 1800L

        private val COMMON_LAUNCHER_PREFIXES = listOf(
            "com.android.launcher",
            "com.google.android.apps.nexuslauncher",
            "com.miui.home",
            "com.sec.android.app.launcher",
            "com.huawei.android.launcher",
            "com.oppo.launcher",
            "com.oneplus.launcher",
            "com.motorola.launcher"
        )

        private val SYSTEM_PACKAGE_PREFIXES = listOf(
            "com.android.systemui",
            "com.android.settings",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller"
        )
    }

    private var overlayView: View? = null
    private var overlayForPackage: String? = null
    private var windowManager: WindowManager? = null
    private var homePackages: Set<String> = emptySet()
    private var overlayShownAtMs: Long = 0L
    private var returnToAppRequestedAtMs: Long = 0L
    private var suppressedPackageAfterReturn: String? = null
    private var suppressPackageUntilMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        homePackages = resolveAllHomePackages()
        debugLog("Service connected. homePackages=$homePackages")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val accessibilityEvent = event ?: return
        val type = accessibilityEvent.eventType
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        clearExpiredReturnToAppGrace()
        if (handleBlockingDisabled()) return

        val currentPackage = accessibilityEvent.packageName?.toString() ?: return
        val className = accessibilityEvent.className?.toString().orEmpty()

        debugLog("event type=$type pkg=$currentPackage class=$className overlayFor=$overlayForPackage")

        if (handleOwnPackageEvent(currentPackage, className)) return
        if (shouldSuppressPackageAfterReturn(currentPackage)) return
        if (handleReturnToAppGraceForNonAppEvent()) return

        if (isTransientSystemSurface(currentPackage, className)) {
            debugLog("Transient system surface -> ignore")
            return
        }

        if (handleAllowedPackage(currentPackage, className)) return

        debugLog("Blocked package -> show overlay")
        showOverlayForPackage(currentPackage)
    }

    private fun clearExpiredReturnToAppGrace() {
        val now = SystemClock.uptimeMillis()

        if (returnToAppRequestedAtMs != 0L && now - returnToAppRequestedAtMs > RETURN_TO_APP_GRACE_MS) {
            returnToAppRequestedAtMs = 0L
        }

        if (suppressPackageUntilMs != 0L && now > suppressPackageUntilMs) {
            suppressedPackageAfterReturn = null
            suppressPackageUntilMs = 0L
        }
    }

    private fun handleBlockingDisabled(): Boolean {
        if (isBlockingEnabled()) return false
        debugLog("Blocking disabled -> remove overlay")
        removeOverlay()
        return true
    }

    private fun handleOwnPackageEvent(currentPackage: String, className: String): Boolean {
        if (currentPackage != this.packageName) return false

        val isOverlayContainer = className == "android.widget.FrameLayout"
        if (overlayView != null && isOverlayContainer) return true

        if (overlayView != null) {
            debugLog("Own app foreground -> remove overlay")
            removeOverlay()
        }
        return true
    }

    private fun shouldSuppressPackageAfterReturn(currentPackage: String): Boolean {
        val suppressedPackage = suppressedPackageAfterReturn ?: return false
        val isSuppressed = currentPackage == suppressedPackage
        val withinWindow = SystemClock.uptimeMillis() <= suppressPackageUntilMs
        if (isSuppressed && withinWindow) {
            debugLog("Suppressed package after return -> ignore re-attach")
            return true
        }
        return false
    }

    private fun handleReturnToAppGraceForNonAppEvent(): Boolean {
        if (returnToAppRequestedAtMs == 0L) return false
        debugLog("Return-to-app grace active -> ignore non-app event")
        return true
    }

    private fun handleAllowedPackage(currentPackage: String, className: String): Boolean {
        if (!isPackageAllowed(currentPackage)) return false

        if (shouldDebounceAllowedSurface(currentPackage, className)) {
            debugLog("Allowed surface debounce -> keep overlay")
            return true
        }

        debugLog("Allowed package -> remove overlay")
        removeOverlay()
        return true
    }

    override fun onInterrupt() {
        removeOverlay()
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private fun isBlockingEnabled(): Boolean {
        val prefs = getSharedPreferences(DeepFocusConfig.PREFS_NAME, MODE_PRIVATE)
        return prefs.getBoolean(DeepFocusConfig.KEY_BLOCKING_ACTIVE, false)
    }

    private fun isPackageAllowed(packageName: String): Boolean {
        val launcherPackage = packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            0
        )?.activityInfo?.packageName

        val isCommonLauncher = isLauncherPackage(packageName)
        val isSystemSurface = SYSTEM_PACKAGE_PREFIXES.any { prefix ->
            packageName.startsWith(prefix)
        }

        return packageName == this.packageName ||
                packageName == launcherPackage ||
                packageName in homePackages ||
                isCommonLauncher ||
                isSystemSurface ||
                packageName == "android" ||
                packageName == "com.android.launcher3"
    }

    private fun isLauncherPackage(packageName: String): Boolean {
        return packageName in homePackages ||
                COMMON_LAUNCHER_PREFIXES.any { prefix -> packageName.startsWith(prefix) }
    }

    private fun shouldDebounceAllowedSurface(packageName: String, className: String): Boolean {
        val hasOverlay = overlayView != null
        if (!hasOverlay) return false

        val elapsed = SystemClock.uptimeMillis() - overlayShownAtMs
        if (elapsed > TRANSIENT_SURFACE_DEBOUNCE_MS) return false

        return isLauncherPackage(packageName) || isTransientSystemSurface(packageName, className)
    }

    private fun isTransientSystemSurface(packageName: String, className: String): Boolean {
        val lowerClassName = className.lowercase()
        return packageName.startsWith("com.android.systemui") && (
                lowerClassName.contains("recents") ||
                        lowerClassName.contains("overview") ||
                        lowerClassName.contains("statusbar") ||
                        lowerClassName.contains("notification")
                )
    }

    private fun resolveAllHomePackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val infos: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
        return infos.mapNotNull { it.activityInfo?.packageName }.toSet()
    }

    private fun showOverlayForPackage(packageName: String) {
        if (overlayView != null && overlayForPackage == packageName) return

        removeOverlay()

        val root = FrameLayout(this).apply {
            setBackgroundColor("#99000000".toColorInt())
            isClickable = true
            isFocusable = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val appName = resolveAppName(packageName)

        val textView = TextView(this).apply {
            text = getString(R.string.deep_focus_overlay_message, appName)
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
        }

        val backButton = Button(this).apply {
            text = getString(R.string.deep_focus_overlay_button)
            setOnClickListener {
                val previouslyBlockedPackage = overlayForPackage

                // Best practice: remove immediately on explicit user action.
                removeOverlay()
                returnToAppRequestedAtMs = SystemClock.uptimeMillis()
                suppressedPackageAfterReturn = previouslyBlockedPackage
                suppressPackageUntilMs = SystemClock.uptimeMillis() + RETURN_TO_APP_GRACE_MS

                val appIntent =
                    packageManager.getLaunchIntentForPackage(this@DeepFocusAccessibilityService.packageName)
                appIntent?.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
                if (appIntent != null) {
                    startActivity(appIntent)
                } else {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }
        }

        content.addView(textView)
        content.addView(backButton)
        root.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager?.addView(root, layoutParams)
        overlayView = root
        overlayForPackage = packageName
        overlayShownAtMs = SystemClock.uptimeMillis()
        debugLog("Overlay attached for package=$packageName")
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: IllegalArgumentException) {
                // Ignore if the view was already removed by the system.
            }
            debugLog("Overlay removed for package=$overlayForPackage")
        }
        overlayView = null
        overlayForPackage = null
        overlayShownAtMs = 0L
    }

    private fun resolveAppName(packageName: String): String {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun debugLog(message: String) {
        Log.d(TAG, message)
    }
}
