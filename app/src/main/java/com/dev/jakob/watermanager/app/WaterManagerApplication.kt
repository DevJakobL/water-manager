package com.dev.jakob.watermanager.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkerFactory
import com.dev.jakob.watermanager.di.appModule
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Haupt-Anwendungsklasse für WaterManager.
 * Initialisiert Koin für die Dependency Injection.
 */
class WaterManagerApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        // Starte Koin
        startKoin {
            // Log Koin-Ausgaben auf INFO-Level
            androidLogger(Level.INFO)
            // Übergibt den Android-Context an Koin
            androidContext(this@WaterManagerApplication)
            // Konfiguriert die WorkManager Factory für Koin
            workManagerFactory()
            // Definiere Module, die Koin zur Verfügung stellen soll
            modules(appModule)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(getKoin().get<WorkerFactory>())
            .build()
}
