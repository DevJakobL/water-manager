package com.dev.jakob.watermanager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WelcomeActivity : AppCompatActivity() {

    private var totalWater = 0
    private lateinit var totalWaterText: TextView
    private lateinit var buttonContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private var containers = mutableListOf<Container>()

    companion object {
        private const val PREFS_NAME = "WaterManagerPrefs"
        private const val KEY_CONTAINERS = "containers"
        private const val KEY_TOTAL_WATER = "totalWater"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_activity)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        totalWaterText = findViewById(R.id.total_water_text)
        buttonContainer = findViewById(R.id.button_container)
        val settingsButton = findViewById<Button>(R.id.settings_button)

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        totalWater = sharedPreferences.getInt(KEY_TOTAL_WATER, 0)
    }

    override fun onResume() {
        super.onResume()
        loadContainers()
        populateButtons()
        updateWaterText()
    }

    private fun loadContainers() {
        val json = sharedPreferences.getString(KEY_CONTAINERS, null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Container>>() {}.type
            containers = gson.fromJson(json, type)
        } else {
            // Default containers
            containers = mutableListOf(
                Container("Glass", 250),
                Container("Bottle", 500),
                Container("Large Bottle", 1000)
            )
        }
    }

    private fun populateButtons() {
        buttonContainer.removeAllViews()
        for (container in containers) {
            val button = Button(this)
            button.text = "${container.name} (${container.size}ml)"
            button.setOnClickListener {
                totalWater += container.size
                saveTotalWater()
                updateWaterText()
            }
            buttonContainer.addView(button)
        }
    }

    private fun saveTotalWater() {
        sharedPreferences.edit().putInt(KEY_TOTAL_WATER, totalWater).apply()
    }

    private fun updateWaterText() {
        totalWaterText.text = "$totalWater ml"
    }
}