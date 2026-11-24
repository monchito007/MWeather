package com.monchito007.mweather.ui.weather.components

import androidx.compose.ui.res.stringArrayResource
import com.monchito007.mweather.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.WeatherUtils

@Composable
fun DailyForecastSection(
    dailyForecast: List<DailyForecast>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier,
    isHappyMode: Boolean = false
) {
    if (dailyForecast.isEmpty()) return

    // Null when no day has data: the range bars then show only their background track.
    val overallMin = dailyForecast.mapNotNull { it.minTemp }.minOrNull()
    val overallMax = dailyForecast.mapNotNull { it.maxTemp }.maxOrNull()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.daily_forecast_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Card wrapping 7-day items
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                dailyForecast.forEachIndexed { index, day ->
                    DailyForecastRowItem(
                        day = day,
                        index = index,
                        unit = unit,
                        overallMin = overallMin,
                        overallMax = overallMax,
                        isHappyMode = isHappyMode
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRowItem(
    day: DailyForecast,
    index: Int,
    unit: TemperatureUnit,
    overallMin: Double?,
    overallMax: Double?,
    isHappyMode: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day Name
        Text(
            text = dayLabel(day.date, index),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(90.dp)
        )

        // Weather Icon + Precip Chance and expected mm
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(84.dp)
        ) {
            ColorfulWeatherIcon(
                weatherCode = day.weatherCode,
                isDay = true,
                isHappyMode = isHappyMode,
                modifier = Modifier.size(32.dp)
            )
            val rainChance = day.precipitationProbabilityMax?.takeIf { it > 0 }
            val locale = LocalConfiguration.current.locales[0]
            val hasAmount = WeatherUtils.formatPrecipitationMm(day.precipitationSum, locale) != null ||
                WeatherUtils.formatPrecipitationMm(day.snowfallSum, locale) != null
            if (rainChance != null || hasAmount) {
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    if (rainChance != null) {
                        Text(
                            text = "$rainChance%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    PrecipitationAmount(rainMm = day.precipitationSum, snowCm = day.snowfallSum)
                }
            }
        }

        // Min Temp
        Text(
            text = unit.formatTemperature(day.minTemp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Temperature Range Bar
        TemperatureRangeBar(
            minTemp = day.minTemp,
            maxTemp = day.maxTemp,
            overallMin = overallMin,
            overallMax = overallMax,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Max Temp
        Text(
            text = unit.formatTemperature(day.maxTemp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
private fun TemperatureRangeBar(
    minTemp: Double?,
    maxTemp: Double?,
    overallMin: Double?,
    overallMax: Double?,
    modifier: Modifier = Modifier
) {
    if (minTemp == null || maxTemp == null || overallMin == null || overallMax == null) {
        // Missing data: draw only the empty track instead of inventing a range.
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        return
    }

    val tempRange = (overallMax - overallMin).coerceAtLeast(1.0)
    val startFraction = ((minTemp - overallMin) / tempRange).coerceIn(0.0, 1.0).toFloat()
    val endFraction = ((maxTemp - overallMin) / tempRange).coerceIn(0.0, 1.0).toFloat()
    val activeWidthFraction = (endFraction - startFraction).coerceAtLeast(0.05f)

    val activeBarBrush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (startFraction > 0f) {
                Spacer(modifier = Modifier.weight(startFraction))
            }
            Box(
                modifier = Modifier
                    .weight(activeWidthFraction)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(brush = activeBarBrush)
            )
            val remainingFraction = 1f - endFraction
            if (remainingFraction > 0f) {
                Spacer(modifier = Modifier.weight(remainingFraction))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DailyForecastSectionPreview() {
    val sampleDaily = List(7) { i ->
        DailyForecast(
            date = "2025-01-0${i + 1}",
            weatherCode = if (i % 2 == 0) 0 else 61,
            weatherDescription = "Sunny",
            maxTemp = 24.0 + i,
            minTemp = 14.0 + i,
            precipitationSum = if (i % 2 == 0) 0.0 else 4.2,
            precipitationProbabilityMax = if (i % 2 == 0) 0 else 30,
            windSpeedMax = 15.0,
            uvIndexMax = 5.0,
            sunrise = "06:00",
            sunset = "18:00"
        )
    }

    MWeatherTheme {
        DailyForecastSection(
            dailyForecast = sampleDaily,
            unit = TemperatureUnit.CELSIUS,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/** "Today", "Tomorrow", then the short weekday name, in the active app language. */
@Composable
private fun dayLabel(date: String, index: Int): String = when (index) {
    0 -> stringResource(R.string.today)
    1 -> stringResource(R.string.tomorrow)
    else -> WeatherUtils.dayOfWeekIndex(date)
        ?.let { stringArrayResource(R.array.days_of_week_short)[it] }
        ?: date
}
