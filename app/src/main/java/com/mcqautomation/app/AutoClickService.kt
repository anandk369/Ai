
package com.mcqautomation.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AutoClickService : AccessibilityService() {

    companion object {
        private var instance: AutoClickService? = null

        fun performClick(x: Float, y: Float) {
            instance?.clickAtPosition(x, y)
        }

        fun isServiceEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return enabledServices?.contains(context.packageName) == true
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("AutoClickService", "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
        // This can be used to monitor screen changes or app switches
    }

    override fun onInterrupt() {
        Log.d("AutoClickService", "Service interrupted")
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d("AutoClickService", "Service destroyed")
    }

    private fun clickAtPosition(x: Float, y: Float) {
        try {
            val path = Path().apply {
                moveTo(x, y)
            }

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()

            val result = dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    Log.d("AutoClickService", "Click gesture completed at ($x, $y)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    Log.w("AutoClickService", "Click gesture cancelled")
                }
            }, null)

            if (!result) {
                Log.e("AutoClickService", "Failed to dispatch gesture")
            }
        } catch (e: Exception) {
            Log.e("AutoClickService", "Error performing click", e)
        }
    }
}
