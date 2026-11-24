package com.monchito007.mweather

import com.monchito007.mweather.data.preferences.SavedLocation
import com.monchito007.mweather.data.preferences.UserPreferences
import com.monchito007.mweather.data.preferences.UserPreferencesRepository
import com.monchito007.mweather.domain.location.DeviceLocation
import com.monchito007.mweather.domain.location.DeviceLocationProvider
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.domain.model.HourlyForecast
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.domain.repository.WeatherRepository
import com.monchito007.mweather.ui.weather.AppLanguage
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.ThemeMode
import com.monchito007.mweather.ui.weather.WeatherUtils
import com.monchito007.mweather.ui.weather.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeWeatherRepository
    private lateinit var fakePreferences: FakeUserPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
        fakePreferences = FakeUserPreferencesRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Creates a ViewModel and performs the default-city load the screen would trigger. */
    private fun newViewModel(provider: DeviceLocationProvider = FakeDeviceLocationProvider()): WeatherViewModel =
        bareViewModel(provider).also { it.loadWeather() }

    /** A ViewModel exactly as the app creates it, with no weather loaded yet. */
    private fun bareViewModel(provider: DeviceLocationProvider = FakeDeviceLocationProvider()): WeatherViewModel =
        WeatherViewModel(
            repository = fakeRepository,
            preferencesRepository = fakePreferences,
            locationProvider = provider
        )

    @Test
    fun testViewModelDoesNotLoadOnConstruction() = runTest {
        val viewModel = bareViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.weather)
        assertEquals(0, fakeRepository.weatherRequests)
    }

    @Test
    fun testStartInitialLoadOnlyOnce() = runTest {
        val viewModel = bareViewModel()

        assertTrue(viewModel.startInitialLoad())
        assertTrue(viewModel.uiState.value.isLoading)
        // Simulates the screen being recreated (e.g. rotation)
        assertFalse(viewModel.startInitialLoad())
    }

    @Test
    fun testPermissionDeniedLoadsDefaultCityOnce() = runTest {
        val viewModel = bareViewModel()
        viewModel.startInitialLoad()

        viewModel.onLocationPermissionDenied()
        advanceUntilIdle()
        assertEquals("Default City", viewModel.uiState.value.weather?.locationName)
        assertFalse(viewModel.uiState.value.isLoading)

        // Already showing data: denying again must not reload
        viewModel.onLocationPermissionDenied()
        advanceUntilIdle()
        assertEquals(1, fakeRepository.weatherRequests)
    }

    @Test
    fun testCachedWeatherIsShownAtOnceThenRefreshedFromNetwork() = runTest {
        fakeRepository.cachedWeather = fakeRepository.weatherAt(48.85, 2.35, "Paris, France")
        val viewModel = bareViewModel()

        assertTrue(viewModel.startInitialLoad()) // still asks for the device location
        val shown = viewModel.uiState.value
        assertEquals("Paris, France", shown.weather?.locationName)
        assertFalse(shown.isLoading) // no full-screen spinner over the cached data

        viewModel.fetchDeviceLocation()
        advanceUntilIdle()
        assertEquals("Madrid", viewModel.uiState.value.weather?.locationName)
    }

    @Test
    fun testOfflineStartKeepsCachedWeatherAndShowsError() = runTest {
        fakeRepository.cachedWeather = fakeRepository.weatherAt(48.85, 2.35, "Paris, France")
        fakeRepository.shouldReturnError = true
        fakePreferences.state.value = UserPreferences(lastLocation = SavedLocation(48.85, 2.35, "Paris, France"))
        val viewModel = bareViewModel()

        viewModel.startInitialLoad()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Paris, France", state.weather?.locationName)
        assertNotNull(state.errorMessageRes)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun testPermissionDeniedRefreshesCachedCityInsteadOfDefault() = runTest {
        fakeRepository.cachedWeather = fakeRepository.weatherAt(48.85, 2.35, "Paris, France")
        val viewModel = bareViewModel()
        viewModel.startInitialLoad()

        viewModel.onLocationPermissionDenied()
        advanceUntilIdle()

        assertEquals(1, fakeRepository.weatherRequests)
        assertEquals("Paris, France", viewModel.uiState.value.weather?.locationName)
        assertEquals(48.85, fakeRepository.lastRequestedLatitude ?: 0.0, 0.001)
    }

    @Test
    fun testSavedSettingsAreRestoredOnStart() = runTest {
        fakePreferences.state.value = UserPreferences(
            unit = TemperatureUnit.FAHRENHEIT,
            language = AppLanguage.ENGLISH,
            themeMode = ThemeMode.DARK,
            isHappyMode = true
        )
        val viewModel = bareViewModel()
        assertFalse(viewModel.uiState.value.preferencesLoaded)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.preferencesLoaded)
        assertEquals(TemperatureUnit.FAHRENHEIT, state.unit)
        assertEquals(AppLanguage.ENGLISH, state.language)
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertTrue(state.isHappyMode)
    }

    @Test
    fun testChangingSettingsPersistsThem() = runTest {
        val viewModel = bareViewModel()
        advanceUntilIdle()

        viewModel.toggleTemperatureUnit()
        viewModel.setLanguage(AppLanguage.ENGLISH)
        viewModel.setThemeMode(ThemeMode.LIGHT)
        viewModel.toggleHappyMode()
        advanceUntilIdle()

        val saved = fakePreferences.state.value
        assertEquals(TemperatureUnit.FAHRENHEIT, saved.unit)
        assertEquals(AppLanguage.ENGLISH, saved.language)
        assertEquals(ThemeMode.LIGHT, saved.themeMode)
        assertTrue(saved.isHappyMode)
    }

    @Test
    fun testSelectingCitySavesItAsLastLocation() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.selectLocation(
            LocationSearchResult(id = 1L, name = "Tokyo", country = "Japan", latitude = 35.6762, longitude = 139.6503)
        )
        advanceUntilIdle()

        assertEquals(SavedLocation(35.6762, 139.6503, "Tokyo, Japan"), fakePreferences.state.value.lastLocation)
    }

    @Test
    fun testStartWithSavedCityLoadsItWithoutDeviceLocation() = runTest {
        fakePreferences.state.value = UserPreferences(lastLocation = SavedLocation(48.85, 2.35, "Paris, France"))
        val viewModel = bareViewModel()

        val needsDeviceLocation = viewModel.startInitialLoad()
        advanceUntilIdle()

        assertFalse(needsDeviceLocation)
        assertEquals("Paris, France", viewModel.uiState.value.weather?.locationName)
        assertEquals(48.85, viewModel.uiState.value.weather?.latitude ?: 0.0, 0.001)
    }

    @Test
    fun testUsingDeviceLocationForgetsSavedCity() = runTest {
        fakePreferences.state.value = UserPreferences(lastLocation = SavedLocation(48.85, 2.35, "Paris, France"))
        val viewModel = newViewModel(FakeDeviceLocationProvider())
        advanceUntilIdle()

        viewModel.fetchDeviceLocation()
        advanceUntilIdle()

        assertEquals("Madrid", viewModel.uiState.value.weather?.locationName)
        assertNull(fakePreferences.state.value.lastLocation)
    }

    @Test
    fun testFailureWithExistingDataKeepsWeatherAndShowsError() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        fakeRepository.shouldReturnError = true
        viewModel.refreshWeather()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Default City", state.weather?.locationName)
        assertNotNull(state.errorMessageRes)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun testInitialStateLoadsDefaultWeather() = runTest {
        val viewModel = newViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.weather)
        assertEquals("Default City", state.weather?.locationName)
        assertNull(state.errorMessageRes)
    }

    @Test
    fun testFetchDeviceLocationSuccess() = runTest {
        val fakeLocationProvider = FakeDeviceLocationProvider()
        val viewModel = newViewModel(fakeLocationProvider)
        advanceUntilIdle()

        viewModel.fetchDeviceLocation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Madrid", state.weather?.locationName)
        assertEquals(40.4168, state.weather?.latitude ?: 0.0, 0.001)
        assertEquals(-3.7038, state.weather?.longitude ?: 0.0, 0.001)
        assertNull(state.selectedLocation)
    }

    @Test
    fun testFetchDeviceLocationPermissionDenied() = runTest {
        val fakeLocationProvider = FakeDeviceLocationProvider(hasPermission = false)
        val viewModel = newViewModel(fakeLocationProvider)
        advanceUntilIdle()

        var permissionDeniedCalled = false
        viewModel.fetchDeviceLocation(onPermissionDenied = { permissionDeniedCalled = true })
        advanceUntilIdle()

        assertTrue(permissionDeniedCalled)
        assertEquals("Default City", viewModel.uiState.value.weather?.locationName)
    }

    @Test
    fun testFetchDeviceLocationNullLocationFallback() = runTest {
        val fakeLocationProvider = FakeDeviceLocationProvider(hasPermission = true, mockLocation = null)
        val viewModel = newViewModel(fakeLocationProvider)
        advanceUntilIdle()

        viewModel.fetchDeviceLocation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.weather)
    }

    @Test
    fun testLoadWeatherSuccess() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.loadWeather(latitude = 51.5074, longitude = -0.1278, locationName = "London")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("London", state.weather?.locationName)
        assertEquals(51.5074, state.weather?.latitude ?: 0.0, 0.001)
        assertFalse(state.isLoading)
        assertNull(state.errorMessageRes)
    }

    @Test
    fun testLoadWeatherFailure() = runTest {
        fakeRepository.shouldReturnError = true
        val viewModel = newViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.weather)
        assertEquals(R.string.error_load_weather, state.errorMessageRes)
    }

    @Test
    fun testRefreshWeather() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.refreshWeather()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.weather)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun testSearchLocationsDebounceAndSuccess() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Paris")
        advanceTimeBy(100) // Before debounce completes
        assertTrue(viewModel.uiState.value.searchResults.isEmpty())

        advanceTimeBy(250) // After 300ms debounce completes
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Paris", state.searchQuery)
        assertEquals(1, state.searchResults.size)
        assertEquals("Paris", state.searchResults.first().name)
        assertFalse(state.isSearching)
    }

    @Test
    fun testSearchLocationsShortQueryClearsResults() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("P")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("P", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.isSearching)
    }

    @Test
    fun testSelectLocation() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        val selectedLoc = LocationSearchResult(
            id = 1L,
            name = "Tokyo",
            country = "Japan",
            latitude = 35.6762,
            longitude = 139.6503
        )

        viewModel.selectLocation(selectedLoc)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(selectedLoc, state.selectedLocation)
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.isSearchActive)
        assertEquals("Tokyo, Japan", state.weather?.locationName)
    }

    @Test
    fun testToggleTemperatureUnit() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        assertEquals(TemperatureUnit.CELSIUS, viewModel.uiState.value.unit)

        viewModel.toggleTemperatureUnit()
        assertEquals(TemperatureUnit.FAHRENHEIT, viewModel.uiState.value.unit)

        viewModel.toggleTemperatureUnit()
        assertEquals(TemperatureUnit.CELSIUS, viewModel.uiState.value.unit)
    }

    @Test
    fun testToggleLanguage() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        assertEquals(AppLanguage.SPANISH, viewModel.uiState.value.language)

        viewModel.toggleLanguage()
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)

        viewModel.toggleLanguage()
        assertEquals(AppLanguage.SPANISH, viewModel.uiState.value.language)
    }

    @Test
    fun testSetLanguage() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.setLanguage(AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)

        viewModel.setLanguage(AppLanguage.SPANISH)
        assertEquals(AppLanguage.SPANISH, viewModel.uiState.value.language)
    }

    @Test
    fun testWeatherUtilsReturnsStringResources() {
        assertEquals(R.string.weather_clear_sky, WeatherUtils.weatherDescriptionRes(0))
        assertEquals(R.string.weather_thunderstorm, WeatherUtils.weatherDescriptionRes(95))
        assertEquals(R.string.weather_thunderstorm_hail, WeatherUtils.weatherDescriptionRes(99))
        assertEquals(R.string.weather_unknown, WeatherUtils.weatherDescriptionRes(999))

        assertEquals(R.string.uv_low, WeatherUtils.uvIndexDescriptionRes(2.0))
        assertEquals(R.string.uv_very_high, WeatherUtils.uvIndexDescriptionRes(9.0))
        assertEquals(R.string.uv_extreme, WeatherUtils.uvIndexDescriptionRes(11.0))
    }

    @Test
    fun testDayOfWeekIndex() {
        assertEquals(0, WeatherUtils.dayOfWeekIndex("2026-11-22")) // Sunday
        assertEquals(4, WeatherUtils.dayOfWeekIndex("2026-11-19")) // Thursday
        assertEquals(6, WeatherUtils.dayOfWeekIndex("2026-11-21T12:00")) // Saturday
        assertNull(WeatherUtils.dayOfWeekIndex("not-a-date"))
        assertNull(WeatherUtils.dayOfWeekIndex("2026-02-30"))
    }

    @Test
    fun testSearchUsesAppLanguage() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Paris")
        advanceUntilIdle()
        assertEquals("es", fakeRepository.lastSearchLanguage)

        viewModel.setLanguage(AppLanguage.ENGLISH)
        viewModel.onSearchQueryChanged("London")
        advanceUntilIdle()
        assertEquals("en", fakeRepository.lastSearchLanguage)
    }

    @Test
    fun testToggleDarkTheme() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.isDarkTheme)

        // Toggling when system is light mode (false) should set isDarkTheme to true
        viewModel.toggleDarkTheme(isSystemInDark = false)
        assertEquals(true, viewModel.uiState.value.isDarkTheme)

        // Toggling again should set isDarkTheme to false
        viewModel.toggleDarkTheme()
        assertEquals(false, viewModel.uiState.value.isDarkTheme)

        // Toggling again should set isDarkTheme to true
        viewModel.toggleDarkTheme()
        assertEquals(true, viewModel.uiState.value.isDarkTheme)
    }

    @Test
    fun testSetDarkTheme() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.setDarkTheme(true)
        assertEquals(true, viewModel.uiState.value.isDarkTheme)

        viewModel.setDarkTheme(false)
        assertEquals(false, viewModel.uiState.value.isDarkTheme)

        viewModel.setDarkTheme(null)
        assertNull(viewModel.uiState.value.isDarkTheme)
    }

    @Test
    fun testTemperatureUnitFormattingAndConversion() {
        assertEquals("20°", TemperatureUnit.CELSIUS.formatTemperature(20.0))
        assertEquals("68°", TemperatureUnit.FAHRENHEIT.formatTemperature(20.0))
        assertEquals("°C", TemperatureUnit.CELSIUS.symbol)
        assertEquals("°F", TemperatureUnit.FAHRENHEIT.symbol)

        assertEquals(0.0, TemperatureUnit.CELSIUS.convertTemperature(0.0), 0.001)
        assertEquals(32.0, TemperatureUnit.FAHRENHEIT.convertTemperature(0.0), 0.001)
    }

    @Test
    fun testSetSearchActive() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.setSearchActive(true)
        assertTrue(viewModel.uiState.value.isSearchActive)

        viewModel.setSearchActive(false)
        assertFalse(viewModel.uiState.value.isSearchActive)
    }

    @Test
    fun testClearError() = runTest {
        fakeRepository.shouldReturnError = true
        val viewModel = newViewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessageRes)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun favoritesAreRestoredFromPreferences() = runTest {
        fakePreferences.state.update { it.copy(favorites = listOf(SavedLocation(41.39, 2.17, "Barcelona"))) }
        val viewModel = bareViewModel()
        advanceUntilIdle()

        assertEquals(listOf("Barcelona"), viewModel.uiState.value.favorites.map { it.name })
    }

    @Test
    fun toggleFavoriteAddsReusingTheShownWeatherThenRemoves() = runTest {
        val viewModel = bareViewModel()
        val madrid = LocationSearchResult(id = 1, name = "Madrid", latitude = 40.42, longitude = -3.70, country = "Spain")
        viewModel.selectLocation(madrid)
        advanceUntilIdle()
        val requestsBefore = fakeRepository.weatherRequests
        val city = viewModel.uiState.value.mainCity!!

        viewModel.toggleFavorite(city)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isFavorite(city))
        assertEquals(listOf(city), fakePreferences.state.value.favorites)
        // The new page already has the weather shown on the main page: no extra download
        viewModel.onFavoritePageShown(0)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.favoriteWeather[city.key])

        viewModel.toggleFavorite(city)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isFavorite(city))
        assertTrue(fakePreferences.state.value.favorites.isEmpty())
        assertTrue(fakeRepository.weatherRequests >= requestsBefore)
    }

    @Test
    fun favoritePageLoadsItsWeatherWithoutTouchingTheCache() = runTest {
        fakePreferences.state.update { it.copy(favorites = listOf(SavedLocation(41.39, 2.17, "Barcelona"))) }
        val viewModel = bareViewModel()
        advanceUntilIdle()

        viewModel.onFavoritePageShown(0)
        advanceUntilIdle()

        val key = viewModel.uiState.value.favorites.first().key
        assertEquals("Barcelona", viewModel.uiState.value.favoriteWeather[key]?.locationName)
        assertEquals(false, fakeRepository.lastSaveToCache)
        assertNull(viewModel.uiState.value.weather) // the main page is untouched
    }

    @Test
    fun failedFavoriteIsMarkedAndCanBeRetried() = runTest {
        fakePreferences.state.update { it.copy(favorites = listOf(SavedLocation(41.39, 2.17, "Barcelona"))) }
        val viewModel = bareViewModel()
        advanceUntilIdle()
        val key = viewModel.uiState.value.favorites.first().key

        fakeRepository.shouldReturnError = true
        viewModel.onFavoritePageShown(0)
        advanceUntilIdle()
        assertTrue(key in viewModel.uiState.value.failedFavorites)

        fakeRepository.shouldReturnError = false
        viewModel.refreshFavorite(0)
        advanceUntilIdle()
        assertFalse(key in viewModel.uiState.value.failedFavorites)
        assertNotNull(viewModel.uiState.value.favoriteWeather[key])
    }

    @Test
    fun samePlaceToleratesTheApiSnappingCoordinates() {
        val searched = SavedLocation(40.4168, -3.7038, "Madrid")
        assertTrue(searched.isSamePlaceAs(SavedLocation(40.4, -3.7, "Madrid")))
        assertFalse(searched.isSamePlaceAs(SavedLocation(41.39, 2.17, "Barcelona")))
    }
}

class FakeDeviceLocationProvider(
    var hasPermission: Boolean = true,
    var mockLocation: DeviceLocation? = DeviceLocation(40.4168, -3.7038, "Madrid")
) : DeviceLocationProvider {
    override suspend fun getCurrentLocation(): DeviceLocation? {
        return if (hasPermission) mockLocation else null
    }

    override fun hasLocationPermission(): Boolean = hasPermission

}

class FakeWeatherRepository : WeatherRepository {
    var shouldReturnError = false
    var weatherRequests = 0
        private set
    var lastRequestedLatitude: Double? = null
        private set
    var lastSaveToCache: Boolean? = null
        private set

    /** What [getCachedWeather] returns: the forecast "saved on disk" by a previous run. */
    var cachedWeather: Weather? = null

    override suspend fun getCachedWeather(): Weather? = cachedWeather

    fun weatherAt(latitude: Double, longitude: Double, name: String): Weather =
        sampleWeather.copy(locationName = name, latitude = latitude, longitude = longitude)

    private val sampleWeather = Weather(
        locationName = "Default City",
        latitude = 0.0,
        longitude = 0.0,
        timezone = "UTC",
        current = CurrentWeather(
            temperature = 20.0,
            apparentTemperature = 19.5,
            humidity = 60,
            windSpeed = 10.0,
            windDirection = 180,
            weatherCode = 0,
            weatherDescription = "Clear sky",
            isDay = true,
            precipitation = 0.0,
            pressure = 1013.25,
            cloudCover = 10
        ),
        hourly = listOf(
            HourlyForecast(
                time = "2025-01-01T12:00",
                temperature = 20.0,
                weatherCode = 0,
                weatherDescription = "Clear sky",
                precipitationProbability = 0,
                precipitation = 0.0,
                humidity = 60,
                windSpeed = 10.0,
                isDay = true
            )
        ),
        daily = listOf(
            DailyForecast(
                date = "2025-01-01",
                weatherCode = 0,
                weatherDescription = "Clear sky",
                maxTemp = 22.0,
                minTemp = 15.0,
                precipitationSum = 0.0,
                precipitationProbabilityMax = 0,
                windSpeedMax = 12.0,
                uvIndexMax = 5.0,
                sunrise = "06:00",
                sunset = "18:00"
            )
        )
    )

    var lastSearchLanguage: String? = null
        private set

    override suspend fun searchLocations(query: String, languageCode: String): Result<List<LocationSearchResult>> {
        lastSearchLanguage = languageCode
        if (shouldReturnError) {
            return Result.failure(Exception("Search Failed"))
        }
        val match = LocationSearchResult(
            id = 1L,
            name = query,
            country = "Sample Country",
            latitude = 10.0,
            longitude = 10.0
        )
        return Result.success(listOf(match))
    }

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        locationName: String,
        saveToCache: Boolean
    ): Result<Weather> {
        weatherRequests++
        lastSaveToCache = saveToCache
        lastRequestedLatitude = latitude
        if (shouldReturnError) {
            return Result.failure(Exception("Network Error"))
        }
        val name = if (locationName.isNotBlank()) locationName else "Custom Location"
        return Result.success(sampleWeather.copy(locationName = name, latitude = latitude, longitude = longitude))
    }

    override suspend fun getDefaultWeather(): Result<Weather> {
        weatherRequests++
        if (shouldReturnError) {
            return Result.failure(Exception("Network Error"))
        }
        return Result.success(sampleWeather)
    }
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    val state = MutableStateFlow(UserPreferences())

    override val preferences: Flow<UserPreferences> = state

    override suspend fun saveDisplaySettings(
        unit: TemperatureUnit,
        language: AppLanguage,
        themeMode: ThemeMode,
        isHappyMode: Boolean
    ) {
        state.update {
            it.copy(unit = unit, language = language, themeMode = themeMode, isHappyMode = isHappyMode)
        }
    }

    override suspend fun saveLastLocation(location: SavedLocation?) {
        state.update { it.copy(lastLocation = location) }
    }

    override suspend fun saveFavorites(favorites: List<SavedLocation>) {
        state.update { it.copy(favorites = favorites) }
    }
}
