package com.monchito007.mweather

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.monchito007.mweather.data.preferences.DataStoreUserPreferencesRepository
import com.monchito007.mweather.data.preferences.SavedLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FavoritesPreferencesTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.dataStore() = PreferenceDataStoreFactory.create(
        scope = backgroundScope,
        produceFile = { folder.root.resolve("prefs.preferences_pb") }
    )

    @Test
    fun favoritesSurviveASaveAndRead() = runTest {
        val repository = DataStoreUserPreferencesRepository(dataStore())
        val favorites = listOf(
            SavedLocation(41.39, 2.17, "Barcelona, Spain"),
            SavedLocation(68.97, 33.07, "Múrmansk, \"Rusia\"")
        )

        repository.saveFavorites(favorites)

        assertEquals(favorites, repository.preferences.first().favorites)
    }

    @Test
    fun unreadableFavoritesAreDroppedInsteadOfCrashing() = runTest {
        val store = dataStore()
        store.edit { it[stringPreferencesKey("favorite_cities")] = "{not a list" }

        val favorites = DataStoreUserPreferencesRepository(store).preferences.first().favorites

        assertTrue(favorites.isEmpty())
    }
}
