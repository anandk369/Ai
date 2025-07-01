package com.mcqautomation.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.text.TextUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtons()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnEnableFloatingButton)?.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startFloatingButtonService()
            } else {
                requestOverlayPermission()
            }
        }

        findViewById<Button>(R.id.btnEnableAccessibilityService)?.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    private fun updateStatusText() {
        val statusTextView = findViewById<TextView>(R.id.statusText)
        val status = buildString {
            append("Status:\n")
            append("• Overlay Permission: ${if (Settings.canDrawOverlays(this@MainActivity)) "✓" else "✗"}\n")
            append("• Accessibility Service: ${if (AutoClickService.isServiceEnabled(this@MainActivity)) "✓" else "✗"}\n")
            append("• Floating Button: ${if (FloatingButtonService.isRunning) "✓" else "✗"}")
        }
        statusTextView?.text = status
    }

    private fun startFloatingButtonService() {
        val intent = Intent(this, FloatingButtonService::class.java)
        startForegroundService(intent)


    private fun isServiceEnabled(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )
        
        if (accessibilityEnabled == 1) {
            val service = "${packageName}/${AutoClickService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            
            return enabledServices?.let {
                TextUtils.SimpleStringSplitter(':').apply {
                    setString(it)
                }.any { it.equals(service, ignoreCase = true) }
            } ?: false
        }
        
        return false
    }

        Toast.makeText(this, "Floating button service started", Toast.LENGTH_SHORT).show()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivity(intent)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}