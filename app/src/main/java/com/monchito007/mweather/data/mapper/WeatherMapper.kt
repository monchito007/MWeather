package com.monchito007.mweather.data.mapper

import com.monchito007.mweather.data.dto.GeocodingLocationDto
import com.monchito007.mweather.data.dto.OpenMeteoForecastDto
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.domain.model.HourlyForecast
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.domain.model.WeatherCodeInfo

fun GeocodingLocationDto.toDomain(): LocationSearchResult {
    return LocationSearchResult(
        id = id,
        name = name,
        country = country,
        state = admin1,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone
    )
}

fun OpenMeteoForecastDto.toDomain(fallbackLocationName: String = ""): Weather {
    val currentDto = current
    val weatherCode = currentDto?.weatherCode ?: WeatherCodeInfo.UNKNOWN_CODE
    val weatherCodeInfo = WeatherCodeInfo.fromCode(weatherCode)

    val currentWeather = CurrentWeather(
        temperature = currentDto?.temperature2m,
        apparentTemperature = currentDto?.apparentTemperature,
        humidity = currentDto?.relativeHumidity2m?.toInt(),
        windSpeed = currentDto?.windSpeed10m,
        windDirection = currentDto?.windDirection10m?.toInt(),
        weatherCode = weatherCode,
        weatherDescription = weatherCodeInfo.description,
        isDay = (currentDto?.isDay ?: 1) == 1,
        precipitation = currentDto?.precipitation,
        pressure = currentDto?.pressureMsl,
        cloudCover = currentDto?.cloudCover?.toInt(),
        uvIndex = daily?.uvIndexMax?.firstOrNull()
    )

    val hourlyList = mutableListOf<HourlyForecast>()
    hourly?.time?.let { times ->
        val firstHour = firstUpcomingHourIndex(times, currentDto?.time)
        for (i in firstHour until times.size) {
            val code = hourly.weatherCode?.getOrNull(i) ?: WeatherCodeInfo.UNKNOWN_CODE
            val codeInfo = WeatherCodeInfo.fromCode(code)
            val timeStr = times[i]
            hourlyList.add(
                HourlyForecast(
                    time = timeStr,
                    temperature = hourly.temperature2m?.getOrNull(i),
                    weatherCode = code,
                    weatherDescription = codeInfo.description,
                    precipitationProbability = hourly.precipitationProbability?.getOrNull(i)?.toInt(),
                    precipitation = hourly.precipitation?.getOrNull(i),
                    humidity = hourly.relativeHumidity2m?.getOrNull(i)?.toInt(),
                    windSpeed = hourly.windSpeed10m?.getOrNull(i),
                    isDay = (hourly.isDay?.getOrNull(i) ?: 1) == 1
                )
            )
        }
    }

    val dailyList = mutableListOf<DailyForecast>()
    daily?.time?.let { dates ->
        for (i in dates.indices) {
            val code = daily.weatherCode?.getOrNull(i) ?: WeatherCodeInfo.UNKNOWN_CODE
            val codeInfo = WeatherCodeInfo.fromCode(code)
            dailyList.add(
                DailyForecast(
                    date = dates[i],
                    weatherCode = code,
                    weatherDescription = codeInfo.description,
                    maxTemp = daily.temperature2mMax?.getOrNull(i),
                    minTemp = daily.temperature2mMin?.getOrNull(i),
                    precipitationSum = daily.precipitationSum?.getOrNull(i),
                    precipitationProbabilityMax = daily.precipitationProbabilityMax?.getOrNull(i)?.toInt(),
                    windSpeedMax = daily.windSpeed10mMax?.getOrNull(i),
                    uvIndexMax = daily.uvIndexMax?.getOrNull(i),
                    sunrise = daily.sunrise?.getOrNull(i),
                    sunset = daily.sunset?.getOrNull(i)
                )
            )
        }
    }

    return Weather(
        // May be blank (unknown name): the UI shows a localized "Current location".
        locationName = fallbackLocationName,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone ?: "UTC",
        current = currentWeather,
        hourly = hourlyList,
        daily = dailyList
    )
}

/**
 * Index of the hour that contains [currentTime], so the forecast skips hours already past.
 * The API's hourly list always starts at 00:00 of today. Both values are ISO local times in
 * the location's timezone ("2026-11-23T14:15" vs "2026-11-23T14:00"), so comparing the
 * "yyyy-MM-ddTHH" prefix as text is enough. Without a current time, nothing is skipped.
 */
internal fun firstUpcomingHourIndex(times: List<String>, currentTime: String?): Int {
    if (currentTime.isNullOrBlank()) return 0
    val currentHour = currentTime.take(13)
    val index = times.indexOfFirst { it.take(13) >= currentHour }
    return if (index == -1) times.size else index
}
