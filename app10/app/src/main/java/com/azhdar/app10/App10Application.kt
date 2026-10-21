package com.azhdar.app10

import android.app.Application
import com.azhdar.app10.core.ads.AdsManager
import com.azhdar.app10.core.flashlight.TorchController
import com.azhdar.app10.data.SettingsRepository

/**
 * Application entry point that owns the app-wide singletons.
 *
 * The project deliberately avoids a DI framework: a small hand written container keeps the
 * dependency graph obvious while still guaranteeing a single instance of the torch
 * controller, which is required because the camera2 torch is a global device resource.
 */
class App10Application : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual dependency container. */
class AppContainer(application: Application) {
    val torchController: TorchController = TorchController(application)
    val settingsRepository: SettingsRepository = SettingsRepository(application)
    val adsManager: AdsManager = AdsManager(application)
}
