package com.dev.jakob.watermanager.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.dev.jakob.watermanager.R
import com.dev.jakob.watermanager.data.model.Container
import com.dev.jakob.watermanager.databinding.WelcomeActivityBinding
import com.dev.jakob.watermanager.ui.settings.SettingsActivity
import com.dev.jakob.watermanager.ui.welcome.viewmodel.WelcomeViewModel
import com.google.android.material.navigation.NavigationView
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * [WelcomeActivity] ist die Hauptaktivität der Anwendung, die dem Benutzer die Möglichkeit bietet,
 * Wasser hinzuzufügen und auf die Einstellungen zuzugreifen.
 *
 * Sie verwendet ein [WelcomeViewModel], um die UI-Logik und Datenhaltung zu verwalten.
 */
class WelcomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: WelcomeActivityBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    /**
     * Das [WelcomeViewModel], das über Koin injiziert wird.
     * Es verwaltet die Daten und die Logik für die [WelcomeActivity].
     */
    private val viewModel: WelcomeViewModel by viewModel()

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
        binding = WelcomeActivityBinding.inflate(layoutInflater)
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

        // Initialen Zustand des Home-Menüpunkts setzen
        binding.navView.setCheckedItem(R.id.nav_home)

        observeViewModel()

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
     * Wird aufgerufen, wenn die Aktivität in den Vordergrund tritt und mit dem Benutzer interagiert.
     * Aktualisiert die Daten, z.B. nach Rückkehr aus den Einstellungen.
     */
    override fun onResume() {
        super.onResume()
        // Daten aktualisieren, wenn die Aktivität fortgesetzt wird, z.B. nach Rückkehr aus den Einstellungen
        viewModel.refreshData()
    }

    /**
     * Behandelt Klicks auf Menüpunkte in der Navigationsleiste.
     * Navigiert zur [SettingsActivity] oder bleibt auf der [WelcomeActivity].
     *
     * @param item Das ausgewählte Menüelement.
     * @return True, wenn das Ereignis verbraucht wurde, false sonst.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Bleibt auf der WelcomeActivity
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        drawerLayout.closeDrawers() // Drawer nach Auswahl schließen
        return true
    }

    /**
     * Beobachtet die LiveData-Objekte des [WelcomeViewModel], um die UI zu aktualisieren.
     * Aktualisiert die Anzeige der gesamten Wassermenge und der Buttons für die Wasserbehälter.
     */
    private fun observeViewModel() {
        viewModel.totalWater.observe(this) { totalWater ->
            binding.totalWaterText.text = getString(R.string.water_amount_ml, totalWater)
        }

        viewModel.containers.observe(this) { containers ->
            populateButtons(containers)
        }
    }

    /**
     * Füllt den Button-Container mit Buttons für jeden verfügbaren Wasserbehälter.
     * Wenn ein Button geklickt wird, wird die entsprechende Wassermenge zum ViewModel hinzugefügt.
     *
     * @param containers Eine Liste von [Container]-Objekten, die die Wasserbehälter repräsentieren.
     */
    private fun populateButtons(containers: List<Container>) {
        binding.buttonContainer.removeAllViews()
        for (container in containers) {
            val button = Button(this)
            button.text = getString(R.string.container_button_text, container.name, container.size)
            button.setOnClickListener {
                viewModel.addWater(container)
            }
            binding.buttonContainer.addView(button)
        }
    }
}
