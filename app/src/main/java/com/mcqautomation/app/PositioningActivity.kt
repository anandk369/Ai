
package com.mcqautomation.app

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PositioningActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var option: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("mcq_settings", Context.MODE_PRIVATE)
        option = intent.getStringExtra("option") ?: "A"

        val frameLayout = FrameLayout(this)

        val instructionText = TextView(this).apply {
            text = "Tap on the location for option $option"
            textSize = 18f
            setPadding(20, 20, 20, 20)
        }

        val overlayView = PositioningOverlayView(this)
        frameLayout.addView(overlayView)
        frameLayout.addView(instructionText)

        val cancelButton = Button(this).apply {
            text = "Cancel"
            setOnClickListener {
                finish()
            }
        }

        val buttonLayout = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            bottomMargin = 50
        }

        frameLayout.addView(cancelButton, buttonLayout)
        setContentView(frameLayout)
    }

    inner class PositioningOverlayView(context: Context) : View(context) {
        private val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.FILL
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    
                    prefs.edit()
                        .putInt("option_${option}_x", x)
                        .putInt("option_${option}_y", y)
                        .apply()
                    
                    finish()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val savedX = prefs.getInt("option_${option}_x", -1)
            val savedY = prefs.getInt("option_${option}_y", -1)
            
            if (savedX >= 0 && savedY >= 0) {
                canvas.drawCircle(savedX.toFloat(), savedY.toFloat(), 20f, paint)
            }
        }
    }
}
