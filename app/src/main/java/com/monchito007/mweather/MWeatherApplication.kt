package com.monchito007.mweather

import android.app.Application
import com.monchito007.mweather.di.AppContainer
import com.monchito007.mweather.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MWeatherApplication : Application() {

    /** Single dependency container for the whole process. */
    lateinit var container: AppContainer
        private set

    /** For work that must outlive any screen, such as refreshing the home-screen widget. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
