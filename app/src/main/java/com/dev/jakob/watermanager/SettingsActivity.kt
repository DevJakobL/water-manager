package com.dev.jakob.watermanager

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SettingsActivity : AppCompatActivity() {

    private lateinit var containerListLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private var containers = mutableListOf<Container>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        sharedPreferences = getSharedPreferences("WaterManagerPrefs", Context.MODE_PRIVATE)
        containerListLayout = findViewById(R.id.container_list)
        val addButton = findViewById<Button>(R.id.add_container_button)
        val saveButton = findViewById<Button>(R.id.save_button)

        loadContainers()
        populateContainerList()

        addButton.setOnClickListener {
            addContainerView(Container("New Container", 0))
        }

        saveButton.setOnClickListener {
            saveContainers()
            finish()
        }
    }

    private fun addContainerView(container: Container) {
        val inflater = LayoutInflater.from(this)
        val containerItemView = inflater.inflate(R.layout.container_item, containerListLayout, false)

        val nameInput = containerItemView.findViewById<EditText>(R.id.container_name_input)
        val sizeInput = containerItemView.findViewById<EditText>(R.id.container_size_input)
        val removeButton = containerItemView.findViewById<Button>(R.id.remove_container_button)

        nameInput.setText(container.name)
        sizeInput.setText(container.size.toString())

        removeButton.setOnClickListener {
            containerListLayout.removeView(containerItemView)
        }

        containerListLayout.addView(containerItemView)
    }

    private fun populateContainerList() {
        containerListLayout.removeAllViews()
        for (container in containers) {
            addContainerView(container)
        }
    }

    private fun loadContainers() {
        val json = sharedPreferences.getString("containers", null)
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

    private fun saveContainers() {
        val newContainers = mutableListOf<Container>()
        for (i in 0 until containerListLayout.childCount) {
            val view = containerListLayout.getChildAt(i)
            val name = view.findViewById<EditText>(R.id.container_name_input).text.toString()
            val size = view.findViewById<EditText>(R.id.container_size_input).text.toString().toIntOrNull() ?: 0
            if (name.isNotBlank()) {
                newContainers.add(Container(name, size))
            }
        }
        val json = gson.toJson(newContainers)
        sharedPreferences.edit().putString("containers", json).apply()
    }
}