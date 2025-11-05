package com.navigine.naviginedemocompose.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.naviginedemocompose.domain.repository.LocationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val repo: LocationsRepository
): ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedId = MutableStateFlow<Int?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val locations = repo.observeLocations()
        .onEach { _isRefreshing.value = false }
        .catch { _error.value = it.message }

    val state: StateFlow<LocationsState> = combine(
        _query, _selectedId, _isRefreshing, _error, locations
    ) { q, sel, refreshing, err, list ->
        val filtered = if (q.isBlank()) list
        else list.filter { it.name.contains(q, ignoreCase = true) }
        LocationsState(query = q, items = filtered, selectedId = sel, isRefreshing = refreshing, error = err)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocationsState())

    init {
        _selectedId.value = repo.getSelectedLocationId()
    }

    fun onQueryChange(q: String) { _query.value = q }

    fun onSelect(id: Int) {
        if (isCurrentLocation(id)) return
        viewModelScope.launch {
            _selectedId.update { if (it == id) null else id }
            repo.selectLocation(id)
        }
    }

    fun refresh() = viewModelScope.launch {
        _isRefreshing.value = true
        runCatching { repo.refresh() }
            .onFailure { _error.value = it.message; _isRefreshing.value = false }
    }

    private fun isCurrentLocation(locationId : Int) : Boolean = repo.isCurrentLocation(locationId)

}