package com.mcqautomation.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

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
package com.mcqautomation.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private val OVERLAY_PERMISSION_REQUEST_CODE = 1234
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val enableFloatingButton = findViewById<Button>(R.id.enableFloatingButton)
        val enableAccessibilityButton = findViewById<Button>(R.id.enableAccessibilityButton)
        
        enableFloatingButton?.setOnClickListener {
            if (canDrawOverlays()) {
                startFloatingButtonService()
            } else {
                requestOverlayPermission()
            }
        }
        
        enableAccessibilityButton?.setOnClickListener {
            openAccessibilitySettings()
        }
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
            val intent = Intent(this, FloatingButtonService::class.java)
            startService(intent)
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
