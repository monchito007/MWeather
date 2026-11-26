package com.monchito007.mweather.ui.weather.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monchito007.mweather.R
import com.monchito007.mweather.ui.weather.WeatherUtils

/**
 * Arrow pointing where the wind blows TO. [degrees] is the meteorological direction the wind
 * comes FROM (0 = from the north), hence the extra 180°. Announced as "Wind from the <point>".
 */
@Composable
fun WindArrow(
    degrees: Int,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val from = stringArrayResource(R.array.compass_points)[WeatherUtils.compassPointIndex(degrees)]
    Icon(
        imageVector = Icons.Rounded.Navigation,
        contentDescription = stringResource(R.string.wind_from, from),
        tint = tint,
        modifier = modifier
            .size(size)
            .rotate(degrees + 180f)
    )
}

/** [WindArrow] inside a small ring, for places with room for a compass look. */
@Composable
fun WindCompass(degrees: Int, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(22.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    ) {
        WindArrow(degrees)
    }
}
