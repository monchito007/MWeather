package com.monchito007.mweather.di

import android.content.Context
import com.monchito007.mweather.data.api.ApiClient
import com.monchito007.mweather.data.cache.FileForecastCache
import com.monchito007.mweather.data.location.DeviceLocationProviderImpl
import com.monchito007.mweather.data.preferences.DataStoreUserPreferencesRepository
import com.monchito007.mweather.data.preferences.UserPreferencesRepository
import com.monchito007.mweather.data.repository.WeatherRepositoryImpl
import com.monchito007.mweather.domain.location.DeviceLocationProvider
import com.monchito007.mweather.domain.repository.WeatherRepository
import java.io.File

/**
 * The app's dependency graph, built by hand. Every long-lived object is created here,
 * once per process, and consumers receive interfaces — so tests can swap in fakes by
 * constructing the ViewModel directly with their own implementations.
 */
interface AppContainer {
    val weatherRepository: WeatherRepository
    val userPreferencesRepository: UserPreferencesRepository
    val locationProvider: DeviceLocationProvider
}

class DefaultAppContainer(context: Context) : AppContainer {

    // Never hold an Activity here: the container lives as long as the process.
    private val appContext = context.applicationContext

    override val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImpl(
            api = ApiClient.openMeteoApi,
            cache = FileForecastCache(File(appContext.filesDir, "last_forecast.json"))
        )
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        DataStoreUserPreferencesRepository.create(appContext)
    }

    override val locationProvider: DeviceLocationProvider by lazy {
        DeviceLocationProviderImpl(appContext)
    }
}
