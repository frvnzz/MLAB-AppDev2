package com.example.purrsistence.service

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.example.purrsistence.R
import java.util.Locale

class DeepFocusOverlayController(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onReturnClick: (String?) -> Unit,
    private val onContinueHoldComplete: (String?) -> Unit
) {

    companion object {
        private const val HOLD_TO_CONTINUE_MS = 5000L
    }

    private var overlayView: View? = null
    private var overlayForPackage: String? = null

    fun currentPackage(): String? = overlayForPackage

    fun hasOverlay(): Boolean = overlayView != null

    fun isShowingFor(packageName: String): Boolean =
        overlayView != null && overlayForPackage == packageName

    fun showOverlay(packageName: String): Boolean {
        if (isShowingFor(packageName)) return false

        // Ensure only one accessibility overlay is attached at any time.
        removeOverlay()

        val appName = resolveAppName(packageName)

        val root = FrameLayout(context).apply {
            setBackgroundColor("#CC000000".toColorInt())
            isClickable = true
            isFocusable = true
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val textView = TextView(context).apply {
            text = context.getString(R.string.deep_focus_overlay_message, appName)
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
        }

        val backButton = Button(context).apply {
            text = context.getString(R.string.deep_focus_overlay_button)
            isAllCaps = false
            // Delegate navigation/state transitions back to the service layer.
            setOnClickListener { onReturnClick(overlayForPackage) }
        }

        val continueButton = Button(context).apply {
            text = context.getString(R.string.deep_focus_overlay_continue_button)
            isAllCaps = false
            setTextColor("#CCFFFFFF".toColorInt())
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.TRANSPARENT)
                setStroke(2, "#66FFFFFF".toColorInt())
            }

            val triggerContinue = Runnable {
                onContinueHoldComplete(overlayForPackage)
            }

            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        postDelayed(triggerContinue, HOLD_TO_CONTINUE_MS)
                        true
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        removeCallbacks(triggerContinue)
                        performClick()
                        true
                    }

                    else -> false
                }
            }
        }

        content.addView(textView)
        content.addView(backButton)
        content.addView(
            continueButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )
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

        windowManager.addView(root, layoutParams)
        overlayView = root
        overlayForPackage = packageName
        return true
    }

    fun removeOverlay(): String? {
        val removedPackage = overlayForPackage
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: IllegalArgumentException) {
                // Ignore if the view was already removed by the system.
            }
        }

        overlayView = null
        overlayForPackage = null
        return removedPackage
    }

    private fun resolveAppName(packageName: String): String {
        return try {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            val label = context.packageManager.getApplicationLabel(info).toString().trim()
            // Prefer launcher label; fallback to package humanization when needed.
            if (label.isNotEmpty() && !looksLikePackageName(label)) {
                label
            } else {
                prettifyPackageName(packageName)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            prettifyPackageName(packageName)
        }
    }

    private fun looksLikePackageName(value: String): Boolean {
        // Heuristic: values like com.openai.chatgpt should be humanized.
        return value.contains('.') && value.lowercase(Locale.ROOT) == value
    }

    private fun prettifyPackageName(packageName: String): String {
        val raw = packageName.substringAfterLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()

        if (raw.isEmpty()) return packageName

        return raw.split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { token ->
                token.replaceFirstChar { c ->
                    if (c.isLowerCase()) c.titlecase(Locale.ROOT) else c.toString()
                }
            }
    }
}
