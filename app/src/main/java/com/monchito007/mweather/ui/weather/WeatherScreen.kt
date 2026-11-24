package com.monchito007.mweather.ui.weather

import com.monchito007.mweather.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.domain.model.HourlyForecast
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.components.CurrentWeatherCard
import com.monchito007.mweather.ui.weather.components.DailyForecastSection
import com.monchito007.mweather.ui.weather.components.ExtraMetricsSection
import com.monchito007.mweather.ui.weather.components.HourlyForecastSection
import com.monchito007.mweather.ui.weather.components.LocationSearchBar
import com.monchito007.mweather.ui.weather.components.SettingsMenuButton

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            viewModel.fetchDeviceLocation()
        } else {
            Toast.makeText(
                context,
                resources.getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.onLocationPermissionDenied()
        }
    }

    val requestLocationAndFetch = remember(viewModel) {
        {
            if (viewModel.hasLocationPermission()) {
                viewModel.fetchDeviceLocation()
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    // LaunchedEffect re-runs after a configuration change (e.g. rotation); the ViewModel
    // guard makes sure the initial location fetch / permission prompt happens only once.
    LaunchedEffect(Unit) {
        if (viewModel.startInitialLoad()) {
            requestLocationAndFetch()
        }
    }

    WeatherScreenContent(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSearchActiveChanged = viewModel::setSearchActive,
        onSelectLocation = viewModel::selectLocation,
        onToggleUnit = viewModel::toggleTemperatureUnit,
        onSelectLanguage = viewModel::setLanguage,
        onSelectThemeMode = viewModel::setThemeMode,
        onToggleHappyMode = viewModel::toggleHappyMode,
        onRequestDeviceLocation = requestLocationAndFetch,
        onRefresh = viewModel::refreshWeather,
        onClearError = viewModel::clearError,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreenContent(
    uiState: WeatherUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit,
    onSelectLocation: (com.monchito007.mweather.domain.model.LocationSearchResult) -> Unit,
    onToggleUnit: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onToggleHappyMode: () -> Unit,
    onRequestDeviceLocation: () -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Resolved here (composable scope) because stringResource can't be called inside the effect.
    val errorMessage = uiState.errorMessageRes?.let { stringResource(it) }
    val retryLabel = stringResource(R.string.retry)

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            val result = snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = retryLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRefresh()
            }
            onClearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Search Bar, Location & Settings Menu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LocationSearchBar(
                        query = uiState.searchQuery,
                        onQueryChanged = onSearchQueryChanged,
                        isActive = uiState.isSearchActive,
                        onActiveChanged = onSearchActiveChanged,
                        searchResults = uiState.searchResults,
                        isSearching = uiState.isSearching,
                        onSelectLocation = onSelectLocation,
                        modifier = Modifier.weight(1f)
                    )

                    if (!uiState.isSearchActive) {
                        IconButton(
                            onClick = onRequestDeviceLocation,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = stringResource(R.string.cd_my_location),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        SettingsMenuButton(
                            currentLanguage = uiState.language,
                            currentThemeMode = uiState.themeMode,
                            isHappyMode = uiState.isHappyMode,
                            onSelectLanguage = onSelectLanguage,
                            onSelectThemeMode = onSelectThemeMode,
                            onToggleHappyMode = onToggleHappyMode,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Main Weather Body with Pull to Refresh
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.loading_weather),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        uiState.currentWeather != null -> {
                            val currentWeather = uiState.currentWeather!!
                            val todayDaily = uiState.dailyForecast.firstOrNull()
                            val precipProb = todayDaily?.precipitationProbabilityMax

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 24.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Error Banner if present alongside cached data
                                if (errorMessage != null) {
                                    item {
                                        ErrorCard(
                                            errorMessage = errorMessage,
                                            onRetry = onRefresh,
                                        )
                                    }
                                }

                                // Current Weather Card
                                item {
                                    CurrentWeatherCard(
                                        locationName = uiState.locationName,
                                        currentWeather = currentWeather,
                                        unit = uiState.unit,
                                        onToggleUnit = onToggleUnit,
                                        maxTemp = todayDaily?.maxTemp,
                                        minTemp = todayDaily?.minTemp,
                                        precipitationProbability = precipProb,
                                        isHappyMode = uiState.isHappyMode,
                                        fetchedAtMillis = uiState.weather?.fetchedAtMillis,
                                        timezone = uiState.weather?.timezone,
                                        daily = uiState.dailyForecast
                                    )
                                }

                                // Hourly Forecast Section
                                if (uiState.hourlyForecast.isNotEmpty()) {
                                    item {
                                        HourlyForecastSection(
                                            hourlyForecast = uiState.hourlyForecast,
                                            unit = uiState.unit,
                                            isHappyMode = uiState.isHappyMode
                                        )
                                    }
                                }

                                // 7-Day Forecast Section
                                if (uiState.dailyForecast.isNotEmpty()) {
                                    item {
                                        DailyForecastSection(
                                            dailyForecast = uiState.dailyForecast,
                                            unit = uiState.unit,
                                            isHappyMode = uiState.isHappyMode
                                        )
                                    }
                                }

                                // Extra Metrics Section
                                item {
                                    ExtraMetricsSection(
                                        currentWeather = currentWeather,
                                        precipitationProbability = precipProb,
                                        today = uiState.dailyForecast.getOrNull(0),
                                        tomorrow = uiState.dailyForecast.getOrNull(1),
                                        unit = uiState.unit,
                                    )
                                }
                            }
                        }

                        else -> {
                            // Empty or Failure State
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = errorMessage ?: stringResource(R.string.no_weather_data_available),
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = onRefresh) {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = stringResource(R.string.retry))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.retry),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    val sampleWeather = Weather(
        locationName = "San José, Costa Rica",
        latitude = 9.9281,
        longitude = -84.0907,
        timezone = "America/Costa_Rica",
        current = CurrentWeather(
            temperature = 23.5,
            apparentTemperature = 22.0,
            humidity = 68,
            windSpeed = 14.2,
            windDirection = 180,
            weatherCode = 1,
            weatherDescription = "Mainly clear",
            isDay = true,
            precipitation = 0.0,
            pressure = 1014.0,
            cloudCover = 15,
            uvIndex = 6.2
        ),
        hourly = List(12) { i ->
            HourlyForecast(
                time = "2025-01-01T${10 + i}:00",
                temperature = 20.0 + i,
                weatherCode = if (i % 4 == 0) 61 else 0,
                weatherDescription = "Clear",
                precipitationProbability = if (i % 4 == 0) 35 else 0,
                precipitation = 0.0,
                humidity = 60,
                windSpeed = 12.0,
                isDay = true
            )
        },
        daily = List(7) { i ->
            DailyForecast(
                date = "2025-01-0${i + 1}",
                weatherCode = if (i % 3 == 0) 61 else 0,
                weatherDescription = "Sunny",
                maxTemp = 25.0 + i,
                minTemp = 16.0 + i,
                precipitationSum = 0.0,
                precipitationProbabilityMax = if (i % 3 == 0) 40 else 10,
                windSpeedMax = 16.0,
                uvIndexMax = 7.0,
                sunrise = "05:45",
                sunset = "17:45"
            )
        }
    )

    MWeatherTheme {
        WeatherScreenContent(
            uiState = WeatherUiState(
                weather = sampleWeather,
                isLoading = false
            ),
            onSearchQueryChanged = {},
            onSearchActiveChanged = {},
            onSelectLocation = {},
            onToggleUnit = {},
            onSelectLanguage = {},
            onSelectThemeMode = {},
            onToggleHappyMode = {},
            onRequestDeviceLocation = {},
            onRefresh = {},
            onClearError = {}
        )
    }
}
