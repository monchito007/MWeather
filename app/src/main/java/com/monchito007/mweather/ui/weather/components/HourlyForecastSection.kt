package com.monchito007.mweather.ui.weather.components

import com.monchito007.mweather.R
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monchito007.mweather.domain.model.HourlyForecast
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.WeatherUtils

@Composable
fun HourlyForecastSection(
    hourlyForecast: List<HourlyForecast>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier,
    isHappyMode: Boolean = false
) {
    if (hourlyForecast.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.hourly_forecast_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.hourly_forecast_count, hourlyForecast.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hourly LazyRow: every hour the API gives from now on (up to the end of the 7 days)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(hourlyForecast, key = { it.time }) { hour ->
                HourlyItemCard(
                    hourly = hour,
                    unit = unit,
                    isHappyMode = isHappyMode
                )
            }
        }
    }
}

@Composable
private fun HourlyItemCard(
    hourly: HourlyForecast,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier,
    isHappyMode: Boolean = false
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.width(85.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            // Short weekday and day of month ("Lun 24") on every card, so the day is always clear
            val weekday = WeatherUtils.dayOfWeekIndex(hourly.time)
                ?.let { stringArrayResource(R.array.days_of_week_short)[it] }
            val dayOfMonth = WeatherUtils.dayOfMonth(hourly.time)
            Text(
                text = listOfNotNull(weekday, dayOfMonth).joinToString(" ").ifEmpty { " " },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Hour
            Text(
                text = WeatherUtils.formatHourlyTime(hourly.time),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Icon
            ColorfulWeatherIcon(
                weatherCode = hourly.weatherCode,
                isDay = hourly.isDay,
                isHappyMode = isHappyMode,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Temperature
            Text(
                text = unit.formatTemperature(hourly.temperature),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Precipitation Probability
            val rainChance = hourly.precipitationProbability
            if (rainChance != null && rainChance > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = stringResource(R.string.rain),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$rainChance%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Expected rain in mm or snow in cm (empty line when dry, so all cards keep the same height)
            PrecipitationAmount(
                rainMm = hourly.precipitation,
                snowCm = hourly.snowfall,
                keepLineWhenDry = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HourlyForecastSectionPreview() {
    val sampleHourly = List(12) { i ->
        HourlyForecast(
            // 18:00 to 05:00, crossing midnight to show the weekday label
            time = "2025-01-0${1 + (18 + i) / 24}T${((18 + i) % 24).toString().padStart(2, '0')}:00",
            temperature = 18.0 + i,
            weatherCode = if (i % 3 == 0) 61 else 0,
            weatherDescription = "Clear",
            precipitationProbability = if (i % 3 == 0) 40 else 0,
            precipitation = if (i % 3 == 0) 1.3 else 0.0,
            humidity = 60,
            windSpeed = 10.0,
            isDay = true
        )
    }

    MWeatherTheme {
        HourlyForecastSection(
            hourlyForecast = sampleHourly,
            unit = TemperatureUnit.CELSIUS,
            modifier = Modifier.padding(16.dp)
        )
    }
}
