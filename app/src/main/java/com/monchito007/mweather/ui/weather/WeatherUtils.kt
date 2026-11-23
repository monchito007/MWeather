package com.monchito007.mweather.ui.weather

import androidx.annotation.StringRes
import com.monchito007.mweather.R
import java.util.Calendar

/**
 * Pure helpers: they return resource IDs, never translated text, so the UI resolves them
 * with stringResource() in whatever language is active.
 */
object WeatherUtils {

    /** Shown in place of any value the API didn't provide. */
    const val NO_DATA = "—"

    @StringRes
    fun weatherDescriptionRes(weatherCode: Int): Int = when (weatherCode) {
        0 -> R.string.weather_clear_sky
        1 -> R.string.weather_mainly_clear
        2 -> R.string.weather_partly_cloudy
        3 -> R.string.weather_overcast
        45, 48 -> R.string.weather_fog
        51, 53, 55 -> R.string.weather_drizzle
        56, 57 -> R.string.weather_freezing_drizzle
        61 -> R.string.weather_slight_rain
        63 -> R.string.weather_moderate_rain
        65 -> R.string.weather_heavy_rain
        66, 67 -> R.string.weather_freezing_rain
        71, 73, 75, 77 -> R.string.weather_snow
        80, 81, 82 -> R.string.weather_rain_showers
        85, 86 -> R.string.weather_snow_showers
        95 -> R.string.weather_thunderstorm
        96, 99 -> R.string.weather_thunderstorm_hail
        else -> R.string.weather_unknown
    }

    @StringRes
    fun uvIndexDescriptionRes(uvIndex: Double): Int = when {
        uvIndex < 3.0 -> R.string.uv_low
        uvIndex < 6.0 -> R.string.uv_moderate
        uvIndex < 8.0 -> R.string.uv_high
        uvIndex < 11.0 -> R.string.uv_very_high
        else -> R.string.uv_extreme
    }

    /**
     * Compass point the wind comes from, as an index into R.array.compass_points_short
     * (0 = N, 1 = NE … 7 = NW). Each point covers 45°, centred on it (N = 338°–22°).
     */
    fun compassPointIndex(degrees: Int): Int {
        val normalized = ((degrees % 360) + 360) % 360
        return ((normalized + 22) / 45) % 8
    }

    fun formatHourlyTime(timeStr: String): String =
        if (timeStr.contains("T")) timeStr.substringAfter("T").take(5) else timeStr

    /**
     * Day of week of an ISO date ("2026-11-19") as an index into R.array.days_of_week_short
     * (0 = Sunday … 6 = Saturday), or null if the date can't be parsed.
     * Uses Calendar instead of java.time so it works on minSdk 24 without desugaring.
     */
    fun dayOfWeekIndex(dateStr: String): Int? {
        val parts = dateStr.take(10).split("-").mapNotNull { it.toIntOrNull() }
        if (parts.size != 3) return null
        val (year, month, day) = parts
        val calendar = Calendar.getInstance().apply {
            clear()
            isLenient = false
            set(year, month - 1, day)
        }
        return try {
            calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

/** Formats a value, or returns [WeatherUtils.NO_DATA] when it's missing. */
inline fun <T : Any> T?.orNoData(format: (T) -> String): String =
    if (this == null) WeatherUtils.NO_DATA else format(this)
