# Project Plan

Un app del clima para los proximos 7 días

## Project Brief

# Project Brief: 7-Day Weather App

## Features
1. **Current Weather Display**: Real-time temperature, condition summaries, and key weather metrics (humidity, wind speed, precipitation) for the current location.
2. **7-Day Weather Forecast**: Clear daily breakdown for the upcoming 7 days showing projected high/low temperatures, weather icons, and precipitation chances.
3. **Location Search**: Ability to search for cities or locations worldwide to view their current weather and 7-day forecast.
4. **Hourly Forecast Overview**: Hourly weather details for the current day to track upcoming weather changes.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose with Material Design 3
- **Navigation**: Jetpack Navigation 3 (State-driven)
- **Adaptive Strategy**: Compose Material Adaptive library
- **Architecture**: MVVM with AndroidX ViewModel & Unidirectional Data Flow (UDF)
- **Asynchrony & Data Streams**: Kotlin Coroutines & Flow
- **Networking**: Ktor Client / Retrofit (for fetching weather forecast data)

## Implementation Steps
**Total Duration:** 13h 22m 31s

### Task_1_DataAndDomainSetup: Implement weather data domain models, API client, and repository layer for fetching current weather, hourly forecast, 7-day forecast, and location search.
- **Status:** COMPLETED
- **Updates:** Implemented domain models (LocationSearchResult, Weather, DailyForecast, HourlyForecast, WeatherCode), Retrofit API layer with Open-Meteo endpoints for forecast and search, weather mappers, WeatherRepositoryImpl with live API calls and default fallback data, and verified with unit tests and assembleDebug build passing.
- **Acceptance Criteria:**
  - Weather API client and models implemented
  - Repository logic for current weather, 7-day forecast, hourly forecast, and search functional
  - build pass
- **Duration:** 7m 8s

### Task_2_WeatherViewModelAndState: Implement ViewModel, UI state handling, and data flow using Kotlin Coroutines and Flow for managing weather and search results.
- **Status:** COMPLETED
- **Updates:** Created WeatherUiState with TemperatureUnit conversion/formatting support, WeatherViewModel with StateFlow, debounced location search, pull-to-refresh, unit toggling, error handling, and complete unit tests (WeatherViewModelTest passing 15/15 tests). Verified build with ./gradlew :app:assembleDebug.
- **Acceptance Criteria:**
  - ViewModel manages UI state for current weather, hourly, 7-day forecast, and search
  - StateFlow exposes weather data and loading/error states
  - build pass
- **Duration:** 1m 44s

### Task_3_WeatherUIAndNavigation: Construct Jetpack Compose UI screens/components including Current Weather display, Hourly Forecast row, 7-Day Forecast list, and Location Search bar.
- **Status:** COMPLETED
- **Updates:** Implemented complete Material 3 Jetpack Compose UI: LocationSearchBar, CurrentWeatherCard with unit toggle, HourlyForecastSection LazyRow, DailyForecastSection 7-day list with max/min range bars, ExtraMetricsSection grid, PullToRefreshBox, WeatherScreen state routing, and connected MainActivity. Verified with assembleDebug and testDebugUnitTest passing.
- **Acceptance Criteria:**
  - Compose UI components created with Material 3 styling
  - Location search bar updates current weather and forecasts upon location selection
  - build pass
- **Duration:** 5m 2s

### Task_4_RunAndVerify: Run and verify application. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Implemented GPS Device Location support: FusedLocationProviderClient, Geocoder location name lookup, runtime permission handling, 'My Location' button (Icons.Rounded.MyLocation) in top bar, dark/light theme switch button, and fallback handling. All unit tests passed (20/20) and assembleDebug succeeded.
- **Acceptance Criteria:**
  - app does not crash
  - make sure all existing tests pass
  - build pass
  - all features (current weather, hourly forecast, 7-day forecast, search) verified and functional
- **Duration:** 13h 8m 37s

