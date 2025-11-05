package com.navigine.naviginedemocompose.data.repository

import android.content.Context
import android.os.Build
import com.navigine.idl.java.Position
import com.navigine.idl.java.SensorMeasurement
import com.navigine.idl.java.SensorType
import com.navigine.idl.java.SignalType
import com.navigine.naviginedemocompose.BuildConfig
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.core.util.DebugSignalRenderer
import com.navigine.naviginedemocompose.core.util.fmtTime
import com.navigine.naviginedemocompose.core.util.format
import com.navigine.naviginedemocompose.domain.model.DebugSnapshot
import com.navigine.naviginedemocompose.domain.model.InfoRow
import com.navigine.naviginedemocompose.domain.model.Section
import com.navigine.naviginedemocompose.domain.model.TextRow
import com.navigine.naviginedemocompose.domain.monitor.LocationMonitor
import com.navigine.naviginedemocompose.domain.monitor.MeasurementMonitor
import com.navigine.naviginedemocompose.domain.monitor.PositionMonitor
import com.navigine.naviginedemocompose.domain.monitor.SystemSettingsMonitor
import com.navigine.naviginedemocompose.domain.repository.DebugRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class DebugRepositoryImpl @Inject constructor(
    private val sdk: NavigineSdkManager,
    private val locationMonitor: LocationMonitor,
    private val positionMonitor: PositionMonitor,
    private val measurementMonitor: MeasurementMonitor,
    private val systemSettingsMonitor: SystemSettingsMonitor,
    @ApplicationContext private val app: Context
) : DebugRepository {

    override fun observeSnapshot(): Flow<DebugSnapshot> {
        val posFlow = positionMonitor.positions
        val locFlow = locationMonitor.currentLocation
        val sigFlow = measurementMonitor.signals
        val senFlow = measurementMonitor.sensors
        val btFlow = systemSettingsMonitor.bluetoothEnabled
        val locEnFlow = systemSettingsMonitor.locationEnabled

        return combine(
            combine(posFlow, locFlow) { pos, loc -> pos to loc },
            combine(sigFlow, senFlow) { sig, sen -> sig to sen },
            combine(btFlow, locEnFlow) { bt, loc -> bt to loc },
        ) {
            (pos, loc),
            (sig, sen),
            (bt, locEn) ->

            val locationName = runCatching { loc?.name ?: "---" }
                .getOrDefault(loc?.location?.name ?: "---")

            val info = buildInfo(locationName, pos, bt, locEn)
            val sensors = renderSensors(sen)
            val signals = DebugSignalRenderer.renderByType(sig)

            fun List<String>.toSection(title: String) = Section(
                title = title,
                counter = this.size,
                entriesPerSec = this.size.toFloat(),
                rows = this.map { TextRow(it) }
            )

            DebugSnapshot(
                info = info,
                sensors = sensors.toSection("SENSORS"),
                beacons = signals[SignalType.BEACON].orEmpty().toSection("BEACONS"),
                ble = signals[SignalType.BLUETOOTH].orEmpty().toSection("BLE devices"),
                eddystone = signals[SignalType.EDDYSTONE].orEmpty().toSection("EDDYSTONE"),
                wifi = signals[SignalType.WIFI].orEmpty().toSection("Wi-Fi"),
                rtt = signals[SignalType.WIFI_RTT].orEmpty().toSection("Wi-Fi RTT")
            )

        }
    }

    private fun buildInfo(
        locationName: String,
        position: Position?,
        btOn: Boolean,
        locOn: Boolean
    ): List<InfoRow> {
        val osCode = Build.VERSION.SDK_INT
        val osName = Build.VERSION_CODES::class.java.fields
            .firstOrNull { it.getInt(Build.VERSION_CODES::class.java) == osCode }?.name ?: "UNKNOWN"

        val positionStr = position?.let { pos ->
            val lp = pos.locationPoint
            val xyz = lp?.let {
                "${it.locationId}/${it.sublocationId}, x=${it.point.x.format(1)}, y=${
                    it.point.y.format(1)
                }"
            }
                ?: "--/--, lat=${pos.point.latitude.format(1)}, lon=${pos.point.longitude.format(1)}"
            "$xyz  (${System.currentTimeMillis().fmtTime()})"
        } ?: "---"

        return listOf(
            InfoRow("App version", BuildConfig.VERSION_NAME),
            InfoRow("Device ID", sdk.getDeviceId()),
            InfoRow("Location", locationName),
            InfoRow("Position", positionStr),
            InfoRow("Bluetooth", if (btOn) "on" else "off"),
            InfoRow("Geolocation", if (locOn) "on" else "off"),
            InfoRow("Device model", "${Build.MODEL} [ ${Build.VERSION.RELEASE} ($osName) ]")
        )
    }

    private fun renderSensors(map: Map<SensorType, SensorMeasurement>): List<String> {
        fun SensorMeasurement.f3() =
            "${values.x.format(4)}, ${values.y.format(4)}, ${values.z.format(4)}"

        fun SensorMeasurement.f1() = values.x.format(2)
        return listOf(
            "Accelerometer" to map[SensorType.ACCELEROMETER]?.f3(),
            "Magnetometer" to map[SensorType.MAGNETOMETER]?.f3(),
            "Gyroscope" to map[SensorType.GYROSCOPE]?.f3(),
            "Barometer" to map[SensorType.BAROMETER]?.f1(),
            "Orientation" to map[SensorType.ORIENTATION]?.let {
                (it.values.x * 180 / Math.PI).format(
                    2
                )
            }
        ).map { (name, v) -> "$name    ${v ?: "---"}" }
    }

}