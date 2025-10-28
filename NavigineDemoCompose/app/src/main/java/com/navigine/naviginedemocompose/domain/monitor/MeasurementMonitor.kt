package com.navigine.naviginedemocompose.domain.monitor

import com.navigine.idl.java.SensorMeasurement
import com.navigine.idl.java.SensorType
import com.navigine.idl.java.SignalMeasurement
import kotlinx.coroutines.flow.Flow

interface MeasurementMonitor {
    val signals : Flow<Map<String, SignalMeasurement>>
    val sensors : Flow<Map<SensorType, SensorMeasurement>>
}