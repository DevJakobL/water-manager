package com.dev.jakob.watermanager.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.databinding.SettingsActivityBinding
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsViewModel
import com.dev.jakob.watermanager.ui.welcome.WelcomeActivity
import com.google.android.material.navigation.NavigationView
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * [SettingsActivity] ermöglicht es dem Benutzer, die Liste der Wasserbehälter anzupassen.
 * Benutzer können Behälter hinzufügen, entfernen und deren Namen sowie Größen bearbeiten.
 * Die Änderungen werden über das [SettingsViewModel] verwaltet und gespeichert.
 */
class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: SettingsActivityBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    /**
     * Das [SettingsViewModel], das über Koin injiziert wird.
     * Es verwaltet die Daten und die Logik für die [SettingsActivity].
     */
    private val viewModel: SettingsViewModel by viewModel()

    /**
     * Wird aufgerufen, wenn die Aktivität zum ersten Mal erstellt wird.
     * Initialisiert das Layout, die Toolbar, die Drawer-Navigation und beobachtet das ViewModel.
     *
     * @param savedInstanceState Wenn die Aktivität neu initialisiert wird, nachdem sie zuvor
     *                           beendet wurde, enthält dieser Bundle die Daten, die zuletzt in
     *                           [onSaveInstanceState] bereitgestellt wurden. Andernfalls ist es null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar als ActionBar setzen
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name) // Titel setzen

        // DrawerLayout und ActionBarDrawerToggle einrichten
        drawerLayout = binding.drawerLayout
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Navigations-Listener setzen
        binding.navView.setNavigationItemSelectedListener(this)

        // Initialen Zustand des Settings-Menüpunkts setzen
        binding.navView.setCheckedItem(R.id.nav_settings)

        val addButton = binding.addContainerButton
        val saveButton = binding.saveButton

        observeViewModel()

        addButton.setOnClickListener {
            viewModel.addContainer(Container("New Container", 0))
        }

        saveButton.setOnClickListener {
            viewModel.saveContainers()
            finish()
        }

        // OnBackPressedCallback für die Behandlung des Zurück-Buttons
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(binding.navView)) {
                    drawerLayout.closeDrawers()
                } else {
                    isEnabled = false // Deaktiviere den Callback, um den Standard-Zurück-Button zu ermöglichen
                    onBackPressedDispatcher.onBackPressed() // Rufe den Standard-Zurück-Button auf
                }
            }
        })
    }

    /**
     * Behandelt Klicks auf Menüpunkte in der Navigationsleiste.
     * Navigiert zur [WelcomeActivity] oder bleibt auf der [SettingsActivity].
     *
     * @param item Das ausgewählte Menüelement.
     * @return True, wenn das Ereignis verbraucht wurde, false sonst.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, WelcomeActivity::class.java))
            }
            R.id.nav_settings -> {
                // Bleibt auf der SettingsActivity
            }
        }
        drawerLayout.closeDrawers() // Drawer nach Auswahl schließen
        return true
    }

    /**
     * Beobachtet die LiveData-Objekte des [SettingsViewModel], um die UI zu aktualisieren.
     */
    private fun observeViewModel() {
        viewModel.containers.observe(this) { containers ->
            populateContainerList(containers)
        }
    }

    /**
     * Füllt die Liste der Behälteransichten basierend auf den geladenen Behältern.
     *
     * @param containers Eine Liste von [Container]-Objekten, die die Wasserbehälter repräsentieren.
     */
    private fun populateContainerList(containers: List<Container>) {
        binding.containerList.removeAllViews()
        containers.forEachIndexed { index, container ->
            addContainerView(container, index)
        }
    }

    /**
     * Fügt eine neue Ansicht für einen Wasserbehälter zur Liste hinzu.
     *
     * @param container Das [Container]-Objekt, das in der Ansicht dargestellt werden soll.
     * @param index Der Index des Containers in der Liste des ViewModels.
     */
    private fun addContainerView(container: Container, index: Int) {
        val inflater = LayoutInflater.from(this)
        val containerItemView = inflater.inflate(R.layout.container_item, binding.containerList, false)

        val nameInput = containerItemView.findViewById<EditText>(R.id.container_name_input)
        val sizeInput = containerItemView.findViewById<EditText>(R.id.container_size_input)
        val removeButton = containerItemView.findViewById<Button>(R.id.remove_container_button)

        nameInput.setText(container.name)
        sizeInput.setText(container.size.toString())

        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newName = s.toString()
                val newSize = sizeInput.text.toString().toIntOrNull() ?: 0
                viewModel.updateContainerAt(index, Container(newName, newSize))
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        sizeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newName = nameInput.text.toString()
                val newSize = s.toString().toIntOrNull() ?: 0
                viewModel.updateContainerAt(index, Container(newName, newSize))
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        removeButton.setOnClickListener {
            viewModel.removeContainerAt(index)
        }

        binding.containerList.addView(containerItemView)
    }
}
