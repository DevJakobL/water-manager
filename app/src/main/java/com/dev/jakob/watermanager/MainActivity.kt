package com.dev.jakob.watermanager

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dev.jakob.watermanager.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

/**
 * The main and only Activity of the Water Manager app, following a Single-Activity architecture.
 * This activity hosts the [DrawerLayout], the [NavigationView] for navigation,
 * and a fragment container that displays different screens ([WelcomeFragment], [SettingsFragment])
 * based on user interaction.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    /**
     * Called when the activity is first created.
     * Initializes the view binding, sets up the toolbar, drawer layout, and navigation view.
     * It also loads the initial [WelcomeFragment].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            navigateToHome()
        }

        // Custom handling for the back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    // If no fragments in back stack, perform default action (exit app)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    /**
     * Handles item selections in the navigation drawer.
     * Replaces the current fragment with the selected one.
     *
     * @param item The selected [MenuItem].
     * @return True to display the item as the selected item.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navigateToHome()
            R.id.nav_settings -> navigateToSettings()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Navigates to the home screen by replacing the fragment container with [WelcomeFragment].
     * This is the default screen of the app. It also clears the back stack.
     */
    fun navigateToHome() {
        // Clear back stack to make Home the top-level destination
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, WelcomeFragment())
            .commit()
        binding.navView.setCheckedItem(R.id.nav_home)
    }

    /**
     * Navigates to the settings screen by replacing the fragment container with [SettingsFragment].
     */
    private fun navigateToSettings() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, SettingsFragment())
            .addToBackStack(null) // Allows user to navigate back to the previous fragment
            .commit()
    }
}
