package com.monchito007.mweather.ui.weather.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monchito007.mweather.ui.weather.AppLanguage
import com.monchito007.mweather.ui.weather.ThemeMode

@Composable
fun SettingsMenuButton(
    currentLanguage: AppLanguage,
    currentThemeMode: ThemeMode,
    isHappyMode: Boolean = false,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onToggleHappyMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = if (currentLanguage == AppLanguage.SPANISH) "Ajustes" else "Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        SettingsDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            currentLanguage = currentLanguage,
            currentThemeMode = currentThemeMode,
            isHappyMode = isHappyMode,
            onSelectLanguage = { lang ->
                onSelectLanguage(lang)
                expanded = false
            },
            onSelectThemeMode = { mode ->
                onSelectThemeMode(mode)
                expanded = false
            },
            onToggleHappyMode = {
                onToggleHappyMode()
                expanded = false
            }
        )
    }
}

@Composable
fun SettingsDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentLanguage: AppLanguage,
    currentThemeMode: ThemeMode,
    isHappyMode: Boolean = false,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onToggleHappyMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSpanish = currentLanguage == AppLanguage.SPANISH

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        // --- Sección de Idioma / Language Section ---
        Text(
            text = if (isSpanish) "Idioma" else "Language",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Option 1: Español
        DropdownMenuItem(
            text = {
                Text(text = "🇪🇸  Español")
            },
            onClick = { onSelectLanguage(AppLanguage.SPANISH) },
            trailingIcon = if (currentLanguage == AppLanguage.SPANISH) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null
        )

        // Option 2: English
        DropdownMenuItem(
            text = {
                Text(text = "🇬🇧  English")
            },
            onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
            trailingIcon = if (currentLanguage == AppLanguage.ENGLISH) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // --- Sección de Tema / Theme Section ---
        Text(
            text = if (isSpanish) "Modo de tema" else "Theme mode",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Option 1: Modo Claro / Light Mode
        DropdownMenuItem(
            text = {
                Text(text = if (isSpanish) "☀️  Modo claro" else "☀️  Light mode")
            },
            onClick = { onSelectThemeMode(ThemeMode.LIGHT) },
            trailingIcon = if (currentThemeMode == ThemeMode.LIGHT) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null
        )

        // Option 2: Modo Oscuro / Dark Mode
        DropdownMenuItem(
            text = {
                Text(text = if (isSpanish) "🌙  Modo oscuro" else "🌙  Dark mode")
            },
            onClick = { onSelectThemeMode(ThemeMode.DARK) },
            trailingIcon = if (currentThemeMode == ThemeMode.DARK) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null
        )

        // Option 3: Modo Sistema / System Default
        DropdownMenuItem(
            text = {
                Text(text = if (isSpanish) "📱  Predeterminado del sistema" else "📱  System default")
            },
            onClick = { onSelectThemeMode(ThemeMode.SYSTEM) },
            trailingIcon = if (currentThemeMode == ThemeMode.SYSTEM) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // --- Sección Modo Divertido / Happy Mode Section ---
        Text(
            text = if (isSpanish) "Estilo visual" else "Visual style",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        DropdownMenuItem(
            text = {
                Text(text = if (isSpanish) "😊  Modo divertido (Caras)" else "😊  Happy mode (Faces)")
            },
            onClick = { onToggleHappyMode() },
            trailingIcon = if (isHappyMode) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null
        )
    }
}
