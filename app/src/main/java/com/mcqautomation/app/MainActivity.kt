package com.mcqautomation.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        updateStatusText()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.enableFloatingButton)?.setOnClickListener {
            if (canDrawOverlays()) {
                startFloatingButtonService()
            } else {
                requestOverlayPermission()
            }
        }

        findViewById<Button>(R.id.enableAccessibilityButton)?.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    private fun updateStatusText() {
        val statusTextView = findViewById<TextView>(R.id.statusText)
        val status = buildString {
            append("Status:\n")
            append("• Overlay Permission: ${if (canDrawOverlays()) "✓" else "✗"}\n")
            append("• Floating Button: ${if (FloatingButtonService.isRunning) "✓" else "✗"}")
        }
        statusTextView?.text = status
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    private fun startFloatingButtonService() {
        try {
            if (!canDrawOverlays()) {
                Toast.makeText(this, "Overlay permission required first", Toast.LENGTH_LONG).show()
                requestOverlayPermission()
                return
            }
            
            val intent = Intent(this, FloatingButtonService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Floating button service started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Could not open accessibility settings", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (canDrawOverlays()) {
                startFloatingButtonService()
            } else {
                Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_LONG).show()
            }
        }
    }
}