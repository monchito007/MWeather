package com.monchito007.mweather.ui.weather

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

object WeatherUtils {

    fun getWeatherIcon(weatherCode: Int, isDay: Boolean = true): ImageVector {
        return when (weatherCode) {
            0 -> if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay
            1, 2 -> if (isDay) Icons.Rounded.WbCloudy else Icons.Rounded.NightsStay
            3 -> Icons.Rounded.Cloud
            45, 48 -> Icons.Rounded.Grain
            51, 53, 55, 56, 57 -> Icons.Rounded.Grain
            61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Rounded.WaterDrop
            71, 73, 75, 77, 85, 86 -> Icons.Rounded.AcUnit
            95, 96, 99 -> Icons.Rounded.Thunderstorm
            else -> if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay
        }
    }

    fun getWeatherDescription(weatherCode: Int, language: AppLanguage = AppLanguage.SPANISH): String {
        return when (language) {
            AppLanguage.SPANISH -> when (weatherCode) {
                0 -> "Cielo despejado"
                1 -> "Principalmente despejado"
                2 -> "Parcialmente nublado"
                3 -> "Nublado"
                45, 48 -> "Niebla"
                51, 53, 55 -> "Llovizna"
                56, 57 -> "Llovizna helada"
                61 -> "Lluvia ligera"
                63 -> "Lluvia moderada"
                65 -> "Lluvia fuerte"
                66, 67 -> "Lluvia helada"
                71, 73, 75, 77 -> "Nieve"
                80, 81, 82 -> "Chubascos"
                85, 86 -> "Chubascos de nieve"
                95 -> "Tormenta"
                96, 99 -> "Tormenta con granizo"
                else -> "Desconocido"
            }
            AppLanguage.ENGLISH -> when (weatherCode) {
                0 -> "Clear sky"
                1 -> "Mainly clear"
                2 -> "Partly cloudy"
                3 -> "Overcast"
                45, 48 -> "Foggy"
                51, 53, 55 -> "Drizzle"
                56, 57 -> "Freezing drizzle"
                61 -> "Slight rain"
                63 -> "Moderate rain"
                65 -> "Heavy rain"
                66, 67 -> "Freezing rain"
                71, 73, 75, 77 -> "Snow"
                80, 81, 82 -> "Rain showers"
                85, 86 -> "Snow showers"
                95 -> "Thunderstorm"
                96, 99 -> "Thunderstorm with hail"
                else -> "Unknown"
            }
        }
    }

    fun getWeatherDescriptionSpanishEnglish(weatherCode: Int): String {
        return getWeatherDescription(weatherCode, AppLanguage.SPANISH)
    }

    fun formatHourlyTime(timeStr: String): String {
        return try {
            if (timeStr.contains("T")) {
                val timePart = timeStr.substringAfter("T")
                timePart.take(5) // e.g. "12:00"
            } else {
                timeStr
            }
        } catch (e: Exception) {
            timeStr
        }
    }

    fun formatDailyDate(dateStr: String, index: Int, language: AppLanguage = AppLanguage.SPANISH): String {
        if (index == 0) return if (language == AppLanguage.SPANISH) "Hoy" else "Today"
        if (index == 1) return if (language == AppLanguage.SPANISH) "Mañana" else "Tomorrow"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val parsed = LocalDate.parse(dateStr)
                if (language == AppLanguage.SPANISH) {
                    when (parsed.dayOfWeek) {
                        DayOfWeek.MONDAY -> "Lun"
                        DayOfWeek.TUESDAY -> "Mar"
                        DayOfWeek.WEDNESDAY -> "Mié"
                        DayOfWeek.THURSDAY -> "Jue"
                        DayOfWeek.FRIDAY -> "Vie"
                        DayOfWeek.SATURDAY -> "Sáb"
                        DayOfWeek.SUNDAY -> "Dom"
                        else -> dateStr
                    }
                } else {
                    when (parsed.dayOfWeek) {
                        DayOfWeek.MONDAY -> "Mon"
                        DayOfWeek.TUESDAY -> "Tue"
                        DayOfWeek.WEDNESDAY -> "Wed"
                        DayOfWeek.THURSDAY -> "Thu"
                        DayOfWeek.FRIDAY -> "Fri"
                        DayOfWeek.SATURDAY -> "Sat"
                        DayOfWeek.SUNDAY -> "Sun"
                        else -> dateStr
                    }
                }
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(dateStr)
                val locale = if (language == AppLanguage.SPANISH) Locale.forLanguageTag("es") else Locale.ENGLISH
                val dayFormat = SimpleDateFormat("EEE", locale)
                date?.let { dayFormat.format(it).replaceFirstChar { char -> char.uppercase() } } ?: dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getUvIndexDescription(uvIndex: Double, language: AppLanguage = AppLanguage.SPANISH): String {
        return if (language == AppLanguage.SPANISH) {
            when {
                uvIndex < 3.0 -> "Bajo"
                uvIndex < 6.0 -> "Moderado"
                uvIndex < 8.0 -> "Alto"
                uvIndex < 11.0 -> "Muy Alto"
                else -> "Extremo"
            }
        } else {
            when {
                uvIndex < 3.0 -> "Low"
                uvIndex < 6.0 -> "Moderate"
                uvIndex < 8.0 -> "High"
                uvIndex < 11.0 -> "Very High"
                else -> "Extreme"
            }
        }
    }
}
