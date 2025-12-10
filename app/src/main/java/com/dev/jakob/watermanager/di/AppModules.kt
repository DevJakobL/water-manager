package com.dev.jakob.watermanager.di

import com.dev.jakob.watermanager.data.repository.WaterRepository
import com.dev.jakob.watermanager.data.source.WaterLocalDataSource
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsViewModel // Import für SettingsViewModel
import com.dev.jakob.watermanager.ui.welcome.viewmodel.WelcomeViewModel
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin-Modul für die Anwendung.
 * Hier werden alle Abhängigkeiten deklariert, die Koin bereitstellen soll.
 */
val appModule = module {
    // Stellt Gson als Singleton bereit
    single { Gson() }

    // Stellt WaterLocalDataSource als Singleton bereit
    single { WaterLocalDataSource(androidContext(), get()) } // 'get()' löst den Context und Gson auf

    // Stellt WaterRepository als Singleton bereit
    single { WaterRepository(get()) } // 'get()' löst WaterLocalDataSource auf

    // Stellt WelcomeViewModel bereit. Koin injiziert automatisch das WaterRepository.
    viewModel { WelcomeViewModel(get()) }

    // Stellt SettingsViewModel bereit. Koin injiziert automatisch das WaterRepository.
    viewModel { SettingsViewModel(get()) }
}
