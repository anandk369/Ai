package com.mcqautomation.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var selectRegionButton: Button
    private lateinit var resolutionRadioGroup: RadioGroup
    private lateinit var radio480p: RadioButton
    private lateinit var radio720p: RadioButton
    private lateinit var radio1080p: RadioButton
    private lateinit var showOptionButtonsSwitch: Switch
    private lateinit var positioningButtonsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("mcq_settings", Context.MODE_PRIVATE)
        initViews()
        setupListeners()
        loadSettings()
    }

    private fun initViews() {
        selectRegionButton = findViewById(R.id.selectRegionButton)
        resolutionRadioGroup = findViewById(R.id.resolutionRadioGroup)
        radio480p = findViewById(R.id.radio480p)
        radio720p = findViewById(R.id.radio720p)
        radio1080p = findViewById(R.id.radio1080p)
        showOptionButtonsSwitch = findViewById(R.id.showOptionButtonsSwitch)
        positioningButtonsContainer = findViewById(R.id.positioningButtonsContainer)
    }

    private fun setupListeners() {
        selectRegionButton.setOnClickListener {
            openRegionSelector()
        }

        resolutionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val resolution = when (checkedId) {
                R.id.radio480p -> "480p"
                R.id.radio720p -> "720p"
                R.id.radio1080p -> "1080p"
                else -> "720p"
            }
            prefs.edit().putString("resolution", resolution).apply()
        }

        showOptionButtonsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_option_buttons", isChecked).apply()
            updatePositioningButtonsVisibility()
        }

        findViewById<Button>(R.id.positionOptionA).setOnClickListener { startPositioning("A") }
        findViewById<Button>(R.id.positionOptionB).setOnClickListener { startPositioning("B") }
        findViewById<Button>(R.id.positionOptionC).setOnClickListener { startPositioning("C") }
        findViewById<Button>(R.id.positionOptionD).setOnClickListener { startPositioning("D") }
    }

    private fun loadSettings() {
        val resolution = prefs.getString("resolution", "720p")
        when (resolution) {
            "480p" -> radio480p.isChecked = true
            "720p" -> radio720p.isChecked = true
            "1080p" -> radio1080p.isChecked = true
            else -> radio720p.isChecked = true
        }

        val showButtons = prefs.getBoolean("show_option_buttons", false)
        showOptionButtonsSwitch.isChecked = showButtons
        updatePositioningButtonsVisibility()
    }

    private fun updatePositioningButtonsVisibility() {
        val showButtons = showOptionButtonsSwitch.isChecked
        positioningButtonsContainer.visibility = if (showButtons) View.VISIBLE else View.GONE
    }

    private fun openRegionSelector() {
        val intent = Intent(this, RegionSelectorActivity::class.java)
        startActivity(intent)
    }

    private fun startPositioning(option: String) {
        val intent = Intent(this, PositioningActivity::class.java)
        intent.putExtra("option", option)
        startActivity(intent)
    }
}