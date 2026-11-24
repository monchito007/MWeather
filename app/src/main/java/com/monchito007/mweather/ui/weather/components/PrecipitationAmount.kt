package com.monchito007.mweather.ui.weather.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monchito007.mweather.R
import com.monchito007.mweather.ui.weather.WeatherUtils

/**
 * Expected precipitation: snow in cm with a snowflake when any snow is forecast (the mm figure
 * would only be its water equivalent), otherwise rain in mm. Shows nothing when dry, or a
 * blank line if [keepLineWhenDry] so cards in a row keep the same height.
 */
@Composable
fun PrecipitationAmount(
    rainMm: Double?,
    snowCm: Double?,
    modifier: Modifier = Modifier,
    keepLineWhenDry: Boolean = false
) {
    val locale = LocalConfiguration.current.locales[0]
    val snow = WeatherUtils.formatPrecipitationMm(snowCm, locale)
    val rain = WeatherUtils.formatPrecipitationMm(rainMm, locale)
    val style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
    val color = MaterialTheme.colorScheme.tertiary

    when {
        snow != null -> Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            Icon(
                imageVector = Icons.Rounded.AcUnit,
                contentDescription = stringResource(R.string.snow),
                tint = color,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = stringResource(R.string.snowfall_cm, snow), style = style, color = color, maxLines = 1)
        }
        rain != null -> Text(
            text = stringResource(R.string.precipitation_mm, rain),
            style = style,
            color = color,
            maxLines = 1,
            modifier = modifier
        )
        keepLineWhenDry -> Text(text = " ", style = style, modifier = modifier)
    }
}
