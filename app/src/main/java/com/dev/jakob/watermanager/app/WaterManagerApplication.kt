package com.dev.jakob.watermanager.app

import android.app.Application
import com.dev.jakob.watermanager.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Haupt-Anwendungsklasse für WaterManager.
 * Initialisiert Koin für die Dependency Injection.
 */
class WaterManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Starte Koin
        startKoin {
            // Log Koin-Ausgaben auf INFO-Level
            androidLogger(Level.INFO)
            // Übergibt den Android-Context an Koin
            androidContext(this@WaterManagerApplication)
            // Definiere Module, die Koin zur Verfügung stellen soll
            modules(appModule)
        }
    }
}
