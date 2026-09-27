// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.mmkrsulis.splitscreenandroid.model.AppPair
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.store by preferencesDataStore("split_screen")

class DataStorePairRepository(private val context: Context, scope: CoroutineScope) : PairRepository {
    private val key = stringPreferencesKey("pairs_json")
    private val json = Json { ignoreUnknownKeys = true }

    override val pairs: StateFlow<List<AppPair>> = context.store.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map(::decodeAndSort)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun save(pair: AppPair): AppPair {
        val value = pair.copy(updatedAt = System.currentTimeMillis())
        context.store.edit { preferences ->
            val list = decode(preferences)
            preferences[key] = json.encodeToString(list.filterNot { it.id == value.id } + value)
        }
        return value
    }

    override suspend fun delete(id: String): Boolean {
        var removed = false
        context.store.edit { preferences ->
            val list = decode(preferences)
            removed = list.any { it.id == id }
            if (removed) preferences[key] = json.encodeToString(list.filterNot { it.id == id })
        }
        return removed
    }

    override suspend fun find(id: String): AppPair? = decode(context.store.data.first()).find { it.id == id }

    override suspend fun markLaunched(id: String, at: Long): Boolean {
        var found = false
        context.store.edit { preferences ->
            val list = decode(preferences)
            val updated = list.map { pair ->
                if (pair.id == id) {
                    found = true
                    pair.copy(lastUsedAt = at, updatedAt = System.currentTimeMillis())
                } else pair
            }
            if (found) preferences[key] = json.encodeToString(updated)
        }
        return found
    }

    private fun decode(preferences: Preferences): List<AppPair> {
        val encoded = preferences[key] ?: return emptyList()
        try {
            return json.decodeFromString(encoded)
        } catch (error: SerializationException) {
            throw CorruptPairDataException(error)
        } catch (error: IllegalArgumentException) {
            throw CorruptPairDataException(error)
        }
    }

    private fun decodeAndSort(preferences: Preferences): List<AppPair> = sort(decode(preferences))

    private fun sort(values: List<AppPair>) = values.sortedWith(
        compareByDescending<AppPair> { it.favorite }
            .thenByDescending { it.lastUsedAt ?: Long.MIN_VALUE }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name },
    )
}

class CorruptPairDataException(cause: Throwable) : IllegalStateException("Saved app-pair data is corrupt", cause)
