package com.monchito007.mweather.ui.weather.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monchito007.mweather.domain.model.LocationSearchResult
import com.monchito007.mweather.ui.theme.MWeatherTheme
import com.monchito007.mweather.ui.weather.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    isActive: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    searchResults: List<LocationSearchResult>,
    isSearching: Boolean,
    onSelectLocation: (LocationSearchResult) -> Unit,
    modifier: Modifier = Modifier,
    language: AppLanguage = AppLanguage.SPANISH
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChanged,
                onSearch = { },
                expanded = isActive,
                onExpandedChange = onActiveChanged,
                placeholder = {
                    Text(text = if (language == AppLanguage.SPANISH) "Buscar ciudad..." else "Search city...")
                },
                leadingIcon = {
                    if (isActive) {
                        IconButton(onClick = { onActiveChanged(false) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = if (language == AppLanguage.SPANISH) "Atrás" else "Back"
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = if (language == AppLanguage.SPANISH) "Buscar" else "Search"
                        )
                    }
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = if (language == AppLanguage.SPANISH) "Limpiar" else "Clear"
                            )
                        }
                    }
                }
            )
        },
        expanded = isActive,
        onExpandedChange = onActiveChanged,
        modifier = modifier.fillMaxWidth()
    ) {
        if (searchResults.isEmpty() && query.trim().length >= 2 && !isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == AppLanguage.SPANISH) "No se encontraron ubicaciones" else "No locations found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(searchResults, key = { it.id }) { location ->
                    ListItem(
                        headlineContent = { Text(text = location.name) },
                        supportingContent = {
                            val details = listOfNotNull(location.state, location.country).joinToString(", ")
                            if (details.isNotBlank()) {
                                Text(text = details)
                            }
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.clickable {
                            onSelectLocation(location)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationSearchBarPreview() {
    MWeatherTheme {
        LocationSearchBar(
            query = "Madrid",
            onQueryChanged = {},
            isActive = true,
            onActiveChanged = {},
            searchResults = listOf(
                LocationSearchResult(1L, "Madrid", "España", "Madrid", 40.4168, -3.7038),
                LocationSearchResult(2L, "Madrid", "Estados Unidos", "Iowa", 41.8780, -93.8205)
            ),
            isSearching = false,
            onSelectLocation = {}
        )
    }
}
