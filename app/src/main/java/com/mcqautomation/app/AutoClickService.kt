package com.mcqautomation.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AutoClickService : AccessibilityService() {

    companion object {
        private var instance: AutoClickService? = null

        suspend fun performClick(x: Float, y: Float): Boolean {
            return instance?.performGestureClick(x, y) ?: false
        }

        fun isServiceEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, AutoClickService::class.java)
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = ComponentName.unflattenFromString(colonSplitter.next())
                if (componentName != null && componentName == expectedComponentName) {
                    return true
                }
            }

            return false
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
    }

    override fun onInterrupt() {
        // Handle service interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    private suspend fun performGestureClick(x: Float, y: Float): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val path = Path().apply {
                moveTo(x, y)
            }

            val gestureBuilder = GestureDescription.Builder()
            val strokeDescription = GestureDescription.StrokeDescription(path, 0, 100)
            gestureBuilder.addStroke(strokeDescription)

            val gesture = gestureBuilder.build()

            val callback = object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    continuation.resume(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    continuation.resume(false)
                }
            }

            val result = dispatchGesture(gesture, callback, null)
            if (!result) {
                continuation.resume(false)
            }
        }
    }
}