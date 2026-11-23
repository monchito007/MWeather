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
    val days: List<WidgetDay>
)

data class WidgetDay(val label: String, val emoji: String, val maxTemp: String, val minTemp: String)

/** Number of days after today shown in the wide widget. */
private const val WIDGET_DAYS = 5

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
        // The first daily entry is today, already summarised by the high/low above.
        days = daily.drop(1).take(WIDGET_DAYS).map { day ->
            WidgetDay(
                label = WeatherUtils.dayOfWeekIndex(day.date)
                    ?.let { context.resources.getStringArray(R.array.days_of_week_short)[it] }
                    ?: day.date,
                emoji = weatherEmoji(day.weatherCode, isDay = true),
                maxTemp = unit.formatTemperature(day.maxTemp),
                minTemp = unit.formatTemperature(day.minTemp)
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
