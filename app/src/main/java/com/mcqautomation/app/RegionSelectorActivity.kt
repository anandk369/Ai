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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegionSelectorActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var isSelecting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("mcq_settings", Context.MODE_PRIVATE)

        val frameLayout = FrameLayout(this)
        frameLayout.setBackgroundColor(Color.TRANSPARENT)

        val overlayView = RegionOverlayView(this)
        frameLayout.addView(overlayView)

        val saveButton = Button(this).apply {
            text = "Save Region"
            setOnClickListener {
                saveRegion()
                finish()
            }
        }

        val cancelButton = Button(this).apply {
            text = "Cancel"
            setOnClickListener {
                finish()
            }
        }

        val saveButtonLayout = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            bottomMargin = 150
        }

        val cancelButtonLayout = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            bottomMargin = 100
        }

        frameLayout.addView(saveButton, saveButtonLayout)
        frameLayout.addView(cancelButton, cancelButtonLayout)

        setContentView(frameLayout)
    }

    private fun saveRegion() {
        val left = minOf(startX, endX).toInt()
        val top = minOf(startY, endY).toInt()
        val width = kotlin.math.abs(endX - startX).toInt()
        val height = kotlin.math.abs(endY - startY).toInt()

        prefs.edit()
            .putInt("capture_x", left)
            .putInt("capture_y", top)
            .putInt("capture_width", width)
            .putInt("capture_height", height)
            .apply()

        Toast.makeText(this, "Region saved", Toast.LENGTH_SHORT).show()
    }

    inner class RegionOverlayView(context: Context) : View(context) {
        private val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    isSelecting = true
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isSelecting) {
                        endX = event.x
                        endY = event.y
                        invalidate()
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    endX = event.x
                    endY = event.y
                    isSelecting = false
                    invalidate()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (isSelecting || (endX != 0f && endY != 0f)) {
                canvas.drawRect(startX, startY, endX, endY, paint)
            }
        }
    }
}