package com.dev.jakob.watermanager

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WelcomeFragment : Fragment() {

    private var totalWater = 0
    private lateinit var totalWaterText: TextView
    private lateinit var buttonContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private var containers = mutableListOf<Container>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("WaterManagerPrefs", Context.MODE_PRIVATE)
        totalWaterText = view.findViewById(R.id.total_water_text)
        buttonContainer = view.findViewById(R.id.button_container)
        return view
    }

    override fun onResume() {
        super.onResume()
        loadContainers()
        populateButtons()
        updateWaterText()
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

    private fun populateButtons() {
        buttonContainer.removeAllViews()
        for (container in containers) {
            val button = Button(requireContext())
            button.text = "${container.name} (${container.size}ml)"
            button.setOnClickListener {
                totalWater += container.size
                updateWaterText()
            }
            buttonContainer.addView(button)
        }
    }

    private fun updateWaterText() {
        totalWaterText.text = "$totalWater ml"
    }
}