package com.monchito007.mweather.ui.weather.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.AppLanguage
import com.monchito007.mweather.ui.weather.TemperatureUnit
import com.monchito007.mweather.ui.weather.WeatherUtils

@Composable
fun CurrentWeatherCard(
    locationName: String,
    currentWeather: CurrentWeather,
    unit: TemperatureUnit,
    onToggleUnit: () -> Unit,
    maxTemp: Double?,
    minTemp: Double?,
    modifier: Modifier = Modifier,
    precipitationProbability: Int = 0,
    language: AppLanguage = AppLanguage.SPANISH,
    isHappyMode: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primaryContainer
    val secondaryColor = MaterialTheme.colorScheme.tertiaryContainer
    val gradientBrush = Brush.linearGradient(
        colors = listOf(primaryColor, secondaryColor)
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradientBrush)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Row: Location name and Temperature Unit Toggle Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = locationName.ifBlank {
                                if (language == AppLanguage.SPANISH) "Ubicación actual" else "Current Location"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Unit Toggle Button
                    FilledTonalButton(
                        onClick = onToggleUnit,
                        shape = CircleShape
                    ) {
                        Text(
                            text = unit.symbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Center Row: Weather Icon and Temperature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = unit.formatTemperature(currentWeather.temperature),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 64.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = WeatherUtils.getWeatherDescription(currentWeather.weatherCode, language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        ColorfulWeatherIcon(
                            weatherCode = currentWeather.weatherCode,
                            isDay = currentWeather.isDay,
                            isHappyMode = isHappyMode,
                            modifier = Modifier.size(92.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High / Low & Apparent Temperature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (maxTemp != null && minTemp != null) {
                        val maxLabel = if (language == AppLanguage.SPANISH) "Máx" else "High"
                        val minLabel = if (language == AppLanguage.SPANISH) "Mín" else "Low"
                        Text(
                            text = "$maxLabel: ${unit.formatTemperature(maxTemp)} / $minLabel: ${unit.formatTemperature(minTemp)}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    val feelsLikeLabel = if (language == AppLanguage.SPANISH) "Sensación" else "Feels like"
                    Text(
                        text = "$feelsLikeLabel: ${unit.formatTemperature(currentWeather.apparentTemperature)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Quick Metrics Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Humidity
                        QuickMetricBadge(
                            icon = Icons.Rounded.WaterDrop,
                            label = if (language == AppLanguage.SPANISH) "Humedad" else "Humidity",
                            value = "${currentWeather.humidity}%"
                        )

                        // Wind Speed
                        QuickMetricBadge(
                            icon = Icons.Rounded.Air,
                            label = if (language == AppLanguage.SPANISH) "Viento" else "Wind",
                            value = "${currentWeather.windSpeed.toInt()} km/h"
                        )

                        // Rain Chance
                        QuickMetricBadge(
                            icon = Icons.Rounded.Grain,
                            label = if (language == AppLanguage.SPANISH) "Lluvia" else "Rain",
                            value = "$precipitationProbability%"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickMetricBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentWeatherCardPreview() {
    MWeatherTheme {
        CurrentWeatherCard(
            locationName = "San José, Costa Rica",
            currentWeather = CurrentWeather(
                temperature = 22.5,
                apparentTemperature = 21.0,
                humidity = 65,
                windSpeed = 12.3,
                windDirection = 180,
                weatherCode = 1,
                weatherDescription = "Mainly clear",
                isDay = true,
                precipitation = 0.0,
                pressure = 1013.25,
                cloudCover = 20,
                uvIndex = 6.0
            ),
            unit = TemperatureUnit.CELSIUS,
            onToggleUnit = {},
            maxTemp = 25.0,
            minTemp = 17.0,
            precipitationProbability = 20,
            modifier = Modifier.padding(16.dp)
        )
    }
}
