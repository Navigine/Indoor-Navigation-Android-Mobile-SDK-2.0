package com.navigine.naviginedemocompose.data.monitor

import com.navigine.idl.java.MeasurementListener
import com.navigine.idl.java.MeasurementManager
import com.navigine.idl.java.SensorMeasurement
import com.navigine.idl.java.SensorType
import com.navigine.idl.java.SignalMeasurement
import com.navigine.naviginedemocompose.core.log.AppLogger
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.domain.monitor.MeasurementMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.HashMap
import javax.inject.Inject

class MeasurementMonitorImpl @Inject constructor(
    private val sdk: NavigineSdkManager,
    private val log: AppLogger
) : MeasurementMonitor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mm: MeasurementManager? = null
    private var listener: MeasurementListener? = null

    private val _signals = MutableSharedFlow<Map<String, SignalMeasurement>>(replay = 1, extraBufferCapacity = 32)
    private val _sensors = MutableSharedFlow<Map<SensorType, SensorMeasurement>>(replay = 1, extraBufferCapacity = 32)

    override val signals: Flow<Map<String, SignalMeasurement>> = _signals.asSharedFlow()
    override val sensors: Flow<Map<SensorType, SensorMeasurement>> = _sensors.asSharedFlow()

    init {
        scope.launch {
            sdk.state.collect { state ->
                when (state) {
                    is NavigineSdkManager.SdkState.Ready -> attach()
                    else -> detach()
                }
            }
        }
    }

    @Synchronized
    private fun attach(){
        if (listener != null) return
        val manager = runCatching { sdk.measurementManager }.getOrNull() ?: return

        val l = object : MeasurementListener() {
            override fun onSensorMeasurementDetected(map: HashMap<SensorType, SensorMeasurement>) {
                _sensors.tryEmit(map )
            }

            override fun onSignalMeasurementDetected(map: HashMap<String, SignalMeasurement>) {
                _signals.tryEmit(map )
            }
        }
        runCatching {
            manager.addMeasurementListener(l)
            mm = manager
        }
            .onFailure { e -> log.nonFatal(e, mapOf("where" to "measurement_attach")) }
        listener = l
    }

    @Synchronized
    private fun detach() {
        val l = listener ?: return
        runCatching { sdk.measurementManager.removeMeasurementListener(l) }
        listener = null
        mm = null
    }

}