package com.navigine.naviginedemocompose.core.util

import com.navigine.idl.java.SignalMeasurement
import com.navigine.idl.java.SignalType
import kotlin.collections.filterNotNull
import kotlin.collections.orEmpty

object DebugSignalRenderer {

    fun renderByType(signals: Map<String, SignalMeasurement>): Map<SignalType, List<String>> {
        val list = signals.values.filterNotNull()
        val grouped = list.groupBy { it.type }

        return buildMap {
            put(SignalType.BEACON, grouped[SignalType.BEACON].toRows(::beaconRow))
            put(SignalType.BLUETOOTH, grouped[SignalType.BLUETOOTH].toRows(::bleRow))
            put(SignalType.EDDYSTONE, grouped[SignalType.EDDYSTONE].toRows(::eddyRow))
            put(SignalType.WIFI, grouped[SignalType.WIFI].toRows(::wifiRow))
            put(SignalType.WIFI_RTT, grouped[SignalType.WIFI_RTT].toRows(::rttRow))
        }.filterValues { it.isNotEmpty() }
    }

    // region Per-type rows

    private fun beaconRow(m: SignalMeasurement): String {
        val id = (m.id ?: "---").removeParens()
        val rssi = m.rssi.format(1)
        val dist = if (m.distance > 0) " ${m.distance.format(1)}m" else ""
        return "$id   $rssi$dist"
    }

    private fun bleRow(m: SignalMeasurement): String {
        val id = m.id ?: "---"
        return "$id   ${m.rssi.format(1)}"
    }

    private fun eddyRow(m: SignalMeasurement): String {
        val prettyId = (m.id ?: "---").let(::formatEddystoneIdSafe)
        val rssi = m.rssi.format(1)
        val dist = if (m.distance > 0) " ${m.distance.format(1)}m" else ""
        return "$prettyId   $rssi$dist"
    }

    private fun wifiRow(m: SignalMeasurement): String {
        val id = m.id?.takeIf { it.isNotBlank() } ?: "---"
        return "$id   ${m.rssi.format(1)}"
    }

    private fun rttRow(m: SignalMeasurement): String {
        val id = m.id?.takeIf { it.isNotBlank() }  ?: "---"
        return "$id   ${m.rssi.format(1)}"
    }

    private fun formatEddystoneIdSafe(raw: String): String {
        val clean = raw.removeParens()
        val parts = clean.split(',')
        if (parts.size < 2) return clean

        val p0 = parts[0]
        val p1 = parts[1]

        val target = (15 - p1.length).coerceIn(1, p0.length)
        val left = p0.take(target) + "…"
        val right = if (p1.length > 1) p1.dropLast(1) else p1
        return "$left, $right"
    }

    private fun String.removeParens(): String =
        replace("[()]".toRegex(), "")

    private inline fun List<SignalMeasurement>?.toRows(
        crossinline row: (SignalMeasurement) -> String
    ): List<String> = this.orEmpty()
        .sortedByDescending { it.rssi }
        .map(row)

}