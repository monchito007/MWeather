package com.monchito007.mweather.ui.weather

import androidx.annotation.StringRes
import com.monchito007.mweather.R
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.domain.model.PollenType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

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

    /** Day of month of an ISO date or date-time ("2026-11-24T05:00" -> 24), or null if invalid. */
    fun dayOfMonth(dateStr: String): Int? =
        dateStr.take(10).split("-").getOrNull(2)?.toIntOrNull()?.takeIf { it in 1..31 }

    /**
     * Compass point the wind comes from, as an index into R.array.compass_points_short
     * (0 = N, 1 = NE … 7 = NW). Each point covers 45°, centred on it (N = 338°–22°).
     */
    fun compassPointIndex(degrees: Int): Int {
        val normalized = ((degrees % 360) + 360) % 360
        return ((normalized + 22) / 45) % 8
    }

    /**
     * How long ago a forecast was downloaded, as a string resource and the number to put in it:
     * "just now" under a minute, then minutes, hours and finally days.
     */
    /**
     * Expected rain in millimetres with one decimal in the [locale]'s style ("1.5" / "1,5"),
     * or null when it would round to 0.0, so dry hours and days show nothing.
     */
    fun formatPrecipitationMm(mm: Double?, locale: Locale): String? =
        if (mm == null || mm < 0.05) null else String.format(locale, "%.1f", mm)

    fun updatedAgo(fetchedAtMillis: Long, nowMillis: Long): Pair<Int, Long> {
        val minutes = ((nowMillis - fetchedAtMillis) / 60_000L).coerceAtLeast(0L)
        return when {
            minutes < 1 -> R.string.updated_just_now to 0L
            minutes < 60 -> R.string.updated_minutes_ago to minutes
            minutes < 24 * 60 -> R.string.updated_hours_ago to minutes / 60
            else -> R.string.updated_days_ago to minutes / (24 * 60)
        }
    }

    /**
     * Minutes of daylight between [sunrise] and [sunset] ("2026-11-24T07:01"), or null when
     * either is missing, malformed or on different dates (e.g. polar day/night).
     */
    fun daylightMinutes(sunrise: String?, sunset: String?): Int? {
        if (sunrise == null || sunset == null) return null
        if (sunrise.substringBefore("T") != sunset.substringBefore("T")) return null
        val start = minuteOfDay(sunrise) ?: return null
        val end = minuteOfDay(sunset) ?: return null
        return (end - start).takeIf { it >= 0 }
    }

    /**
     * Signed change in daylight from [today] to [tomorrow] ("+1", "−2", "±0"), or null when
     * either day's daylight is unknown.
     */
    fun daylightChange(today: DailyForecast?, tomorrow: DailyForecast?): String? {
        val now = daylightMinutes(today?.sunrise, today?.sunset) ?: return null
        val next = daylightMinutes(tomorrow?.sunrise, tomorrow?.sunset) ?: return null
        val diff = next - now
        return when {
            diff > 0 -> "+$diff"
            diff < 0 -> "−${-diff}"
            else -> "±0"
        }
    }

    private fun minuteOfDay(time: String): Int? {
        val parts = time.substringAfter("T", "").split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minutes = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return if (hours in 0..23 && minutes in 0..59) hours * 60 + minutes else null
    }

    /** "24 km" from 1 km up, "800 m" below. */
    fun formatVisibility(metres: Double): String =
        if (metres >= 1000) "${(metres / 1000).roundToInt()} km" else "${metres.roundToInt()} m"

    @StringRes
    fun visibilityLevelRes(metres: Double): Int = when {
        metres < 1000 -> R.string.visibility_very_low
        metres < 5000 -> R.string.visibility_reduced
        else -> R.string.visibility_good
    }

    /** Seconds as whole hours and remaining minutes, e.g. 16200.0 -> (4, 30). */
    fun hoursAndMinutes(seconds: Double): Pair<Int, Int> {
        val minutes = (seconds / 60).roundToInt().coerceAtLeast(0)
        return minutes / 60 to minutes % 60
    }

    /** Local date and time at [timezone] ("2026-11-24T16:05"), the same format the API uses. */
    fun localDateTime(nowMillis: Long, timezone: String): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone(timezone)
        }.format(Date(nowMillis))

    /** The next sunrise or sunset after [nowLocal], and how many minutes away it is. */
    data class SunEvent(val isSunrise: Boolean, val minutesUntil: Int)

    /**
     * Finds the next sunrise or sunset in [days] after [nowLocal] (both in the location's local
     * time), or null when there is none, e.g. during polar day/night or with no data.
     */
    fun nextSunEvent(nowLocal: String, days: List<DailyForecast>): SunEvent? {
        val now = epochMinutes(nowLocal) ?: return null
        for (day in days) {
            for ((time, isSunrise) in listOf(day.sunrise to true, day.sunset to false)) {
                val at = time?.let { epochMinutes(it) } ?: continue
                if (at > now) return SunEvent(isSunrise, (at - now).toInt())
            }
        }
        return null
    }

    /** Minutes since the epoch of a local "yyyy-MM-ddTHH:mm", read as if it were UTC. */
    private fun epochMinutes(localTime: String): Long? = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }.parse(localTime)?.time?.div(60_000L)
    } catch (_: java.text.ParseException) {
        null
    }

    /** European AQI bands: 0–20 good … 100+ extremely poor. */
    @StringRes
    fun airQualityLevelRes(europeanAqi: Int): Int = when {
        europeanAqi < 20 -> R.string.aqi_good
        europeanAqi < 40 -> R.string.aqi_fair
        europeanAqi < 60 -> R.string.aqi_moderate
        europeanAqi < 80 -> R.string.aqi_poor
        europeanAqi < 100 -> R.string.aqi_very_poor
        else -> R.string.aqi_extremely_poor
    }

    @StringRes
    fun pollenNameRes(type: PollenType): Int = when (type) {
        PollenType.ALDER -> R.string.pollen_alder
        PollenType.BIRCH -> R.string.pollen_birch
        PollenType.GRASS -> R.string.pollen_grass
        PollenType.MUGWORT -> R.string.pollen_mugwort
        PollenType.OLIVE -> R.string.pollen_olive
        PollenType.RAGWEED -> R.string.pollen_ragweed
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
