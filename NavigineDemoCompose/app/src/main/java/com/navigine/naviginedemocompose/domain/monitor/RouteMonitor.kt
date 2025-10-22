package com.navigine.naviginedemocompose.domain.monitor

import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.RouteStatus
import com.navigine.idl.java.Venue
import kotlinx.coroutines.flow.Flow

interface RouteMonitor {

    val events: Flow<RouteMonitorEvent>

    suspend fun startRouteTo(target: LocationPoint)

    suspend fun startRouteTo(venue: Venue)

    suspend fun cancel()
}

sealed interface RouteMonitorEvent {
    data class Changed(val status: RouteStatus, val path: RoutePath?) : RouteMonitorEvent

    data class Advanced(val progress: Float, val point : LocationPoint) : RouteMonitorEvent

    data object Cancelled : RouteMonitorEvent
}