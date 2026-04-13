package com.example.purrsistence.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.SystemClock
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
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

    private var overlayController: DeepFocusOverlayController? = null
    private var homePackages: Set<String> = emptySet()
    private var overlayShownAtMs: Long = 0L
    private var returnToAppRequestedAtMs: Long = 0L
    private var suppressedPackageAfterReturn: String? = null
    private var suppressPackageUntilMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayController = DeepFocusOverlayController(
            context = this,
            windowManager = windowManager,
            onReturnClick = { previouslyBlockedPackage ->
                handleReturnClick(previouslyBlockedPackage)
            }
        )
        homePackages = resolveAllHomePackages()
        debugLog("Service connected. homePackages=$homePackages")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val accessibilityEvent = event ?: return
        val type = accessibilityEvent.eventType
        // React only to foreground window changes to avoid noisy event churn.
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        clearExpiredReturnToAppGrace()
        if (handleBlockingDisabled()) return

        val currentPackage = accessibilityEvent.packageName?.toString() ?: return
        val className = accessibilityEvent.className?.toString().orEmpty()

        debugLog("event type=$type pkg=$currentPackage class=$className overlayFor=${overlayController?.currentPackage()}")

        // Short-circuit checks are ordered from cheapest/safest to most specific.
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

        // Grace windows prevent immediate overlay re-attach during app-switch transitions.
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
        if (hasOverlay() && isOverlayContainer) return true

        if (hasOverlay()) {
            debugLog("Own app foreground -> remove overlay")
            removeOverlay()
        }
        return true
    }

    private fun shouldSuppressPackageAfterReturn(currentPackage: String): Boolean {
        val suppressedPackage = suppressedPackageAfterReturn ?: return false
        val isSuppressed = currentPackage == suppressedPackage
        val withinWindow = SystemClock.uptimeMillis() <= suppressPackageUntilMs
        // Ignore one quick bounce back to the previously blocked app after pressing return.
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
        val hasOverlay = hasOverlay()
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
        if (overlayController?.isShowingFor(packageName) == true) return

        val didAttach = overlayController?.showOverlay(packageName = packageName) == true
        if (didAttach) {
            overlayShownAtMs = SystemClock.uptimeMillis()
            debugLog("Overlay attached for package=$packageName")
        }
    }

    private fun removeOverlay() {
        val removedPackage = overlayController?.removeOverlay()
        if (removedPackage != null) {
            debugLog("Overlay removed for package=$removedPackage")
        }
        overlayShownAtMs = 0L
    }

    private fun hasOverlay(): Boolean = overlayController?.hasOverlay() == true

    private fun handleReturnClick(previouslyBlockedPackage: String?) {
        // Button-driven exit from overlay: hide immediately, then protect transition.
        removeOverlay()
        returnToAppRequestedAtMs = SystemClock.uptimeMillis()
        suppressedPackageAfterReturn = previouslyBlockedPackage
        suppressPackageUntilMs = SystemClock.uptimeMillis() + RETURN_TO_APP_GRACE_MS

        val appIntent = packageManager.getLaunchIntentForPackage(this.packageName)
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


    private fun debugLog(message: String) {
        Log.d(TAG, message)
    }
}
