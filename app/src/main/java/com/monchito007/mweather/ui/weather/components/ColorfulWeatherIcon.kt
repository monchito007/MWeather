package com.monchito007.mweather.ui.weather.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class WeatherIconType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY,
    PARTLY_CLOUDY_NIGHT,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    THUNDERSTORM,
    SNOW
}

object WeatherIconMapper {
    fun getIconType(weatherCode: Int, isDay: Boolean): WeatherIconType {
        return when (weatherCode) {
            0 -> if (isDay) WeatherIconType.CLEAR_DAY else WeatherIconType.CLEAR_NIGHT
            1, 2 -> if (isDay) WeatherIconType.PARTLY_CLOUDY_DAY else WeatherIconType.PARTLY_CLOUDY_NIGHT
            3 -> WeatherIconType.CLOUDY
            45, 48 -> WeatherIconType.FOG
            51, 53, 55, 56, 57 -> WeatherIconType.DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> WeatherIconType.RAIN
            71, 73, 75, 77, 85, 86 -> WeatherIconType.SNOW
            95, 96, 99 -> WeatherIconType.THUNDERSTORM
            else -> if (isDay) WeatherIconType.CLEAR_DAY else WeatherIconType.CLEAR_NIGHT
        }
    }
}

enum class FaceStyle {
    HAPPY_SUN,
    SLEEPY_MOON,
    KAWAII_CLOUD,
    CHEEKY_STORM,
    COZY_FOG
}

/**
 * Vibrant, highly colorful, clear, and graphically expressive weather icon composable.
 * Supports "Happy Mode" where elements feature cute, friendly faces!
 */
@Composable
fun ColorfulWeatherIcon(
    weatherCode: Int,
    isDay: Boolean = true,
    isHappyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val iconType = WeatherIconMapper.getIconType(weatherCode, isDay)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (iconType) {
            WeatherIconType.CLEAR_DAY -> drawClearDay(w, h, isHappyMode)
            WeatherIconType.CLEAR_NIGHT -> drawClearNight(w, h, isHappyMode)
            WeatherIconType.PARTLY_CLOUDY_DAY -> drawPartlyCloudyDay(w, h, isHappyMode)
            WeatherIconType.PARTLY_CLOUDY_NIGHT -> drawPartlyCloudyNight(w, h, isHappyMode)
            WeatherIconType.CLOUDY -> drawCloudy(w, h, isHappyMode)
            WeatherIconType.FOG -> drawFog(w, h, isHappyMode)
            WeatherIconType.DRIZZLE -> drawDrizzle(w, h, isHappyMode)
            WeatherIconType.RAIN -> drawRain(w, h, isHappyMode)
            WeatherIconType.THUNDERSTORM -> drawThunderstorm(w, h, isHappyMode)
            WeatherIconType.SNOW -> drawSnow(w, h, isHappyMode)
        }
    }
}

// --- DRAWING IMPLEMENTATIONS ---

private fun DrawScope.drawClearDay(w: Float, h: Float, isHappyMode: Boolean = false) {
    val center = Offset(w * 0.50f, h * 0.50f)
    val sunRadius = w * 0.22f

    // Soft radial halo background (#FFB300, #FF6F00 glow)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFE082).copy(alpha = 0.55f),
                Color(0xFFFFB300).copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = center,
            radius = w * 0.48f
        ),
        center = center,
        radius = w * 0.48f
    )

    // Sun Rays
    val rayStart = w * 0.28f
    val rayEnd = w * 0.40f
    val rayStroke = w * 0.06f
    for (i in 0 until 8) {
        val angleRad = Math.toRadians(i * 45.0).toFloat()
        val start = Offset(
            center.x + rayStart * cos(angleRad),
            center.y + rayStart * sin(angleRad)
        )
        val end = Offset(
            center.x + rayEnd * cos(angleRad),
            center.y + rayEnd * sin(angleRad)
        )
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFB300), Color(0xFFFF6F00)),
                start = start,
                end = end
            ),
            start = start,
            end = end,
            strokeWidth = rayStroke,
            cap = StrokeCap.Round
        )
    }

    // Sun Body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFFF6F00)),
            center = Offset(center.x - sunRadius * 0.3f, center.y - sunRadius * 0.3f),
            radius = sunRadius * 1.3f
        ),
        center = center,
        radius = sunRadius
    )

    if (isHappyMode) {
        drawKawaiiFace(
            center = center,
            faceSize = sunRadius * 1.10f,
            style = FaceStyle.HAPPY_SUN,
            eyeColor = Color(0xFF5D4037),
            cheekColor = Color(0xFFFF5252)
        )
    }
}

private fun DrawScope.drawClearNight(w: Float, h: Float, isHappyMode: Boolean = false) {
    val center = Offset(w * 0.48f, h * 0.50f)

    // Deep Indigo Night Sky Background Halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF3F51B5).copy(alpha = 0.40f),
                Color(0xFF1A237E).copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = center,
            radius = w * 0.48f
        ),
        center = center,
        radius = w * 0.48f
    )

    // Golden Crescent Moon (#FFD54F)
    val moonCenter = Offset(w * 0.42f, h * 0.52f)
    val outerRadius = w * 0.28f
    drawCrescentMoon(
        center = moonCenter,
        outerRadius = outerRadius,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFF59D), Color(0xFFFFD54F), Color(0xFFFFB300)),
            start = Offset(moonCenter.x - outerRadius, moonCenter.y - outerRadius),
            end = Offset(moonCenter.x + outerRadius, moonCenter.y + outerRadius)
        )
    )

    // Glowing Golden Stars (#FFE082)
    drawSparkleStar(Offset(w * 0.72f, h * 0.24f), w * 0.09f, Color(0xFFFFE082))
    drawSparkleStar(Offset(w * 0.84f, h * 0.50f), w * 0.07f, Color(0xFFFFF9C4))
    drawSparkleStar(Offset(w * 0.22f, h * 0.22f), w * 0.06f, Color(0xFFFFE082))
    drawSparkleStar(Offset(w * 0.28f, h * 0.78f), w * 0.07f, Color(0xFFFFF59D))

    if (isHappyMode) {
        val moonFaceCenter = Offset(moonCenter.x - outerRadius * 0.18f, moonCenter.y + outerRadius * 0.04f)
        drawKawaiiFace(
            center = moonFaceCenter,
            faceSize = outerRadius * 0.85f,
            style = FaceStyle.SLEEPY_MOON,
            eyeColor = Color(0xFF3E2723),
            cheekColor = Color(0xFFFF8A80)
        )
    }
}

private fun DrawScope.drawPartlyCloudyDay(w: Float, h: Float, isHappyMode: Boolean = false) {
    // Sun in Top Right peeking behind cloud
    val sunCenter = Offset(w * 0.65f, h * 0.36f)
    val sunRadius = w * 0.18f

    // Sun Halo & Rays
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFE082).copy(alpha = 0.40f), Color.Transparent),
            center = sunCenter,
            radius = w * 0.35f
        ),
        center = sunCenter,
        radius = w * 0.35f
    )

    for (i in 0 until 6) {
        val angleRad = Math.toRadians(i * 60.0).toFloat()
        val start = Offset(sunCenter.x + (sunRadius * 1.1f) * cos(angleRad), sunCenter.y + (sunRadius * 1.1f) * sin(angleRad))
        val end = Offset(sunCenter.x + (sunRadius * 1.6f) * cos(angleRad), sunCenter.y + (sunRadius * 1.6f) * sin(angleRad))
        drawLine(
            color = Color(0xFFFFB300),
            start = start,
            end = end,
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )
    }

    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFFF6F00))
        ),
        center = sunCenter,
        radius = sunRadius
    )

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(sunCenter.x + sunRadius * 0.15f, sunCenter.y - sunRadius * 0.15f),
            faceSize = sunRadius * 0.85f,
            style = FaceStyle.HAPPY_SUN,
            eyeColor = Color(0xFF5D4037)
        )
    }

    // Soft Blue-Gray Cloud in Foreground (#90A4AE, #E0F7FA)
    val cloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFE0F7FA), Color(0xFF90A4AE))
    )
    drawMainCloud(w, h, cloudBrush, offsetY = h * 0.05f)

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.48f, h * 0.52f + h * 0.05f),
            faceSize = w * 0.26f,
            style = FaceStyle.KAWAII_CLOUD,
            eyeColor = Color(0xFF37474F)
        )
    }
}

private fun DrawScope.drawPartlyCloudyNight(w: Float, h: Float, isHappyMode: Boolean = false) {
    // Crescent Moon in Top Right
    val moonCenter = Offset(w * 0.65f, h * 0.38f)
    val moonRadius = w * 0.22f
    drawCrescentMoon(
        center = moonCenter,
        outerRadius = moonRadius,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFF59D), Color(0xFFFFD54F))
        )
    )
    drawSparkleStar(Offset(w * 0.25f, h * 0.22f), w * 0.07f, Color(0xFFFFE082))

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(moonCenter.x - moonRadius * 0.15f, moonCenter.y),
            faceSize = moonRadius * 0.75f,
            style = FaceStyle.SLEEPY_MOON,
            eyeColor = Color(0xFF3E2723)
        )
    }

    // Soft Cloud in Foreground
    val cloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFECEFF1), Color(0xFFB0BEC5), Color(0xFF78909C))
    )
    drawMainCloud(w, h, cloudBrush, offsetY = h * 0.05f)

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.48f, h * 0.52f + h * 0.05f),
            faceSize = w * 0.26f,
            style = FaceStyle.KAWAII_CLOUD,
            eyeColor = Color(0xFF263238)
        )
    }
}

private fun DrawScope.drawCloudy(w: Float, h: Float, isHappyMode: Boolean = false) {
    // Multi-layered soft clouds (#78909C, #B0BEC5)
    // Back darker cloud layer
    val backCloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFF546E7A))
    )
    drawCloudPath(
        leftCenter = Offset(w * 0.40f, h * 0.45f),
        leftRadius = w * 0.18f,
        topCenter = Offset(w * 0.58f, h * 0.35f),
        topRadius = w * 0.22f,
        rightCenter = Offset(w * 0.78f, h * 0.46f),
        rightRadius = w * 0.17f,
        baseRect = Rect(w * 0.26f, h * 0.42f, w * 0.88f, h * 0.58f),
        cornerRadius = w * 0.10f,
        brush = backCloudBrush
    )

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.65f, h * 0.40f),
            faceSize = w * 0.16f,
            style = FaceStyle.KAWAII_CLOUD,
            eyeColor = Color(0xFFECEFF1)
        )
    }

    // Front lighter cloud layer
    val frontCloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1), Color(0xFFB0BEC5))
    )
    drawMainCloud(w, h, frontCloudBrush, offsetY = h * 0.08f)

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.48f, h * 0.52f + h * 0.08f),
            faceSize = w * 0.26f,
            style = FaceStyle.KAWAII_CLOUD,
            eyeColor = Color(0xFF37474F)
        )
    }
}

private fun DrawScope.drawFog(w: Float, h: Float, isHappyMode: Boolean = false) {
    // Back cloud layer
    val backCloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFF546E7A))
    )
    drawCloudPath(
        leftCenter = Offset(w * 0.40f, h * 0.45f),
        leftRadius = w * 0.18f,
        topCenter = Offset(w * 0.58f, h * 0.35f),
        topRadius = w * 0.22f,
        rightCenter = Offset(w * 0.78f, h * 0.46f),
        rightRadius = w * 0.17f,
        baseRect = Rect(w * 0.26f, h * 0.42f, w * 0.88f, h * 0.58f),
        cornerRadius = w * 0.10f,
        brush = backCloudBrush
    )

    val frontCloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1), Color(0xFFB0BEC5))
    )
    drawMainCloud(w, h, frontCloudBrush, offsetY = h * 0.08f)

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.48f, h * 0.52f + h * 0.08f),
            faceSize = w * 0.26f,
            style = FaceStyle.COZY_FOG,
            eyeColor = Color(0xFF37474F)
        )
    }

    drawFogStreaks(w, h)
}

private fun DrawScope.drawDrizzle(w: Float, h: Float, isHappyMode: Boolean = false) {
    // Soft Rain Cloud
    val cloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFECEFF1), Color(0xFF90A4AE), Color(0xFF607D8B))
    )
    drawMainCloud(w, h, cloudBrush, offsetY = -h * 0.05f)

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.48f, h * 0.52f - h * 0.05f),
            faceSize = w * 0.26f,
            style = FaceStyle.KAWAII_CLOUD,
            eyeColor = Color(0xFF263238)
        )
    }

    // Light Vivid Cyan Raindrops
    val drops = listOf(
        Offset(w * 0.32f, h * 0.70f),
        Offset(w * 0.50f, h * 0.75f),
        Offset(w * 0.68f, h * 0.70f)
    )
    drawRaindrops(
        drops = drops,
        dropLength = h * 0.12f,
        dropWidth = w * 0.05f,
        colors = listOf(Color(0xFF80DEEA), Color(0xFF29B6F6))
    )
}

private fun DrawScope.drawRain(w: Float, h: Float, isHappyMode: Boolean = false) {
    // Rain Cloud
    val cloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFB0BEC5), Color(0xFF78909C), Color(0xFF455A64))
    )
    drawMainCloud(w, h, cloudBrush, offsetY = -h * 0.06f)

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.48f, h * 0.52f - h * 0.06f),
            faceSize = w * 0.26f,
            style = FaceStyle.KAWAII_CLOUD,
            eyeColor = Color(0xFF1A237E)
        )
    }

    // Vivid Blue/Cyan Raindrops (#29B6F6, #0288D1)
    val drops = listOf(
        Offset(w * 0.25f, h * 0.68f),
        Offset(w * 0.42f, h * 0.78f),
        Offset(w * 0.58f, h * 0.68f),
        Offset(w * 0.75f, h * 0.76f)
    )
    drawRaindrops(
        drops = drops,
        dropLength = h * 0.16f,
        dropWidth = w * 0.06f,
        colors = listOf(Color(0xFF29B6F6), Color(0xFF0288D1), Color(0xFF00B0FF))
    )
}

private fun DrawScope.drawThunderstorm(w: Float, h: Float, isHappyMode: Boolean = false) {
    // Deep Indigo Storm Cloud (#37474F, #1A237E)
    val cloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF546E7A), Color(0xFF37474F), Color(0xFF1A237E))
    )
    drawMainCloud(w, h, cloudBrush, offsetY = -h * 0.08f)

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.48f, h * 0.52f - h * 0.08f),
            faceSize = w * 0.26f,
            style = FaceStyle.CHEEKY_STORM,
            eyeColor = Color(0xFFECEFF1),
            cheekColor = Color(0xFFFF5252)
        )
    }

    // Bold Electric Yellow Lightning Bolt (#FFEB3B, #F57F17)
    drawLightningBolt(
        center = Offset(w * 0.50f, h * 0.62f),
        scale = w * 0.70f
    )

    // Raindrops around lightning
    val drops = listOf(
        Offset(w * 0.22f, h * 0.72f),
        Offset(w * 0.78f, h * 0.72f)
    )
    drawRaindrops(
        drops = drops,
        dropLength = h * 0.14f,
        dropWidth = w * 0.05f,
        colors = listOf(Color(0xFF29B6F6), Color(0xFF00E5FF))
    )
}

private fun DrawScope.drawSnow(w: Float, h: Float, isHappyMode: Boolean = false) {
    // Soft Blue-White Cloud
    val cloudBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFE0F7FA), Color(0xFF78909C))
    )
    drawMainCloud(w, h, cloudBrush, offsetY = -h * 0.06f)

    if (isHappyMode) {
        drawKawaiiFace(
            center = Offset(w * 0.48f, h * 0.52f - h * 0.06f),
            faceSize = w * 0.26f,
            style = FaceStyle.KAWAII_CLOUD,
            eyeColor = Color(0xFF37474F),
            cheekColor = Color(0xFF80DEEA)
        )
    }

    // Soft Cyan/White Crystalline Snowflakes (#80DEEA, #E0F7FA)
    drawSnowflake(Offset(w * 0.50f, h * 0.75f), w * 0.11f, Color(0xFF80DEEA))
    drawSnowflake(Offset(w * 0.26f, h * 0.70f), w * 0.08f, Color(0xFFE0F7FA))
    drawSnowflake(Offset(w * 0.74f, h * 0.71f), w * 0.08f, Color(0xFF80DEEA))
}

// --- HELPER GRAPHICS DRAWING FUNCTIONS ---

private fun DrawScope.drawKawaiiFace(
    center: Offset,
    faceSize: Float,
    style: FaceStyle = FaceStyle.KAWAII_CLOUD,
    eyeColor: Color = Color(0xFF2C3E50),
    cheekColor: Color = Color(0xFFFF8A80)
) {
    val eyeDistance = faceSize * 0.36f
    val eyeYOffset = -faceSize * 0.08f
    val cheekYOffset = faceSize * 0.10f
    val cheekDistance = faceSize * 0.46f
    val mouthYOffset = faceSize * 0.16f

    val leftEyeCenter = Offset(center.x - eyeDistance, center.y + eyeYOffset)
    val rightEyeCenter = Offset(center.x + eyeDistance, center.y + eyeYOffset)
    val leftCheekCenter = Offset(center.x - cheekDistance, center.y + cheekYOffset)
    val rightCheekCenter = Offset(center.x + cheekDistance, center.y + cheekYOffset)

    // 1. Rosy Cheeks (Bigger & Rosier)
    val cheekRadius = faceSize * 0.18f
    drawCircle(
        color = cheekColor.copy(alpha = 0.75f),
        center = leftCheekCenter,
        radius = cheekRadius
    )
    drawCircle(
        color = cheekColor.copy(alpha = 0.75f),
        center = rightCheekCenter,
        radius = cheekRadius
    )

    // 2. Eyes based on style (Substantially Larger)
    val eyeRadius = faceSize * 0.15f
    val strokeWidth = faceSize * 0.10f

    when (style) {
        FaceStyle.HAPPY_SUN, FaceStyle.KAWAII_CLOUD -> {
            // Cute glossy eyes
            drawCircle(color = eyeColor, center = leftEyeCenter, radius = eyeRadius)
            drawCircle(color = eyeColor, center = rightEyeCenter, radius = eyeRadius)
            // Eye shine reflection dots
            val shineOffset = eyeRadius * 0.35f
            val shineRadius = eyeRadius * 0.38f
            drawCircle(
                color = Color.White,
                center = Offset(leftEyeCenter.x - shineOffset, leftEyeCenter.y - shineOffset),
                radius = shineRadius
            )
            drawCircle(
                color = Color.White,
                center = Offset(rightEyeCenter.x - shineOffset, rightEyeCenter.y - shineOffset),
                radius = shineRadius
            )
        }
        FaceStyle.SLEEPY_MOON -> {
            // Closed happy arc eyes (^ ^)
            drawHappyArcEye(leftEyeCenter, eyeRadius * 1.3f, strokeWidth, eyeColor)
            drawHappyArcEye(rightEyeCenter, eyeRadius * 1.3f, strokeWidth, eyeColor)
        }
        FaceStyle.CHEEKY_STORM -> {
            // Wink eye on left, glossy eye on right
            drawWinkEye(leftEyeCenter, eyeRadius * 1.3f, strokeWidth, eyeColor)
            drawCircle(color = eyeColor, center = rightEyeCenter, radius = eyeRadius)
            drawCircle(
                color = Color.White,
                center = Offset(rightEyeCenter.x - eyeRadius * 0.35f, rightEyeCenter.y - eyeRadius * 0.35f),
                radius = eyeRadius * 0.38f
            )
        }
        FaceStyle.COZY_FOG -> {
            // Contented closed eyes (v v)
            drawContentedEye(leftEyeCenter, eyeRadius * 1.3f, strokeWidth, eyeColor)
            drawContentedEye(rightEyeCenter, eyeRadius * 1.3f, strokeWidth, eyeColor)
        }
    }

    // 3. Mouth (Bigger & Clearer)
    val mouthPath = Path()
    val mouthWidth = faceSize * 0.30f
    val mouthCurve = faceSize * 0.22f

    when (style) {
        FaceStyle.HAPPY_SUN, FaceStyle.KAWAII_CLOUD, FaceStyle.SLEEPY_MOON -> {
            // Big curved smile (◡)
            mouthPath.moveTo(center.x - mouthWidth, center.y + mouthYOffset)
            mouthPath.quadraticTo(
                center.x, center.y + mouthYOffset + mouthCurve,
                center.x + mouthWidth, center.y + mouthYOffset
            )
            drawPath(
                path = mouthPath,
                color = eyeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        FaceStyle.CHEEKY_STORM -> {
            // Cute playful 'w' mouth
            mouthPath.moveTo(center.x - mouthWidth, center.y + mouthYOffset)
            mouthPath.quadraticTo(
                center.x - mouthWidth * 0.5f, center.y + mouthYOffset + mouthCurve * 0.8f,
                center.x, center.y + mouthYOffset
            )
            mouthPath.quadraticTo(
                center.x + mouthWidth * 0.5f, center.y + mouthYOffset + mouthCurve * 0.8f,
                center.x + mouthWidth, center.y + mouthYOffset
            )
            drawPath(
                path = mouthPath,
                color = eyeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        FaceStyle.COZY_FOG -> {
            // Cute small open 'o' mouth
            drawCircle(
                color = eyeColor,
                center = Offset(center.x, center.y + mouthYOffset + eyeRadius * 0.5f),
                radius = eyeRadius * 0.8f
            )
        }
    }
}

private fun DrawScope.drawHappyArcEye(center: Offset, radius: Float, strokeWidth: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - radius, center.y + radius * 0.3f)
        quadraticTo(center.x, center.y - radius * 0.8f, center.x + radius, center.y + radius * 0.3f)
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.drawWinkEye(center: Offset, radius: Float, strokeWidth: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - radius, center.y)
        lineTo(center.x + radius, center.y)
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.drawContentedEye(center: Offset, radius: Float, strokeWidth: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x - radius, center.y - radius * 0.3f)
        quadraticTo(center.x, center.y + radius * 0.8f, center.x + radius, center.y - radius * 0.3f)
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.drawMainCloud(
    w: Float,
    h: Float,
    brush: Brush,
    offsetY: Float = 0f
) {
    drawCloudPath(
        leftCenter = Offset(w * 0.32f, h * 0.52f + offsetY),
        leftRadius = w * 0.17f,
        topCenter = Offset(w * 0.48f, h * 0.42f + offsetY),
        topRadius = w * 0.23f,
        rightCenter = Offset(w * 0.68f, h * 0.52f + offsetY),
        rightRadius = w * 0.18f,
        baseRect = Rect(w * 0.18f, h * 0.48f + offsetY, w * 0.82f, h * 0.65f + offsetY),
        cornerRadius = w * 0.10f,
        brush = brush
    )
}

private fun DrawScope.drawCloudPath(
    leftCenter: Offset,
    leftRadius: Float,
    topCenter: Offset,
    topRadius: Float,
    rightCenter: Offset,
    rightRadius: Float,
    baseRect: Rect,
    cornerRadius: Float,
    brush: Brush
) {
    val cloudPath = Path().apply {
        addOval(rectFromCenterRadius(leftCenter, leftRadius))
        addOval(rectFromCenterRadius(topCenter, topRadius))
        addOval(rectFromCenterRadius(rightCenter, rightRadius))
        addRoundRect(RoundRect(rect = baseRect, cornerRadius = CornerRadius(cornerRadius)))
    }
    drawPath(path = cloudPath, brush = brush)
}

private fun DrawScope.drawCrescentMoon(
    center: Offset,
    outerRadius: Float,
    brush: Brush
) {
    val outerRect = rectFromCenterRadius(center, outerRadius)
    val innerCenter = Offset(center.x + outerRadius * 0.42f, center.y - outerRadius * 0.22f)
    val innerRadius = outerRadius * 0.85f
    val innerRect = rectFromCenterRadius(innerCenter, innerRadius)

    val moonPath = Path().apply {
        addOval(outerRect)
    }
    val subtractPath = Path().apply {
        addOval(innerRect)
    }
    val resultPath = Path()
    resultPath.op(moonPath, subtractPath, PathOperation.Difference)

    drawPath(path = resultPath, brush = brush)
}

private fun DrawScope.drawSparkleStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path = path, color = color)
    drawCircle(color = Color.White, center = center, radius = radius * 0.30f)
}

private fun DrawScope.drawLightningBolt(
    center: Offset,
    scale: Float
) {
    val boltPath = Path().apply {
        moveTo(center.x + scale * 0.08f, center.y - scale * 0.32f)
        lineTo(center.x - scale * 0.16f, center.y + scale * 0.04f)
        lineTo(center.x + scale * 0.02f, center.y + scale * 0.04f)
        lineTo(center.x - scale * 0.20f, center.y + scale * 0.42f)
        lineTo(center.x + scale * 0.22f, center.y - scale * 0.04f)
        lineTo(center.x + scale * 0.04f, center.y - scale * 0.04f)
        close()
    }

    // Outer glow
    drawPath(
        path = boltPath,
        color = Color(0xFFFFE082).copy(alpha = 0.6f),
        style = Stroke(width = scale * 0.08f, cap = StrokeCap.Round)
    )

    // Electric Yellow Fill
    val boltBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFF59D), Color(0xFFFFEB3B), Color(0xFFF57F17)),
        startY = center.y - scale * 0.32f,
        endY = center.y + scale * 0.42f
    )
    drawPath(path = boltPath, brush = boltBrush)
}

private fun DrawScope.drawRaindrops(
    drops: List<Offset>,
    dropLength: Float,
    dropWidth: Float,
    colors: List<Color>
) {
    val angleRad = Math.toRadians(15.0).toFloat()
    val deltaX = (dropLength / 2) * sin(angleRad)
    val deltaY = (dropLength / 2) * cos(angleRad)

    drops.forEachIndexed { index, dropCenter ->
        val color = colors[index % colors.size]
        val start = Offset(dropCenter.x - deltaX, dropCenter.y - deltaY)
        val end = Offset(dropCenter.x + deltaX, dropCenter.y + deltaY)

        val dropBrush = Brush.linearGradient(
            colors = listOf(color.copy(alpha = 0.95f), color),
            start = start,
            end = end
        )

        drawLine(
            brush = dropBrush,
            start = start,
            end = end,
            strokeWidth = dropWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawFogStreaks(w: Float, h: Float) {
    val streakColor1 = Color(0xFFB0BEC5).copy(alpha = 0.85f)
    val streakColor2 = Color(0xFFE0F7FA).copy(alpha = 0.75f)
    val streakColor3 = Color(0xFF78909C).copy(alpha = 0.65f)

    drawLine(
        color = streakColor1,
        start = Offset(w * 0.15f, h * 0.68f),
        end = Offset(w * 0.85f, h * 0.68f),
        strokeWidth = h * 0.06f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = streakColor2,
        start = Offset(w * 0.25f, h * 0.78f),
        end = Offset(w * 0.75f, h * 0.78f),
        strokeWidth = h * 0.05f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = streakColor3,
        start = Offset(w * 0.10f, h * 0.58f),
        end = Offset(w * 0.60f, h * 0.58f),
        strokeWidth = h * 0.05f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSnowflake(
    center: Offset,
    radius: Float,
    color: Color
) {
    val strokeWidth = radius * 0.18f
    for (i in 0 until 6) {
        val angleRad = Math.toRadians(i * 60.0).toFloat()
        val end = Offset(
            x = center.x + radius * cos(angleRad),
            y = center.y + radius * sin(angleRad)
        )
        drawLine(
            color = color,
            start = center,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        val branchStartDist = radius * 0.55f
        val branchLength = radius * 0.35f
        val branchStart = Offset(
            x = center.x + branchStartDist * cos(angleRad),
            y = center.y + branchStartDist * sin(angleRad)
        )
        val branch1Angle = Math.toRadians(i * 60.0 + 35.0).toFloat()
        val branch2Angle = Math.toRadians(i * 60.0 - 35.0).toFloat()

        drawLine(
            color = color,
            start = branchStart,
            end = Offset(
                x = branchStart.x + branchLength * cos(branch1Angle),
                y = branchStart.y + branchLength * sin(branch1Angle)
            ),
            strokeWidth = strokeWidth * 0.75f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = branchStart,
            end = Offset(
                x = branchStart.x + branchLength * cos(branch2Angle),
                y = branchStart.y + branchLength * sin(branch2Angle)
            ),
            strokeWidth = strokeWidth * 0.75f,
            cap = StrokeCap.Round
        )
    }
}

private fun rectFromCenterRadius(center: Offset, radius: Float): Rect {
    return Rect(
        left = center.x - radius,
        top = center.y - radius,
        right = center.x + radius,
        bottom = center.y + radius
    )
}

@Preview(showBackground = true)
@Composable
fun ColorfulWeatherIconGridPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ColorfulWeatherIcon(weatherCode = 0, isDay = true, isHappyMode = true, modifier = Modifier.size(64.dp))
            ColorfulWeatherIcon(weatherCode = 0, isDay = false, isHappyMode = true, modifier = Modifier.size(64.dp))
            ColorfulWeatherIcon(weatherCode = 1, isDay = true, isHappyMode = true, modifier = Modifier.size(64.dp))
            ColorfulWeatherIcon(weatherCode = 3, isDay = true, isHappyMode = true, modifier = Modifier.size(64.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
            ColorfulWeatherIcon(weatherCode = 45, isDay = true, isHappyMode = true, modifier = Modifier.size(64.dp))
            ColorfulWeatherIcon(weatherCode = 61, isDay = true, isHappyMode = true, modifier = Modifier.size(64.dp))
            ColorfulWeatherIcon(weatherCode = 95, isDay = true, isHappyMode = true, modifier = Modifier.size(64.dp))
            ColorfulWeatherIcon(weatherCode = 71, isDay = true, isHappyMode = true, modifier = Modifier.size(64.dp))
        }
    }
}
