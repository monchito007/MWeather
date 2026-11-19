package com.monchito007.mweather

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
import com.monchito007.mweather.ui.weather.WeatherUtils
import com.monchito007.mweather.ui.weather.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Creates a ViewModel and performs the default-city load the screen would trigger. */
    private fun newViewModel(provider: DeviceLocationProvider? = null): WeatherViewModel =
        WeatherViewModel(fakeRepository, provider).also { it.loadWeather() }

    @Test
    fun testViewModelDoesNotLoadOnConstruction() = runTest {
        val viewModel = WeatherViewModel(fakeRepository)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.weather)
        assertEquals(0, fakeRepository.weatherRequests)
    }

    @Test
    fun testStartInitialLoadOnlyOnce() = runTest {
        val viewModel = WeatherViewModel(fakeRepository)

        assertTrue(viewModel.startInitialLoad())
        assertTrue(viewModel.uiState.value.isLoading)
        // Simulates the screen being recreated (e.g. rotation)
        assertFalse(viewModel.startInitialLoad())
    }

    @Test
    fun testPermissionDeniedLoadsDefaultCityOnce() = runTest {
        val viewModel = WeatherViewModel(fakeRepository)
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
    fun testFailureWithExistingDataKeepsWeatherAndShowsError() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        fakeRepository.shouldReturnError = true
        viewModel.refreshWeather()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Default City", state.weather?.locationName)
        assertNotNull(state.errorMessage)
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
        assertNull(state.errorMessage)
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
        assertNull(state.errorMessage)
    }

    @Test
    fun testLoadWeatherFailure() = runTest {
        fakeRepository.shouldReturnError = true
        val viewModel = newViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.weather)
        assertEquals("No se pudo cargar el clima.", state.errorMessage)
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
    fun testWeatherUtilsLocalization() {
        // Descriptions
        assertEquals("Cielo despejado", WeatherUtils.getWeatherDescription(0, AppLanguage.SPANISH))
        assertEquals("Clear sky", WeatherUtils.getWeatherDescription(0, AppLanguage.ENGLISH))
        assertEquals("Tormenta", WeatherUtils.getWeatherDescription(95, AppLanguage.SPANISH))
        assertEquals("Thunderstorm", WeatherUtils.getWeatherDescription(95, AppLanguage.ENGLISH))

        // Daily date
        assertEquals("Hoy", WeatherUtils.formatDailyDate("2025-01-01", 0, AppLanguage.SPANISH))
        assertEquals("Today", WeatherUtils.formatDailyDate("2025-01-01", 0, AppLanguage.ENGLISH))
        assertEquals("Mañana", WeatherUtils.formatDailyDate("2025-01-02", 1, AppLanguage.SPANISH))
        assertEquals("Tomorrow", WeatherUtils.formatDailyDate("2025-01-02", 1, AppLanguage.ENGLISH))

        // UV Index
        assertEquals("Bajo", WeatherUtils.getUvIndexDescription(2.0, AppLanguage.SPANISH))
        assertEquals("Low", WeatherUtils.getUvIndexDescription(2.0, AppLanguage.ENGLISH))
        assertEquals("Muy Alto", WeatherUtils.getUvIndexDescription(9.0, AppLanguage.SPANISH))
        assertEquals("Very High", WeatherUtils.getUvIndexDescription(9.0, AppLanguage.ENGLISH))
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

        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
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

    override suspend fun searchLocations(query: String): Result<List<LocationSearchResult>> {
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
        locationName: String
    ): Result<Weather> {
        weatherRequests++
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
