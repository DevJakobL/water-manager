package com.dev.jakob.watermanager

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

/**
 * Die Hauptaktivität der Water Manager App.
 * Diese Aktivität verwaltet die Navigation mittels einer Drawer-Navigation
 * und lädt verschiedene Fragmente basierend auf der Benutzerauswahl.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private val TAG = "MainActivity"

    /**
     * Wird aufgerufen, wenn die Aktivität zum ersten Mal erstellt wird.
     * Initialisiert das Layout, die Drawer-Navigation und lädt das Startfragment.
     *
     * @param savedInstanceState Wenn die Aktivität neu initialisiert wird, nachdem sie zuvor
     *                           beendet wurde, enthält dieser Bundle die Daten, die zuletzt in
     *                           [onSaveInstanceState] bereitgestellt wurden. Andernfalls ist es null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity started")
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: Layout set")

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
        Log.d(TAG, "onCreate: DrawerLayout and NavigationView initialized")

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: Replacing fragment with WelcomeFragment")
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, WelcomeFragment())
                .commit()
            navView.setCheckedItem(R.id.nav_home)
        }

        // OnBackPressedCallback für die Behandlung des Zurück-Buttons
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    isEnabled = false // Deaktiviere den Callback, um den Standard-Zurück-Button zu ermöglichen
                    onBackPressedDispatcher.onBackPressed() // Rufe den Standard-Zurück-Button auf
                }
            }
        })
    }

    /**
     * Wird aufgerufen, wenn ein Element in der Navigationsansicht ausgewählt wird.
     *
     * @param item Das ausgewählte Menüelement.
     * @return True, wenn das Ereignis verbraucht wurde, false sonst.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, WelcomeFragment())
                    .commit()
            }
            R.id.nav_settings -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, SettingsFragment())
                    .commit()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    /**
     * Diese Hook wird aufgerufen, wenn ein Element in der Optionsleiste ausgewählt wird.
     *
     * @param item Das Menüelement, das ausgewählt wurde.
     * @return True, wenn das Ereignis verbraucht wurde, false sonst.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
