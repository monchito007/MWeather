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
    val weatherCode = currentDto?.weatherCode ?: 0
    val weatherCodeInfo = WeatherCodeInfo.fromCode(weatherCode)

    val currentWeather = CurrentWeather(
        temperature = currentDto?.temperature2m ?: 0.0,
        apparentTemperature = currentDto?.apparentTemperature ?: 0.0,
        humidity = currentDto?.relativeHumidity2m?.toInt() ?: 0,
        windSpeed = currentDto?.windSpeed10m ?: 0.0,
        windDirection = currentDto?.windDirection10m?.toInt() ?: 0,
        weatherCode = weatherCode,
        weatherDescription = weatherCodeInfo.description,
        isDay = (currentDto?.isDay ?: 1) == 1,
        precipitation = currentDto?.precipitation ?: 0.0,
        pressure = currentDto?.pressureMsl ?: 1013.25,
        cloudCover = currentDto?.cloudCover?.toInt() ?: 0,
        uvIndex = daily?.uvIndexMax?.firstOrNull() ?: 0.0
    )

    val hourlyList = mutableListOf<HourlyForecast>()
    hourly?.time?.let { times ->
        for (i in times.indices) {
            val code = hourly.weatherCode?.getOrNull(i) ?: 0
            val codeInfo = WeatherCodeInfo.fromCode(code)
            val timeStr = times[i]
            hourlyList.add(
                HourlyForecast(
                    time = timeStr,
                    temperature = hourly.temperature2m?.getOrNull(i) ?: 0.0,
                    weatherCode = code,
                    weatherDescription = codeInfo.description,
                    precipitationProbability = hourly.precipitationProbability?.getOrNull(i)?.toInt() ?: 0,
                    precipitation = hourly.precipitation?.getOrNull(i) ?: 0.0,
                    humidity = hourly.relativeHumidity2m?.getOrNull(i)?.toInt() ?: 0,
                    windSpeed = hourly.windSpeed10m?.getOrNull(i) ?: 0.0,
                    isDay = (hourly.isDay?.getOrNull(i) ?: 1) == 1
                )
            )
        }
    }

    val dailyList = mutableListOf<DailyForecast>()
    daily?.time?.let { dates ->
        for (i in dates.indices) {
            val code = daily.weatherCode?.getOrNull(i) ?: 0
            val codeInfo = WeatherCodeInfo.fromCode(code)
            dailyList.add(
                DailyForecast(
                    date = dates[i],
                    weatherCode = code,
                    weatherDescription = codeInfo.description,
                    maxTemp = daily.temperature2mMax?.getOrNull(i) ?: 0.0,
                    minTemp = daily.temperature2mMin?.getOrNull(i) ?: 0.0,
                    precipitationSum = daily.precipitationSum?.getOrNull(i) ?: 0.0,
                    precipitationProbabilityMax = daily.precipitationProbabilityMax?.getOrNull(i)?.toInt() ?: 0,
                    windSpeedMax = daily.windSpeed10mMax?.getOrNull(i) ?: 0.0,
                    uvIndexMax = daily.uvIndexMax?.getOrNull(i) ?: 0.0,
                    sunrise = daily.sunrise?.getOrNull(i) ?: "",
                    sunset = daily.sunset?.getOrNull(i) ?: ""
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
