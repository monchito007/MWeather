package com.monchito007.mweather.ui.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.monchito007.mweather.MWeatherApplication
import com.monchito007.mweather.MainActivity
import com.monchito007.mweather.R
import com.monchito007.mweather.domain.model.Weather
import com.monchito007.mweather.ui.weather.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.util.Locale

/**
 * Home-screen widget with the current weather, plus the next 5 days when it's wide enough.
 *
 * It shows the forecast cached by the app, so it works offline. When that forecast is older
 * than [REFRESH_AFTER_MILLIS] it first downloads a new one for the same place; the system
 * asks for an update every 30 minutes (see res/xml/weather_widget_info.xml).
 */
class WeatherWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, WIDE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val firstRequest = refreshRequests.value
        val firstContent = loadContent(context)

        provideContent {
            // Glance keeps this session alive for a while after drawing. An update requested
            // meanwhile only recomposes, it doesn't call provideGlance again, so new data
            // must be loaded from here: every refresh request reloads it.
            val request by refreshRequests.collectAsState()
            val content by produceState(firstContent, request) {
                if (request != firstRequest) value = loadContent(context)
            }
            GlanceTheme {
                WidgetContent(content.model, content.noDataText)
            }
        }
    }

    private class Content(val model: WidgetUiModel?, val noDataText: String)

    /**
     * Reads the cached forecast and the display settings. When the forecast is older than
     * [REFRESH_AFTER_MILLIS] it downloads a new one for the same place first; offline, the
     * cached forecast is shown as it is, with its age.
     */
    private suspend fun loadContent(context: Context): Content {
        val container = (context.applicationContext as MWeatherApplication).container
        val preferences = container.userPreferencesRepository.preferences.first()
        val repository = container.weatherRepository

        var weather = repository.getCachedWeather()
        val cached = weather
        if (cached != null && needsRefresh(cached, System.currentTimeMillis())) {
            repository.getWeather(cached.latitude, cached.longitude, cached.locationName)
                .onSuccess { weather = it }
        }

        val localized = context.localized(preferences.language)
        return Content(
            model = weather?.toWidgetUiModel(localized, preferences.unit, System.currentTimeMillis()),
            noDataText = localized.getString(R.string.widget_no_data)
        )
    }

    companion object {
        private val SMALL = DpSize(110.dp, 110.dp)
        private val WIDE = DpSize(250.dp, 110.dp)

        const val REFRESH_AFTER_MILLIS = 30 * 60 * 1000L

        /** Bumped on every refresh request, so a still-open session reloads its data. */
        private val refreshRequests = MutableStateFlow(0)

        /** Marks the data as changed; open widget sessions reload it. */
        fun requestRefresh() {
            refreshRequests.update { it + 1 }
        }

        fun needsRefresh(weather: Weather, nowMillis: Long): Boolean {
            val fetchedAt = weather.fetchedAtMillis ?: return true
            return nowMillis - fetchedAt >= REFRESH_AFTER_MILLIS
        }

        /** Redraws every placed widget, e.g. after new data or a settings change in the app. */
        suspend fun updateAll(context: Context) {
            requestRefresh()
            val manager = GlanceAppWidgetManager(context)
            if (manager.getGlanceIds(WeatherWidget::class.java).isNotEmpty()) {
                WeatherWidget().updateAll(context)
            }
        }
    }
}

/** Widgets don't follow the app's in-app language on their own: build a context for it. */
private fun Context.localized(language: AppLanguage): Context {
    val configuration = Configuration(resources.configuration).apply {
        setLocale(Locale.forLanguageTag(language.code))
    }
    return createConfigurationContext(configuration)
}

@Composable
private fun WidgetContent(model: WidgetUiModel?, noDataText: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(20.dp)
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (model == null) {
            Text(
                text = noDataText,
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp)
            )
        } else if (LocalSize.current.width >= 250.dp && model.days.isNotEmpty()) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CurrentWeather(model, modifier = GlanceModifier.defaultWeight())
                Spacer(modifier = GlanceModifier.width(8.dp))
                model.days.forEach { day -> DayColumn(day) }
            }
        } else {
            CurrentWeather(model)
        }
    }
}

@Composable
private fun CurrentWeather(model: WidgetUiModel, modifier: GlanceModifier = GlanceModifier) {
    Column(modifier = modifier) {
        Text(
            text = model.locationName,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = model.emoji, style = TextStyle(fontSize = 28.sp))
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = model.temperature,
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Text(
            text = model.description,
            maxLines = 1,
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 12.sp)
        )
        Text(
            text = model.highLow,
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
        )
        if (model.updatedAgo.isNotEmpty()) {
            Text(
                text = model.updatedAgo,
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp)
            )
        }
    }
}

@Composable
private fun DayColumn(day: WidgetDay) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = GlanceModifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = day.label,
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp)
        )
        Text(text = day.emoji, style = TextStyle(fontSize = 18.sp))
        Text(
            text = day.maxTemp,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = day.minTemp,
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
        )
    }
}
