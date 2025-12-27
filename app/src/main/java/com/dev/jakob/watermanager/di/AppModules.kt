package com.dev.jakob.watermanager.di

import androidx.room.Room
import androidx.work.WorkManager
import com.dev.jakob.watermanager.data.repository.WaterRepository
import com.dev.jakob.watermanager.data.source.AppDatabase
import com.dev.jakob.watermanager.data.source.SettingsDataStore
import com.dev.jakob.watermanager.data.source.WaterLocalDataSource
import com.dev.jakob.watermanager.ui.settings.viewmodel.SettingsViewModel
import com.dev.jakob.watermanager.ui.statistics.StatisticsViewModel
import com.dev.jakob.watermanager.ui.welcome.viewmodel.WelcomeViewModel
import com.dev.jakob.watermanager.worker.HydrationReminderWorker
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val appModule = module {
    single { Gson() }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "water_manager_db"
        ).build()
    }

    single { get<AppDatabase>().waterDao() }
    single { get<AppDatabase>().containerDao() }

    single { SettingsDataStore(androidContext()) }

    single {
        WaterLocalDataSource(
            waterDao = get(),
            containerDao = get(),
            settingsDataStore = get(),
            gson = get()
        )
    }

    single { WaterRepository(get()) }

    single { WorkManager.getInstance(androidContext()) }

    worker { HydrationReminderWorker(get(), get(), get()) }

    viewModel { WelcomeViewModel(get()) }
    viewModel { StatisticsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
