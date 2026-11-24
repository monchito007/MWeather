package com.monchito007.mweather.ui.weather

import com.monchito007.mweather.MWeatherApplication
import com.monchito007.mweather.R
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monchito007.mweather.data.preferences.SavedLocation
import com.monchito007.mweather.data.preferences.UserPreferences
import com.monchito007.mweather.data.preferences.UserPreferencesRepository
import com.monchito007.mweather.domain.location.DeviceLocationProvider
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.repository.WeatherRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.io.IOException

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val locationProvider: DeviceLocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /** Survives configuration changes, so rotating the screen never triggers a second initial load. */
    private var initialLoadStarted = false

    private val storedPreferences = CompletableDeferred<UserPreferences>()

    /**
     * True once a forecast has been downloaded in this session. The weather on screen may
     * instead come from the offline cache, which must still be refreshed from the network.
     */
    private var hasFreshWeather = false

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferences.first()
            _uiState.update { state ->
                state.copy(
                    unit = prefs.unit,
                    language = prefs.language,
                    themeMode = prefs.themeMode,
                    isHappyMode = prefs.isHappyMode,
                    preferencesLoaded = true,
                    favorites = prefs.favorites
                )
            }
            storedPreferences.complete(prefs)
        }
    }

    /**
     * Called by the screen on first composition. Shows the last cached forecast at once if there
     * is one (otherwise the loading indicator), then:
     * - if the user had picked a city last time, loads it and returns false;
     * - otherwise returns true, meaning the screen must request the device location.
     * Only the first call per ViewModel lifetime does anything (rotation-safe).
     */
    suspend fun startInitialLoad(): Boolean {
        if (initialLoadStarted) return false
        _uiState.update { it.copy(isLoading = true) }
        val savedLocation = storedPreferences.await().lastLocation
        // Re-check after suspending: a recreated screen may have started the load meanwhile.
        if (initialLoadStarted) return false
        initialLoadStarted = true

        // Show the last forecast right away; the network load below then refreshes it.
        val cached = repository.getCachedWeather()
        if (cached != null && _uiState.value.weather == null) {
            _uiState.update { it.copy(weather = cached, isLoading = false) }
        }

        if (savedLocation != null) {
            loadWeather(savedLocation.latitude, savedLocation.longitude, savedLocation.name)
            return false
        }
        return true
    }

    /**
     * Location permission was denied: refresh whatever is shown (the cached city), or load the
     * default city if nothing is shown yet. Already-downloaded weather is left as it is.
     */
    fun onLocationPermissionDenied() {
        if (!hasFreshWeather) {
            refreshWeather()
        }
    }

    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()

    fun fetchDeviceLocation(onPermissionDenied: (() -> Unit)? = null) {
        if (!locationProvider.hasLocationPermission()) {
            onPermissionDenied?.invoke()
            if (!hasFreshWeather) {
                refreshWeather()
            }
            return
        }

        viewModelScope.launch {
            val hasExistingData = _uiState.value.weather != null
            _uiState.update { state ->
                state.copy(
                    isLoading = !hasExistingData,
                    isRefreshing = hasExistingData,
                    errorMessageRes = null
                )
            }

            val deviceLocation = locationProvider.getCurrentLocation()
            if (deviceLocation != null) {
                // Blank name: the UI shows a localized "Current location" label instead.
                val locationName = deviceLocation.name.orEmpty()
                val result = repository.getWeather(
                    latitude = deviceLocation.latitude,
                    longitude = deviceLocation.longitude,
                    locationName = locationName
                )
                result.fold(
                    onSuccess = { weather ->
                        hasFreshWeather = true
                        // The user chose "my location": stop restoring the last searched city.
                        viewModelScope.launch { preferencesRepository.saveLastLocation(null) }
                        _uiState.update { state ->
                            state.copy(
                                weather = weather,
                                selectedLocation = null,
                                isLoading = false,
                                isRefreshing = false,
                                errorMessageRes = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessageRes = errorMessageFor(error)
                            )
                        }
                    }
                )
            } else {
                if (!hasFreshWeather) {
                    refreshWeather()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessageRes = R.string.error_location_unavailable
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
                    errorMessageRes = null
                )
            }

            val result = if (latitude != null && longitude != null) {
                repository.getWeather(latitude, longitude, locationName ?: "")
            } else {
                repository.getDefaultWeather()
            }

            result.fold(
                onSuccess = { weather ->
                    hasFreshWeather = true
                    _uiState.update { state ->
                        state.copy(
                            weather = weather,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessageRes = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessageRes = errorMessageFor(error)
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

            repository.searchLocations(trimmedQuery, _uiState.value.language.code).fold(
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
        viewModelScope.launch {
            preferencesRepository.saveLastLocation(
                SavedLocation(location.latitude, location.longitude, location.displayLocation)
            )
        }
        loadWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            locationName = location.displayLocation
        )
    }

    /** Adds [city] to the favorites, or removes it if it already is one. */
    fun toggleFavorite(city: SavedLocation) {
        val favorites = _uiState.updateAndGet { state ->
            val existing = state.favorites.firstOrNull { it.isSamePlaceAs(city) }
            if (existing != null) {
                state.copy(
                    favorites = state.favorites - existing,
                    favoriteWeather = state.favoriteWeather - existing.key
                )
            } else {
                // The main page already has this city's weather: reuse it for the new page.
                val weather = state.weather?.takeIf { state.mainCity?.isSamePlaceAs(city) == true }
                state.copy(
                    favorites = state.favorites + city,
                    favoriteWeather = weather?.let { state.favoriteWeather + (city.key to it) } ?: state.favoriteWeather
                )
            }
        }.favorites
        viewModelScope.launch { preferencesRepository.saveFavorites(favorites) }
    }

    /** A favorite's page was shown: download its weather unless a recent one is already there. */
    fun onFavoritePageShown(index: Int) = loadFavorite(index, force = false)

    fun refreshFavorite(index: Int) = loadFavorite(index, force = true)

    private fun loadFavorite(index: Int, force: Boolean) {
        val city = _uiState.value.favorites.getOrNull(index) ?: return
        val key = city.key
        val current = _uiState.value
        if (key in current.loadingFavorites) return
        val fetchedAt = current.favoriteWeather[key]?.fetchedAtMillis
        if (!force && fetchedAt != null && System.currentTimeMillis() - fetchedAt < FAVORITE_MAX_AGE_MILLIS) return

        _uiState.update { it.copy(loadingFavorites = it.loadingFavorites + key, failedFavorites = it.failedFavorites - key) }
        viewModelScope.launch {
            val result = repository.getWeather(city.latitude, city.longitude, city.name, saveToCache = false)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { weather ->
                        state.copy(
                            favoriteWeather = state.favoriteWeather + (key to weather),
                            loadingFavorites = state.loadingFavorites - key
                        )
                    },
                    onFailure = {
                        state.copy(
                            loadingFavorites = state.loadingFavorites - key,
                            failedFavorites = state.failedFavorites + key
                        )
                    }
                )
            }
        }
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

    fun toggleTemperatureUnit() = updateSettings { state ->
        val nextUnit = if (state.unit == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
        state.copy(unit = nextUnit)
    }

    fun setTemperatureUnit(unit: TemperatureUnit) = updateSettings { it.copy(unit = unit) }

    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }

    fun toggleDarkTheme(isSystemInDark: Boolean = false) = updateSettings { state ->
        val currentDark = state.isDarkTheme ?: isSystemInDark
        state.copy(themeMode = if (currentDark) ThemeMode.LIGHT else ThemeMode.DARK)
    }

    fun setDarkTheme(isDark: Boolean?) = updateSettings { state ->
        val mode = when (isDark) {
            true -> ThemeMode.DARK
            false -> ThemeMode.LIGHT
            null -> ThemeMode.SYSTEM
        }
        state.copy(themeMode = mode)
    }

    fun toggleLanguage() = updateSettings { state ->
        val nextLanguage = if (state.language == AppLanguage.SPANISH) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.SPANISH
        }
        state.copy(language = nextLanguage)
    }

    fun setLanguage(language: AppLanguage) = updateSettings { it.copy(language = language) }

    fun toggleHappyMode() = updateSettings { it.copy(isHappyMode = !it.isHappyMode) }

    fun setHappyMode(enabled: Boolean) = updateSettings { it.copy(isHappyMode = enabled) }

    /** Updates the UI immediately and persists the display settings in the background. */
    private fun updateSettings(transform: (WeatherUiState) -> WeatherUiState) {
        val state = _uiState.updateAndGet(transform)
        viewModelScope.launch {
            preferencesRepository.saveDisplaySettings(
                unit = state.unit,
                language = state.language,
                themeMode = state.themeMode,
                isHappyMode = state.isHappyMode
            )
        }
    }

    @StringRes
    private fun errorMessageFor(error: Throwable): Int = when (error) {
        is IOException -> R.string.error_no_connection
        else -> R.string.error_load_weather
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(errorMessageRes = null)
        }
    }

    companion object {
        /** A favorite's weather younger than this is shown again without downloading it. */
        private const val FAVORITE_MAX_AGE_MILLIS = 15 * 60 * 1000L

        /** Builds the ViewModel from the app-wide [com.monchito007.mweather.di.AppContainer]. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val container = (application as MWeatherApplication).container
                WeatherViewModel(
                    repository = container.weatherRepository,
                    preferencesRepository = container.userPreferencesRepository,
                    locationProvider = container.locationProvider
                )
            }
        }
    }
}
