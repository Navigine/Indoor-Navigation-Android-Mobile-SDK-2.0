package com.navigine.naviginedemocompose.domain.model

import androidx.compose.runtime.Immutable

/** Row in the top info card. */
@Immutable
data class InfoRow(val title: String, val value: String)

/** Simple text row for signals/sensors. */
@Immutable
data class TextRow(val text: String)

@Immutable
data class Section(
    val title: String,
    val counter: Int,
    val entriesPerSec: Float,
    val rows: List<TextRow>
)

/** Aggregate snapshot consumed by the UI. */
@Immutable
data class DebugSnapshot(
    val info: List<InfoRow> = emptyList(),
    val beacons: Section = Section("BEACONS", 0, 0f, emptyList()),
    val ble: Section = Section("BLE devices", 0, 0f, emptyList()),
    val eddystone: Section = Section("EDDYSTONE", 0, 0f, emptyList()),
    val wifi: Section = Section("Wi-Fi", 0, 0f, emptyList()),
    val rtt: Section = Section("Wi-Fi RTT", 0, 0f, emptyList()),
    val sensors: Section = Section("SENSORS", 0, 0f, emptyList()),
    val errors: List<String> = emptyList()
)