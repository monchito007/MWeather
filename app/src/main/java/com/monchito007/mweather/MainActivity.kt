package com.monchito007.mweather

import com.monchito007.mweather.ui.weather.ProvideAppLanguage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monchito007.mweather.data.location.DeviceLocationProviderImpl
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.ThemeMode
import com.monchito007.mweather.ui.weather.WeatherScreen
import com.monchito007.mweather.ui.weather.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels { WeatherViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locationProvider = DeviceLocationProviderImpl(applicationContext)
        viewModel.setLocationProvider(locationProvider)

        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            // Saved settings are read in a few ms; until then the window background is shown
            // instead of briefly rendering the default theme/language.
            if (!uiState.preferencesLoaded) return@setContent
            val isDarkTheme = when (uiState.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MWeatherTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProvideAppLanguage(uiState.language) {
                        WeatherScreen(
                            viewModel = viewModel,
                            locationProvider = locationProvider
                        )
                    }
                }
            }
        }
    }
}
