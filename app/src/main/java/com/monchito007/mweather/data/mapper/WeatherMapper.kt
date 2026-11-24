package com.monchito007.mweather.data.mapper

import com.monchito007.mweather.data.dto.GeocodingLocationDto
import com.monchito007.mweather.data.dto.OpenMeteoForecastDto
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.domain.model.HourlyForecast
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.domain.model.WeatherCodeInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

/**
 * [localNow] is "now" as an ISO local time in the forecast's timezone ("2026-11-23T14:15"):
 * hours and days before it are dropped. It defaults to the API's current time, which is right
 * for a fresh download; for a cached forecast pass the real current time instead (see [localTimeAt]).
 */
fun OpenMeteoForecastDto.toDomain(
    fallbackLocationName: String = "",
    fetchedAtMillis: Long? = null,
    localNow: String? = current?.time,
): Weather {
    val currentDto = current
    val weatherCode = currentDto?.weatherCode ?: WeatherCodeInfo.UNKNOWN_CODE
    val weatherCodeInfo = WeatherCodeInfo.fromCode(weatherCode)

    val dailyList = mutableListOf<DailyForecast>()
    daily?.time?.let { dates ->
        val firstDay = firstIndexAtOrAfter(dates, localNow, DATE_LENGTH)
        for (i in firstDay until dates.size) {
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

    val currentWeather = CurrentWeather(
        temperature = currentDto?.temperature2m,
        apparentTemperature = currentDto?.apparentTemperature,
        humidity = currentDto?.relativeHumidity2m?.toInt(),
        windSpeed = currentDto?.windSpeed10m,
        windDirection = currentDto?.windDirection10m?.toInt(),
        windGusts = currentDto?.windGusts10m,
        weatherCode = weatherCode,
        weatherDescription = weatherCodeInfo.description,
        isDay = (currentDto?.isDay ?: 1) == 1,
        precipitation = currentDto?.precipitation,
        pressure = currentDto?.pressureMsl,
        cloudCover = currentDto?.cloudCover?.toInt(),
        uvIndex = currentDto?.uvIndex
    )

    val hourlyList = mutableListOf<HourlyForecast>()
    hourly?.time?.let { times ->
        val firstHour = firstUpcomingHourIndex(times, localNow)
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

    return Weather(
        // May be blank (unknown name): the UI shows a localized "Current location".
        locationName = fallbackLocationName,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone ?: "UTC",
        current = currentWeather,
        hourly = hourlyList,
        daily = dailyList,
        fetchedAtMillis = fetchedAtMillis
    )
}

/**
 * Index of the hour that contains [currentTime], so the forecast skips hours already past.
 * The API's hourly list always starts at 00:00 of today. Both values are ISO local times in
 * the location's timezone ("2026-11-23T14:15" vs "2026-11-23T14:00"), so comparing the
 * "yyyy-MM-ddTHH" prefix as text is enough. Without a current time, nothing is skipped.
 */
internal fun firstUpcomingHourIndex(times: List<String>, currentTime: String?): Int =
    firstIndexAtOrAfter(times, currentTime, HOUR_LENGTH)

/** First index whose first [length] characters are >= those of [now]; 0 when [now] is unknown. */
private fun firstIndexAtOrAfter(values: List<String>, now: String?, length: Int): Int {
    if (now.isNullOrBlank()) return 0
    val nowPrefix = now.take(length)
    val index = values.indexOfFirst { it.take(length) >= nowPrefix }
    return if (index == -1) values.size else index
}

private const val DATE_LENGTH = 10 // "yyyy-MM-dd"
private const val HOUR_LENGTH = 13 // "yyyy-MM-ddTHH"

/**
 * [epochMillis] as an ISO local time ("2026-11-23T14:15") at a place [utcOffsetSeconds] from UTC.
 * Falls back to the [timezone] ID when the offset is missing. Uses SimpleDateFormat (not
 * java.time) so it works on minSdk 24 without desugaring.
 */
internal fun localTimeAt(epochMillis: Long, utcOffsetSeconds: Int?, timezone: String? = null): String {
    val offsetMillis = utcOffsetSeconds?.let { it * 1000L }
        ?: TimeZone.getTimeZone(timezone ?: "UTC").getOffset(epochMillis).toLong()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return format.format(Date(epochMillis + offsetMillis))
}
