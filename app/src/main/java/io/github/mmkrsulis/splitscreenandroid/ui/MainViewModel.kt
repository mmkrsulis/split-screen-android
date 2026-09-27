// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.mmkrsulis.splitscreenandroid.SplitScreenApplication
import io.github.mmkrsulis.splitscreenandroid.data.PairRepository
import io.github.mmkrsulis.splitscreenandroid.model.AppPair
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeState(val pairs: List<AppPair> = emptyList(), val message: String? = null)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: PairRepository = (app as SplitScreenApplication).repository
    private val message = MutableStateFlow<String?>(null)
    val state = combine(repo.pairs, message, ::HomeState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun save(pair: AppPair) = viewModelScope.launch {
        repo.save(pair)
        message.value = "Saved ${pair.name}"
    }

    fun delete(id: String) = viewModelScope.launch {
        if (repo.delete(id)) {
            io.github.mmkrsulis.splitscreenandroid.shortcuts.ShortcutSupport(getApplication()).disable(id)
            message.value = "Pair deleted"
        }
    }

    fun favorite(pair: AppPair) = viewModelScope.launch { repo.save(pair.copy(favorite = !pair.favorite)) }
    fun recordLaunchAttempt(id: String) = viewModelScope.launch { repo.markLaunched(id) }
    fun clearMessage() { message.value = null }
}
