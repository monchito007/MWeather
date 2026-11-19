package com.monchito007.mweather

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.domain.model.HourlyForecast
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.AppLanguage
import com.monchito007.mweather.ui.weather.ProvideAppLanguage
import com.monchito007.mweather.ui.weather.ThemeMode
import com.monchito007.mweather.ui.weather.WeatherScreenContent
import com.monchito007.mweather.ui.weather.WeatherUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the stateless screen content: each test renders a fixed [WeatherUiState]
 * and checks what the user sees and which callbacks fire. No network or GPS involved.
 */
@RunWith(AndroidJUnit4::class)
class WeatherScreenContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<String>()
    private var selectedLocation: LocationSearchResult? = null
    private var selectedTheme: ThemeMode? = null
    private var typedQuery: String? = null

    private fun setContent(state: WeatherUiState) {
        composeRule.setContent {
            MWeatherTheme {
                ProvideAppLanguage(state.language) {
                    WeatherScreenContent(
                        uiState = state,
                        onSearchQueryChanged = { typedQuery = it },
                        onSearchActiveChanged = { calls += "searchActive:$it" },
                        onSelectLocation = { selectedLocation = it },
                        onToggleUnit = { calls += "toggleUnit" },
                        onSelectLanguage = { calls += "language:$it" },
                        onSelectThemeMode = { selectedTheme = it },
                        onToggleHappyMode = { calls += "happyMode" },
                        onRequestDeviceLocation = { calls += "deviceLocation" },
                        onRefresh = { calls += "refresh" },
                        onClearError = { calls += "clearError" }
                    )
                }
            }
        }
    }

    // ---------- states ----------

    @Test
    fun loadingState_showsLoadingMessage() {
        setContent(WeatherUiState(isLoading = true))

        composeRule.onNodeWithText("Cargando clima…").assertExists()
    }

    @Test
    fun weatherLoaded_showsCurrentConditionsAndForecastSections() {
        setContent(WeatherUiState(weather = sampleWeather()))

        composeRule.onNodeWithText("Madrid").assertExists()
        composeRule.onNodeWithText("14°").assertExists()
        composeRule.onNodeWithText("Máx: 18° / Mín: 7°").assertExists()
        composeRule.onNodeWithText("Sensación: 12°").assertExists()
        composeRule.onNodeWithText("Parcialmente nublado").assertExists()
        composeRule.onNodeWithText("Pronóstico por horas").assertExists()

        scrollTo("Pronóstico de 7 días")
        composeRule.onNodeWithText("Hoy").assertExists()
        composeRule.onNodeWithText("Mañana").assertExists()
    }

    @Test
    fun englishLanguage_showsEnglishTexts() {
        setContent(WeatherUiState(weather = sampleWeather(), language = AppLanguage.ENGLISH))

        composeRule.onNodeWithText("High: 18° / Low: 7°").assertExists()
        composeRule.onNodeWithText("Feels like: 12°").assertExists()
        composeRule.onNodeWithText("Partly cloudy").assertExists()
        composeRule.onNodeWithText("Hourly forecast").assertExists()

        scrollTo("7-Day forecast")
        composeRule.onNodeWithText("Today").assertExists()
    }

    @Test
    fun missingValues_areShownAsDash_notAsZero() {
        val weather = sampleWeather().let {
            it.copy(current = it.current.copy(humidity = null, windSpeed = null, pressure = null))
        }
        setContent(WeatherUiState(weather = weather))

        assertTrue(composeRule.onAllNodesWithText("—").fetchSemanticsNodes().isNotEmpty())
        composeRule.onNodeWithText("0%").assertDoesNotExist()
        composeRule.onNodeWithText("0 km/h").assertDoesNotExist()
    }

    @Test
    fun missingLocationName_showsLocalizedCurrentLocation() {
        setContent(WeatherUiState(weather = sampleWeather().copy(locationName = "")))

        composeRule.onNodeWithText("Ubicación actual").assertExists()
    }

    @Test
    fun errorWithoutData_showsMessageAndRetryCallsRefresh() {
        setContent(WeatherUiState(errorMessageRes = R.string.error_no_connection))

        composeRule.onAllNodesWithText("Sin conexión. Revisa tu red e inténtalo de nuevo.")
            .onFirst().assertExists()
        composeRule.onAllNodesWithText("Reintentar").onFirst().performClick()
        composeRule.waitForIdle()

        assertTrue("refresh" in calls)
    }

    @Test
    fun errorWithData_keepsWeatherVisible() {
        setContent(
            WeatherUiState(weather = sampleWeather(), errorMessageRes = R.string.error_load_weather)
        )

        composeRule.onNodeWithText("Madrid").assertExists()
        composeRule.onAllNodesWithText("No se pudo cargar el clima.").onFirst().assertExists()
    }

    // ---------- search ----------

    @Test
    fun typingInSearchField_reportsQuery() {
        setContent(WeatherUiState(weather = sampleWeather()))

        composeRule.onNode(hasSetTextAction()).performTextInput("Lon")

        assertEquals("Lon", typedQuery)
    }

    @Test
    fun searchResults_areListedAndSelectable() {
        val london = LocationSearchResult(
            id = 1, name = "Londres", country = "Reino Unido", state = "Inglaterra",
            latitude = 51.5, longitude = -0.12
        )
        setContent(
            WeatherUiState(
                weather = sampleWeather(),
                isSearchActive = true,
                searchQuery = "Lon",
                searchResults = listOf(london)
            )
        )

        composeRule.onNodeWithText("Inglaterra, Reino Unido").assertExists()
        composeRule.onNodeWithText("Londres").performClick()

        assertEquals(london, selectedLocation)
    }

    @Test
    fun searchWithoutResults_showsEmptyMessage() {
        setContent(
            WeatherUiState(weather = sampleWeather(), isSearchActive = true, searchQuery = "zzzz")
        )

        composeRule.onNodeWithText("No se encontraron ubicaciones").assertExists()
    }

    // ---------- toolbar ----------

    @Test
    fun myLocationButton_requestsDeviceLocation() {
        setContent(WeatherUiState(weather = sampleWeather()))

        composeRule.onNodeWithContentDescription("Ubicación actual").performClick()

        assertTrue("deviceLocation" in calls)
    }

    @Test
    fun settingsMenu_selectsThemeAndLanguage() {
        setContent(WeatherUiState(weather = sampleWeather()))

        composeRule.onNodeWithContentDescription("Ajustes").performClick()
        composeRule.onNodeWithText("Modo oscuro", substring = true).performClick()
        assertEquals(ThemeMode.DARK, selectedTheme)

        composeRule.onNodeWithContentDescription("Ajustes").performClick()
        composeRule.onNodeWithText("English", substring = true).performClick()
        assertTrue("language:ENGLISH" in calls)
    }

    // ---------- helpers ----------

    private fun scrollTo(text: String) {
        composeRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text))
        composeRule.onNodeWithText(text).assertExists()
    }

    private fun sampleWeather() = Weather(
        locationName = "Madrid",
        latitude = 40.42,
        longitude = -3.7,
        timezone = "Europe/Madrid",
        current = CurrentWeather(
            temperature = 14.0,
            apparentTemperature = 12.0,
            humidity = 61,
            windSpeed = 9.7,
            windDirection = 250,
            weatherCode = 2,
            weatherDescription = "Partly cloudy",
            isDay = true,
            precipitation = 0.0,
            pressure = 1019.4,
            cloudCover = 40,
            uvIndex = 2.4
        ),
        hourly = List(6) { hour ->
            HourlyForecast(
                time = "2026-11-19T1$hour:00",
                temperature = 9.0 + hour,
                weatherCode = 1,
                weatherDescription = "Mainly clear",
                precipitationProbability = 0,
                precipitation = 0.0,
                humidity = 60,
                windSpeed = 8.0,
                isDay = true
            )
        },
        daily = List(7) { day ->
            DailyForecast(
                date = "2026-11-${19 + day}",
                weatherCode = 3,
                weatherDescription = "Overcast",
                maxTemp = 18.0 - day * 0.5,
                minTemp = 7.0,
                precipitationSum = 0.0,
                precipitationProbabilityMax = 10,
                windSpeedMax = 15.0,
                uvIndexMax = 2.0,
                sunrise = "2026-11-19T08:05",
                sunset = "2026-11-19T17:52"
            )
        }
    )
}
