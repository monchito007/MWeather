package com.monchito007.mweather.ui.weather

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import java.util.Locale

/**
 * Makes stringResource() and LocalContext.current.getString() inside [content] resolve
 * strings in the in-app [language], regardless of the device language. Switching
 * languages recomposes instantly, without recreating the Activity.
 */
@Composable
fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current

    val localizedContext = remember(language, baseContext, baseConfiguration) {
        val locale = Locale.forLanguageTag(language.code)
        val configuration = Configuration(baseConfiguration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        LocalizedContext(baseContext, baseContext.createConfigurationContext(configuration).resources)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        LocalResources provides localizedContext.resources,
        content = content
    )
}

/**
 * Wraps (rather than replaces) the Activity context, so code that walks the ContextWrapper
 * chain to find the Activity keeps working; only the resources are localized.
 */
private class LocalizedContext(
    base: Context,
    private val localizedResources: Resources
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
}
