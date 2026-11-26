package com.monchito007.mweather.ui.weather.components

import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import com.monchito007.mweather.domain.model.DailyForecast
import com.monchito007.mweather.ui.weather.orNoData
import com.monchito007.mweather.R
import androidx.compose.ui.res.stringResource
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
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monchito007.mweather.domain.model.CurrentWeather
import com.monchito007.mweather.ui.theme.MWeatherTheme
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
    precipitationProbability: Int? = null,
    isHappyMode: Boolean = false,
    fetchedAtMillis: Long? = null,
    timezone: String? = null,
    daily: List<DailyForecast> = emptyList(),
    /** Null hides the star (nothing that could be saved is shown). */
    isFavorite: Boolean? = null,
    onToggleFavorite: () -> Unit = {}
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
                                stringResource(R.string.current_location)
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (isFavorite != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = stringResource(
                                    if (isFavorite) R.string.remove_favorite else R.string.add_favorite
                                ),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
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

                if (fetchedAtMillis != null) {
                    UpdatedAgoText(fetchedAtMillis)
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
                            text = stringResource(WeatherUtils.weatherDescriptionRes(currentWeather.weatherCode)),
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
                    if (maxTemp != null || minTemp != null) {
                        val maxLabel = stringResource(R.string.temp_max)
                        val minLabel = stringResource(R.string.temp_min)
                        Text(
                            text = "$maxLabel: ${unit.formatTemperature(maxTemp)} / $minLabel: ${unit.formatTemperature(minTemp)}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    val feelsLikeLabel = stringResource(R.string.feels_like)
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
                            label = stringResource(R.string.humidity),
                            value = currentWeather.humidity.orNoData { "$it%" }
                        )

                        // Wind Speed
                        QuickMetricBadge(
                            icon = Icons.Rounded.Air,
                            label = stringResource(R.string.wind),
                            value = currentWeather.windSpeed.orNoData { "${it.roundToInt()} km/h" },
                            windDirection = currentWeather.windDirection
                        )

                        // Rain Chance
                        QuickMetricBadge(
                            icon = Icons.Rounded.Grain,
                            label = stringResource(R.string.rain),
                            value = precipitationProbability.orNoData { "$it%" }
                        )
                    }
                }

                // Local time and time to the next sunrise/sunset, at the bottom of the card
                if (timezone != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LocalTimeAndSunRow(timezone, daily)
                }
            }
        }
    }
}

@Composable
private fun QuickMetricBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    windDirection: Int? = null
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
        if (windDirection != null) {
            Spacer(modifier = Modifier.width(4.dp))
            WindArrow(windDirection, size = 16.dp)
        }
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

/**
 * "Updated 5 min ago". The current time is re-read when the forecast changes and whenever the
 * app comes back to the foreground, which is when an old value would otherwise be noticed.
 */
/**
 * Current time at the location (not the phone's) and how long until the next sunrise or
 * sunset. Ticks at the start of every minute while on screen.
 */
@Composable
private fun LocalTimeAndSunRow(timezone: String, daily: List<DailyForecast>) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(60_000L - nowMillis % 60_000L)
        }
    }
    val nowLocal = WeatherUtils.localDateTime(nowMillis, timezone)
    val sunEvent = WeatherUtils.nextSunEvent(nowLocal, daily)
    val color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
    val style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Rounded.Schedule,
            contentDescription = stringResource(R.string.local_time),
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = WeatherUtils.formatHourlyTime(nowLocal), style = style, color = color)

        if (sunEvent != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = if (sunEvent.isSunrise) Icons.Rounded.WbTwilight else Icons.Rounded.NightsStay,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            val hours = sunEvent.minutesUntil / 60
            val minutes = sunEvent.minutesUntil % 60
            val duration = if (hours > 0) stringResource(R.string.duration_h_min, hours, minutes)
            else stringResource(R.string.duration_min, minutes)
            Text(
                text = stringResource(if (sunEvent.isSunrise) R.string.sunrise_in else R.string.sunset_in, duration),
                style = style,
                color = color
            )
        }
    }
}

@Composable
private fun UpdatedAgoText(fetchedAtMillis: Long) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LifecycleResumeEffect(fetchedAtMillis) {
        nowMillis = System.currentTimeMillis()
        onPauseOrDispose { }
    }
    val (textRes, amount) = WeatherUtils.updatedAgo(fetchedAtMillis, nowMillis)
    Text(
        text = stringResource(textRes, amount),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
        modifier = Modifier.padding(start = 30.dp, top = 2.dp) // aligned with the location name
    )
}
