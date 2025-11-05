package com.navigine.naviginedemocompose.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.naviginedemocompose.domain.repository.DebugRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val repo: DebugRepository
): ViewModel() {

    private val expanded = MutableStateFlow(
        mapOf(
            DebugSectionKey.BEACONS to false,
            DebugSectionKey.BLE to false,
            DebugSectionKey.EDDYSTONE to false,
            DebugSectionKey.WIFI to false,
            DebugSectionKey.RTT to false
        )
    )

    val uiState: StateFlow<DebugState> = combine(
        repo.observeSnapshot(),
        expanded
    ) { snap, expandedMap ->
        DebugState(
            info = snap.info,
            beacons = snap.beacons,
            ble = snap.ble,
            eddystone = snap.eddystone,
            wifi = snap.wifi,
            rtt = snap.rtt,
            sensors = snap.sensors,
            expanded = expandedMap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugState())

    fun toggle(key: DebugSectionKey) {
        expanded.update { it + (key to !(it[key] ?: false)) }
    }
}