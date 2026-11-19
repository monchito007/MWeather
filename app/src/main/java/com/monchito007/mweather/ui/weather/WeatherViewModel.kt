package com.monchito007.mweather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monchito007.mweather.data.repository.WeatherRepositoryImpl
import com.monchito007.mweather.domain.location.DeviceLocationProvider
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepositoryImpl(),
    private var locationProvider: DeviceLocationProvider? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /** Survives configuration changes, so rotating the screen never triggers a second initial load. */
    private var initialLoadStarted = false

    /**
     * Returns true exactly once per ViewModel lifetime. The screen calls this on first
     * composition and, if true, starts the initial load (device location or permission
     * request). Meanwhile the UI shows the loading indicator instead of an empty screen.
     */
    fun startInitialLoad(): Boolean {
        if (initialLoadStarted) return false
        initialLoadStarted = true
        _uiState.update { it.copy(isLoading = true) }
        return true
    }

    /** Location permission was denied: fall back to the default city if nothing is shown yet. */
    fun onLocationPermissionDenied() {
        if (_uiState.value.weather == null) {
            loadWeather()
        }
    }

    fun setLocationProvider(provider: DeviceLocationProvider) {
        this.locationProvider = provider
    }

    fun fetchDeviceLocation(
        provider: DeviceLocationProvider? = locationProvider,
        onPermissionDenied: (() -> Unit)? = null
    ) {
        val activeProvider = provider ?: locationProvider
        if (activeProvider == null || !activeProvider.hasLocationPermission()) {
            onPermissionDenied?.invoke()
            if (_uiState.value.weather == null) {
                loadWeather()
            }
            return
        }

        viewModelScope.launch {
            val hasExistingData = _uiState.value.weather != null
            _uiState.update { state ->
                state.copy(
                    isLoading = !hasExistingData,
                    isRefreshing = hasExistingData,
                    errorMessage = null
                )
            }

            val deviceLocation = activeProvider.getCurrentLocation()
            if (deviceLocation != null) {
                val locationName = deviceLocation.name ?: "Ubicación actual"
                val result = repository.getWeather(
                    latitude = deviceLocation.latitude,
                    longitude = deviceLocation.longitude,
                    locationName = locationName
                )
                result.fold(
                    onSuccess = { weather ->
                        _uiState.update { state ->
                            state.copy(
                                weather = weather,
                                selectedLocation = null,
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = errorMessageFor(error)
                            )
                        }
                    }
                )
            } else {
                if (_uiState.value.weather == null) {
                    loadWeather()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = if (_uiState.value.language == AppLanguage.SPANISH) {
                                "No se pudo obtener la ubicación actual"
                            } else {
                                "Could not get current location"
                            }
                        )
                    }
                }
            }
        }
    }

    fun loadWeather(
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null
    ) {
        viewModelScope.launch {
            val hasExistingData = _uiState.value.weather != null
            _uiState.update { state ->
                state.copy(
                    isLoading = !hasExistingData,
                    isRefreshing = hasExistingData,
                    errorMessage = null
                )
            }

            val result = if (latitude != null && longitude != null) {
                repository.getWeather(latitude, longitude, locationName ?: "")
            } else {
                repository.getDefaultWeather()
            }

            result.fold(
                onSuccess = { weather ->
                    _uiState.update { state ->
                        state.copy(
                            weather = weather,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = errorMessageFor(error)
                        )
                    }
                }
            )
        }
    }

    fun refreshWeather() {
        val currentWeather = _uiState.value.weather
        val selectedLocation = _uiState.value.selectedLocation

        val lat = currentWeather?.latitude ?: selectedLocation?.latitude
        val lon = currentWeather?.longitude ?: selectedLocation?.longitude
        val name = currentWeather?.locationName ?: selectedLocation?.displayLocation

        loadWeather(latitude = lat, longitude = lon, locationName = name)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        searchJob?.cancel()

        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 2) {
            _uiState.update {
                it.copy(
                    searchResults = emptyList(),
                    isSearching = false
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300L)
            _uiState.update { it.copy(isSearching = true) }

            repository.searchLocations(trimmedQuery).fold(
                onSuccess = { locations ->
                    _uiState.update { state ->
                        state.copy(
                            searchResults = locations,
                            isSearching = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { state ->
                        state.copy(
                            searchResults = emptyList(),
                            isSearching = false
                        )
                    }
                }
            )
        }
    }

    fun selectLocation(location: LocationSearchResult) {
        _uiState.update { state ->
            state.copy(
                selectedLocation = location,
                searchQuery = "",
                searchResults = emptyList(),
                isSearchActive = false
            )
        }
        loadWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            locationName = location.displayLocation
        )
    }

    fun setSearchActive(active: Boolean) {
        _uiState.update { state ->
            state.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else state.searchQuery,
                searchResults = if (!active) emptyList() else state.searchResults
            )
        }
    }

    fun toggleTemperatureUnit() {
        _uiState.update { state ->
            val nextUnit = if (state.unit == TemperatureUnit.CELSIUS) {
                TemperatureUnit.FAHRENHEIT
            } else {
                TemperatureUnit.CELSIUS
            }
            state.copy(unit = nextUnit)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _uiState.update { state ->
            state.copy(unit = unit)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { state ->
            state.copy(themeMode = mode)
        }
    }

    fun toggleDarkTheme(isSystemInDark: Boolean = false) {
        _uiState.update { state ->
            val currentDark = state.isDarkTheme ?: isSystemInDark
            val nextMode = if (currentDark) ThemeMode.LIGHT else ThemeMode.DARK
            state.copy(themeMode = nextMode)
        }
    }

    fun setDarkTheme(isDark: Boolean?) {
        _uiState.update { state ->
            val mode = when (isDark) {
                true -> ThemeMode.DARK
                false -> ThemeMode.LIGHT
                null -> ThemeMode.SYSTEM
            }
            state.copy(themeMode = mode)
        }
    }

    fun toggleLanguage() {
        _uiState.update { state ->
            val nextLanguage = if (state.language == AppLanguage.SPANISH) {
                AppLanguage.ENGLISH
            } else {
                AppLanguage.SPANISH
            }
            state.copy(language = nextLanguage)
        }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { state ->
            state.copy(language = language)
        }
    }

    fun toggleHappyMode() {
        _uiState.update { state ->
            state.copy(isHappyMode = !state.isHappyMode)
        }
    }

    fun setHappyMode(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(isHappyMode = enabled)
        }
    }

    private fun errorMessageFor(error: Throwable): String {
        val spanish = _uiState.value.language == AppLanguage.SPANISH
        return when (error) {
            is IOException -> if (spanish) {
                "Sin conexión. Revisa tu red e inténtalo de nuevo."
            } else {
                "No connection. Check your network and try again."
            }
            else -> if (spanish) "No se pudo cargar el clima." else "Could not load weather data."
        }
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(errorMessage = null)
        }
    }
}
