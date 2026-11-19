package com.monchito007.mweather

import android.app.Application
import com.monchito007.mweather.di.AppContainer
import com.monchito007.mweather.di.DefaultAppContainer

class MWeatherApplication : Application() {

    /** Single dependency container for the whole process. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
