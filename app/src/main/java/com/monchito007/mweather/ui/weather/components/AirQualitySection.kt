package com.monchito007.mweather.ui.weather.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monchito007.mweather.R
import com.monchito007.mweather.domain.model.AirQuality
import com.monchito007.mweather.domain.model.Pollen
import com.monchito007.mweather.domain.model.PollenType
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.WeatherUtils
import com.monchito007.mweather.ui.weather.orNoData
import kotlin.math.roundToInt

/** Colours of the European AQI bands, as published by the European Environment Agency. */
private val AqiBandColors = listOf(
    Color(0xFF50F0E6), // good
    Color(0xFF50CCAA), // fair
    Color(0xFFF0E641), // moderate
    Color(0xFFFF5050), // poor
    Color(0xFF960032), // very poor
    Color(0xFF7D2181)  // extremely poor
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AirQualitySection(
    airQuality: AirQuality,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Air,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.air_quality),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // AQI value and level
                val aqi = airQuality.europeanAqi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = aqi.orNoData { "$it" },
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = aqi?.let { stringResource(WeatherUtils.airQualityLevelRes(it)) }
                                ?: WeatherUtils.NO_DATA,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.european_aqi),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (aqi != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AqiScale(aqi)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main pollutants
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Pollutant("PM2.5", airQuality.pm25)
                    Pollutant("PM10", airQuality.pm10)
                    Pollutant("NO₂", airQuality.nitrogenDioxide)
                    Pollutant("O₃", airQuality.ozone)
                }

                // Pollen, only where and when there is any
                if (airQuality.pollen.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFlorist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.pollen_title),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        airQuality.pollen.forEach { PollenChip(it) }
                    }
                }
            }
        }
    }
}

/** The six AQI bands as a coloured bar, with a dot where the current value sits. */
@Composable
private fun AqiScale(aqi: Int) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val fraction = (aqi.coerceIn(0, 120) / 120f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(AqiBandColors))
        )
        Box(
            modifier = Modifier
                .offset(x = (maxWidth - 14.dp) * fraction, y = (-3).dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
                .padding(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
private fun Pollutant(name: String, microgramsPerCubicMetre: Double?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = microgramsPerCubicMetre.orNoData { "${it.roundToInt()}" },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "µg/m³",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PollenChip(pollen: Pollen) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = stringResource(
                R.string.pollen_value,
                stringResource(WeatherUtils.pollenNameRes(pollen.type)),
                pollen.grainsPerCubicMetre.roundToInt()
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AirQualitySectionPreview() {
    MWeatherTheme {
        AirQualitySection(
            airQuality = AirQuality(
                europeanAqi = 34,
                pm25 = 8.4,
                pm10 = 15.2,
                nitrogenDioxide = 21.0,
                ozone = 48.0,
                pollen = listOf(Pollen(PollenType.GRASS, 24.0), Pollen(PollenType.OLIVE, 3.0))
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
