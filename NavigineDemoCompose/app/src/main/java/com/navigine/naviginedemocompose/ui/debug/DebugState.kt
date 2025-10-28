package com.navigine.naviginedemocompose.ui.debug

import androidx.compose.runtime.Immutable
import com.navigine.naviginedemocompose.domain.model.InfoRow
import com.navigine.naviginedemocompose.domain.model.Section

/** Keys to keep expand/collapse state stable across updates. */
enum class DebugSectionKey { BEACONS, BLE, EDDYSTONE, WIFI, RTT }

@Immutable
data class DebugState(
    val info: List<InfoRow> = emptyList(),
    val beacons: Section = Section("BEACONS", 0, 0f, emptyList()),
    val ble: Section = Section("BLE devices", 0, 0f, emptyList()),
    val eddystone: Section = Section("EDDYSTONE", 0, 0f, emptyList()),
    val wifi: Section = Section("Wi-Fi", 0, 0f, emptyList()),
    val rtt: Section = Section("Wi-Fi RTT", 0, 0f, emptyList()),
    val sensors: Section = Section("SENSORS", 0, 0f, emptyList()),
    val expanded: Map<DebugSectionKey, Boolean> = emptyMap()
) {
    fun isExpanded(key: DebugSectionKey): Boolean = expanded[key] == true
}