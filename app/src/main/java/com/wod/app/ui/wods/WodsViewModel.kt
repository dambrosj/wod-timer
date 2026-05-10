package com.wod.app.ui.wods

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wod.app.WodApp
import com.wod.app.domain.model.SavedWod
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

import kotlinx.serialization.encodeToString

class WodsViewModel(app: Application) : AndroidViewModel(app) {

    private val wodApp = app as WodApp
    private val jsonDecoder = Json { ignoreUnknownKeys = true }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow<TimerType?>(null)

    val searchQuery: StateFlow<String> = _searchQuery
    val selectedType: StateFlow<TimerType?> = _selectedType

    private val allWods = wodApp.savedWodRepository.allFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredWods: StateFlow<List<SavedWod>> = combine(allWods, _searchQuery, _selectedType)
    { wods, query, type ->
        wods.filter { wod ->
            (type == null || wod.type == type) &&
            (query.isBlank() || wod.name.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setTypeFilter(t: TimerType?) { _selectedType.value = t }

    fun toggleFavourite(wod: SavedWod) {
        viewModelScope.launch { wodApp.savedWodRepository.setFavourite(wod.id, !wod.isFavourite) }
    }

    fun duplicate(wod: SavedWod) {
        viewModelScope.launch {
            val copy = wod.copy(
                id          = "user_${System.currentTimeMillis()}",
                name        = "${wod.name} (copia)",
                isBuiltIn   = false,
                createdAt   = System.currentTimeMillis(),
                lastUsedAt  = null,
                timesUsed   = 0,
            )
            wodApp.savedWodRepository.save(copy)
        }
    }

    fun editWod(wod: SavedWod, newName: String, newDescription: String) {
        if (wod.isBuiltIn) return
        viewModelScope.launch {
            wodApp.savedWodRepository.save(
                wod.copy(name = newName.trim(), description = newDescription.trim())
            )
        }
    }

    fun delete(wod: SavedWod) {
        viewModelScope.launch { wodApp.savedWodRepository.delete(wod.id) }
    }

    fun selectWod(wod: SavedWod) { wodApp.selectedWod = wod }

    /** Store wod in selectedWod so the edit screen can read it, then navigate. */
    fun startEditWod(wod: SavedWod, onNavigate: (wodId: String) -> Unit) {
        wodApp.selectedWod = wod
        onNavigate(wod.id)
    }

    /** Persist the updated config JSON back to the saved WOD. */
    fun updateWodConfig(wod: SavedWod, newConfig: TimerConfig) {
        if (wod.isBuiltIn) return
        viewModelScope.launch {
            wodApp.savedWodRepository.save(
                wod.copy(configJson = jsonDecoder.encodeToString<TimerConfig>(newConfig))
            )
        }
    }

    /** Decode configJson, store as pendingConfig, increment usage, then navigate. */
    fun startWod(wod: SavedWod, onNavigate: (typeName: String) -> Unit) {
        viewModelScope.launch {
            runCatching { jsonDecoder.decodeFromString<TimerConfig>(wod.configJson) }
                .onSuccess { config ->
                    wodApp.pendingConfig = config
                    wodApp.savedWodRepository.incrementUsage(wod.id)
                    withContext(Dispatchers.Main) { onNavigate(wod.type.name) }
                }
        }
    }
}
