package com.monchito007.mweather.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Entry point the system talks to (declared in the manifest). */
class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()

    /** Periodic update from the system (every 30 min): reload data even if a session is open. */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WeatherWidget.requestRefresh()
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}
