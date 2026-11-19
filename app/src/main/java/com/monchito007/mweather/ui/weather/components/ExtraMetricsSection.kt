package com.monchito007.mweather.ui.weather.components

import com.monchito007.mweather.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Grain
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.WeatherUtils

@Composable
fun ExtraMetricsSection(
    currentWeather: CurrentWeather,
    modifier: Modifier = Modifier,
    precipitationProbability: Int = 0,
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
                    value = "${currentWeather.humidity}%",
                    supportingText = stringResource(R.string.humidity_level),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Rounded.Air,
                    title = stringResource(R.string.wind_speed),
                    value = "${currentWeather.windSpeed} km/h",
                    supportingText = stringResource(R.string.wind_direction, currentWeather.windDirection),
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
                    value = "${currentWeather.pressure.toInt()} hPa",
                    supportingText = stringResource(R.string.atmospheric_pressure),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Rounded.WbSunny,
                    title = stringResource(R.string.uv_index),
                    value = "${currentWeather.uvIndex}",
                    supportingText = stringResource(WeatherUtils.uvIndexDescriptionRes(currentWeather.uvIndex)),
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
                    value = "$precipitationProbability%",
                    supportingText = stringResource(R.string.precipitation_amount, currentWeather.precipitation.toString()),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Rounded.Cloud,
                    title = stringResource(R.string.cloud_cover),
                    value = "${currentWeather.cloudCover}%",
                    supportingText = stringResource(R.string.cloud_coverage),
                    modifier = Modifier.weight(1f)
                )
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
    modifier: Modifier = Modifier
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

            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
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
