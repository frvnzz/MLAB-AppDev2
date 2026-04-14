package com.example.purrsistence.focus

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.example.purrsistence.service.DeepFocusAccessibilityService

object DeepFocusAccessibilityState {

    fun isServiceEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val expectedService = ComponentName(
            context,
            DeepFocusAccessibilityService::class.java
        ).flattenToString()

        return enabledServices
            .split(':')
            .any { it.equals(expectedService, ignoreCase = true) }
    }
}
