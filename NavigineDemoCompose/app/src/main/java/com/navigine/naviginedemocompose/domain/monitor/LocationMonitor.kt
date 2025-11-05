package com.navigine.naviginedemocompose.domain.monitor

import com.navigine.naviginedemocompose.domain.model.LocationEvent
import com.navigine.naviginedemocompose.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

interface LocationMonitor {

    val events: Flow<LocationEvent>

    val currentLocation: Flow<LocationModel?>

    /**
     * wrapper: key(mapRecomposeKey) { NavigineLocation(...) }.
     */
    val mapRecomposeKey: Flow<Int>
}