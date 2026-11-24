package com.monchito007.mweather.ui.weather.components

import kotlin.math.roundToInt
import com.monchito007.mweather.ui.weather.orNoData
import com.monchito007.mweather.R
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.WeatherUtils

@Composable
fun ExtraMetricsSection(
    currentWeather: CurrentWeather,
    modifier: Modifier = Modifier,
    precipitationProbability: Int? = null,
    today: DailyForecast? = null,
    tomorrow: DailyForecast? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.GridView,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.detailed_metrics_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grid Rows
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row 1: Humidity and Wind Speed
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    icon = Icons.Rounded.WaterDrop,
                    title = stringResource(R.string.humidity),
                    value = currentWeather.humidity.orNoData { "$it%" },
                    supportingText = stringResource(R.string.humidity_level),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Rounded.Air,
                    title = stringResource(R.string.wind_speed),
                    value = currentWeather.windSpeed.orNoData { "$it km/h" },
                    supportingText = currentWeather.windDirection?.let {
                        val point = stringArrayResource(R.array.compass_points_short)[WeatherUtils.compassPointIndex(it)]
                        stringResource(R.string.wind_direction, point, it)
                    } ?: WeatherUtils.NO_DATA,
                    supportingLeading = currentWeather.windDirection?.let { degrees ->
                        { WindCompass(degrees) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Pressure and UV Index
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    icon = Icons.Rounded.Compress,
                    title = stringResource(R.string.pressure),
                    value = currentWeather.pressure.orNoData { "${it.roundToInt()} hPa" },
                    supportingText = stringResource(R.string.atmospheric_pressure),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Rounded.WbSunny,
                    title = stringResource(R.string.uv_index),
                    value = currentWeather.uvIndex.orNoData { "$it" },
                    // Current UV (0 at night) plus today's peak, so the card is right at any hour
                    supportingText = currentWeather.uvIndex?.let { uv ->
                        val level = stringResource(WeatherUtils.uvIndexDescriptionRes(uv))
                        today?.uvIndexMax?.let { stringResource(R.string.uv_with_max, level, "$it") } ?: level
                    } ?: WeatherUtils.NO_DATA,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: Rain Chance and Cloud Cover
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    icon = Icons.Rounded.Grain,
                    title = stringResource(R.string.rain_chance),
                    value = precipitationProbability.orNoData { "$it%" },
                    supportingText = currentWeather.precipitation?.let { stringResource(R.string.precipitation_amount, it.toString()) }
                        ?: WeatherUtils.NO_DATA,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Rounded.Cloud,
                    title = stringResource(R.string.cloud_cover),
                    value = currentWeather.cloudCover.orNoData { "$it%" },
                    supportingText = stringResource(R.string.cloud_coverage),
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: Sunrise (with hours of daylight) and Sunset (with tomorrow's change in daylight)
            if (today?.sunrise != null || today?.sunset != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        icon = Icons.Rounded.WbTwilight,
                        title = stringResource(R.string.sunrise),
                        value = today.sunrise.orNoData { WeatherUtils.formatHourlyTime(it) },
                        supportingText = WeatherUtils.daylightMinutes(today.sunrise, today.sunset)
                            ?.let { stringResource(R.string.daylight_duration, it / 60, it % 60) }
                            ?: WeatherUtils.NO_DATA,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Rounded.NightsStay,
                        title = stringResource(R.string.sunset),
                        value = today.sunset.orNoData { WeatherUtils.formatHourlyTime(it) },
                        supportingText = WeatherUtils.daylightChange(today, tomorrow)
                            ?.let { stringResource(R.string.daylight_change, it) }
                            ?: WeatherUtils.NO_DATA,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    supportingLeading: (@Composable () -> Unit)? = null
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (supportingLeading != null) {
                    supportingLeading()
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Small compass: a ring with an arrow pointing where the wind blows TO. [degrees] is the
 * meteorological direction the wind comes FROM (0 = from the north), hence the extra 180°.
 */
@Composable
private fun WindCompass(degrees: Int, modifier: Modifier = Modifier) {
    val from = stringArrayResource(R.array.compass_points)[WeatherUtils.compassPointIndex(degrees)]
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(22.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Rounded.Navigation,
            contentDescription = stringResource(R.string.wind_from, from),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(14.dp)
                .rotate(degrees + 180f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExtraMetricsSectionPreview() {
    val sampleCurrent = CurrentWeather(
        temperature = 22.0,
        apparentTemperature = 21.0,
        humidity = 70,
        windSpeed = 15.4,
        windDirection = 210,
        weatherCode = 3,
        weatherDescription = "Overcast",
        isDay = true,
        precipitation = 1.2,
        pressure = 1012.0,
        cloudCover = 85,
        uvIndex = 4.5
    )

    MWeatherTheme {
        ExtraMetricsSection(
            currentWeather = sampleCurrent,
            precipitationProbability = 45,
            modifier = Modifier.padding(16.dp)
        )
    }
}
