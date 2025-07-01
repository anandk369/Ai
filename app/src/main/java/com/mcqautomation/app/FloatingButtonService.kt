package com.mcqautomation.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class FloatingButtonService : Service() {

    companion object {
        private const val CHANNEL_ID = "FloatingButtonChannel"
        private const val NOTIFICATION_ID = 1
        var isRunning = false
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var playButton: ImageButton? = null
    private var settingsButton: ImageButton? = null
    private var optionButtonsContainer: LinearLayout? = null
    private var optionA: Button? = null
    private var optionB: Button? = null
    private var optionC: Button? = null
    private var optionD: Button? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createFloatingView()
        isRunning = true
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MCQ Automation")
            .setContentText("Floating buttons active")
            .setSmallIcon(R.drawable.ic_play)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager?.removeView(it) }
        serviceScope.cancel()
        isRunning = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Button Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createFloatingView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_buttons_layout, null)

        // Initialize views
        playButton = floatingView?.findViewById(R.id.playButton)
        settingsButton = floatingView?.findViewById(R.id.settingsButton)
        optionButtonsContainer = floatingView?.findViewById(R.id.optionButtonsContainer)
        optionA = floatingView?.findViewById(R.id.optionA)
        optionB = floatingView?.findViewById(R.id.optionB)
        optionC = floatingView?.findViewById(R.id.optionC)
        optionD = floatingView?.findViewById(R.id.optionD)

        setupClickListeners()
        setupDragging()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error creating floating button: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun setupClickListeners() {
        playButton?.setOnClickListener {
            // Start MCQ automation process
            processScreen()
        }

        settingsButton?.setOnClickListener {
            // Toggle option buttons visibility
            toggleOptionButtons()
        }

        optionA?.setOnClickListener { clickOption("A") }
        optionB?.setOnClickListener { clickOption("B") }
        optionC?.setOnClickListener { clickOption("C") }
        optionD?.setOnClickListener { clickOption("D") }
    }

    private fun toggleOptionButtons() {
        optionButtonsContainer?.let { container ->
            if (container.visibility == View.VISIBLE) {
                container.visibility = View.GONE
            } else {
                container.visibility = View.VISIBLE
            }
        }
    }

    private fun setupDragging() {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val params = floatingView?.layoutParams as WindowManager.LayoutParams
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val params = floatingView?.layoutParams as WindowManager.LayoutParams
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun processScreen() {
        serviceScope.launch {
            try {
                playButton?.setImageResource(R.drawable.ic_loading)

                // Simulate processing
                delay(1000)

                playButton?.setImageResource(R.drawable.ic_success)
                Toast.makeText(this@FloatingButtonService, "Processing complete", Toast.LENGTH_SHORT).show()

                // Reset icon after 2 seconds
                delay(2000)
                playButton?.setImageResource(R.drawable.ic_play)

            } catch (e: Exception) {
                playButton?.setImageResource(R.drawable.ic_error)
                Toast.makeText(this@FloatingButtonService, "Processing failed: ${e.message}", Toast.LENGTH_SHORT).show()

                // Reset icon after 2 seconds
                delay(2000)
                playButton?.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun clickOption(option: String) {
        serviceScope.launch {
            try {
                Toast.makeText(this@FloatingButtonService, "Clicked option $option", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@FloatingButtonService, "Failed to click option $option", Toast.LENGTH_SHORT).show()
            }
        }
    }
}