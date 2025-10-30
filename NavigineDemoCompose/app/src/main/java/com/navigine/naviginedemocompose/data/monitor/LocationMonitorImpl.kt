package com.navigine.naviginedemocompose.data.monitor

import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.naviginedemocompose.core.log.AppLogger
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.domain.model.LocationEvent
import com.navigine.naviginedemocompose.domain.model.LocationModel
import com.navigine.naviginedemocompose.domain.monitor.LocationMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationMonitorImpl @Inject constructor(
    private val sdk: NavigineSdkManager,
    log: AppLogger
) : LocationMonitor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val counter = AtomicInteger(0)

    private val sdkEvents: Flow<LocationEvent> =
        sdk.state.flatMapLatest { st ->
            if (st is NavigineSdkManager.SdkState.Ready) {
                callbackFlow {
                    val listener = object : LocationListener() {
                        override fun onLocationLoaded(location: Location) {
                            trySend(
                                LocationEvent.Loaded(
                                    LocationModel(
                                        id = location.id,
                                        name = location.name,
                                        location = location
                                    )
                                )
                            )
                        }

                        override fun onLocationUploaded(var1: Int) {
                            trySend(LocationEvent.Uploaded(var1))
                        }

                        override fun onLocationFailed(code: Int, error: Error) {
                            trySend(LocationEvent.Failed(code, error.message ?: "Unknown error"))
                            log.nonFatal(error, mapOf("where" to "loc_monitor"))
                        }
                    }
                    sdk.locationManager.addLocationListener(listener)

                    awaitClose { sdk.locationManager.removeLocationListener(listener) }
                }
            } else emptyFlow()
    }

    override val events: SharedFlow<LocationEvent> =
        sdkEvents.shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 5_000,
                replayExpirationMillis = 0
            ),
            replay = 0
        )

    override val currentLocation: StateFlow<LocationModel?> =
        events.filterIsInstance<LocationEvent.Loaded>()
            .map { it.location }
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                initialValue = runCatching {
                sdk.locationManager.locationId }.getOrNull()?.takeIf { it > 0 }
                ?.let { LocationModel(it, "") }
            )

    override val mapRecomposeKey: StateFlow<Int> =
        events.filterIsInstance<LocationEvent.Loaded>()
            .map { counter.incrementAndGet() }
            .stateIn(scope, SharingStarted.Eagerly, 0)


}