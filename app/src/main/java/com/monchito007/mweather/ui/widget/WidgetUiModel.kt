package com.monchito007.mweather.ui.widget

import android.content.Context
import com.monchito007.mweather.R
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.WeatherUtils

/** Everything the widget shows, already formatted, so the Glance layout only draws it. */
data class WidgetUiModel(
    val locationName: String,
    val temperature: String,
    val emoji: String,
    val description: String,
    val highLow: String,
    val updatedAgo: String,
    val hours: List<WidgetHour>
)

data class WidgetHour(val time: String, val emoji: String, val temperature: String)

/** Number of upcoming hours shown in the wide widget. */
private const val WIDGET_HOURS = 5

/** [context] must already be localized to the in-app language (see [WeatherWidget]). */
fun Weather.toWidgetUiModel(context: Context, unit: TemperatureUnit, nowMillis: Long): WidgetUiModel {
    val today = daily.firstOrNull()
    val updatedAgo = fetchedAtMillis?.let {
        val (textRes, amount) = WeatherUtils.updatedAgo(it, nowMillis)
        context.getString(textRes, amount)
    }.orEmpty()
    return WidgetUiModel(
        locationName = locationName.ifBlank { context.getString(R.string.current_location) },
        temperature = unit.formatTemperature(current.temperature),
        emoji = weatherEmoji(current.weatherCode, current.isDay),
        description = context.getString(WeatherUtils.weatherDescriptionRes(current.weatherCode)),
        highLow = "${unit.formatTemperature(today?.maxTemp)} / ${unit.formatTemperature(today?.minTemp)}",
        updatedAgo = updatedAgo,
        hours = hourly.take(WIDGET_HOURS).map { hour ->
            WidgetHour(
                time = WeatherUtils.formatHourlyTime(hour.time),
                emoji = weatherEmoji(hour.weatherCode, hour.isDay),
                temperature = unit.formatTemperature(hour.temperature)
            )
        }
    )
}

/**
 * Emoji for a WMO weather code. Glance widgets can't draw the app's Compose Canvas icons;
 * emoji are colourful, need no image assets and follow the system emoji font.
 */
fun weatherEmoji(weatherCode: Int, isDay: Boolean): String = when (weatherCode) {
    0 -> if (isDay) "☀️" else "🌙"
    1 -> if (isDay) "🌤️" else "🌙"
    2 -> if (isDay) "⛅" else "☁️"
    3 -> "☁️"
    45, 48 -> "🌫️"
    51, 53, 55, 56, 57 -> "🌦️"
    61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️"
    71, 73, 75, 77, 85, 86 -> "❄️"
    95, 96, 99 -> "⛈️"
    else -> "🌡️"
}
