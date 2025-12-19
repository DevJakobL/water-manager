package com.dev.jakob.watermanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dev.jakob.watermanager.ui.MainScreen
import com.dev.jakob.watermanager.ui.theme.WaterManagerTheme
import org.koin.androidx.compose.KoinAndroidContext

/**
 * The main and only Activity of the Water Manager app, following a Single-Activity architecture.
 * This activity is the entry point of the application and hosts all the Composable screens.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is first created.
     * This method enables edge-to-edge display and sets up the Jetpack Compose UI content
     * for the entire application.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in [onSaveInstanceState]. Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinAndroidContext {
                WaterManagerTheme {
                    MainScreen()
                }
            }
        }
    }
}
