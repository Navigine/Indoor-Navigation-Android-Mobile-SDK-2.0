package com.navigine.naviginedemocompose.data.monitor

import com.navigine.idl.java.AsyncRouteListener
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.RouteOptions
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.RouteSession
import com.navigine.idl.java.RouteStatus
import com.navigine.idl.java.Venue
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.domain.monitor.RouteMonitor
import com.navigine.naviginedemocompose.domain.monitor.RouteMonitorEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class RouteMonitorImpl @Inject constructor(
    private val sdk: NavigineSdkManager
) : RouteMonitor {

    private val _events = MutableSharedFlow<RouteMonitorEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events: SharedFlow<RouteMonitorEvent> = _events

    private val sessionMutex = Mutex()
    private var session: RouteSession? = null
    private var listener : AsyncRouteListener? = null
    private var currentCollectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override suspend fun startRouteTo(
        target: LocationPoint
    ) {
        startRouteInternal(target)
    }

    override suspend fun startRouteTo(
        venue: Venue
    ) {
        val lp = LocationPoint(venue.point,venue.locationId, venue.sublocationId)
        startRouteInternal(lp)
    }

    override suspend fun cancel() {
        sessionMutex.withLock {
            cancelInternal()
            emit(RouteMonitorEvent.Cancelled)
        }
    }

    private fun cancelInternal() {
        currentCollectJob?.cancel()
        currentCollectJob = null

        val s = session ?: return
        val l = listener ?: return
        runCatching {
            sdk.asyncRouteManager.cancelRouteSession(s)
        }
        runCatching {
            s.removeRouteListener(l)
        }
        session = null
        listener = null
    }

    private suspend fun startRouteInternal(target : LocationPoint){
        sessionMutex.withLock {
            cancelInternal()

            val state = sdk.state.value
            require(state is NavigineSdkManager.SdkState.Ready) {
                "SDK is not ready; cannot start route"
            }

            val routeFlow = callbackFlow {
                listener = object : AsyncRouteListener(){
                    override fun onRouteChanged(
                        status: RouteStatus, path: RoutePath?
                    ) {
                        trySend(RouteMonitorEvent.Changed(status, path))
                    }

                    override fun onRouteAdvanced(
                        progress: Float, point: LocationPoint
                    ) {
                        trySend(RouteMonitorEvent.Advanced(progress, point))
                    }
                }
                val ses = sdk.asyncRouteManager.createRouteSession(target, buildRouteOptions()).also {
                    it.addRouteListener(listener)
                }
                session = ses
                awaitClose {
                    runCatching { ses.removeRouteListener(listener) }
                    runCatching { sdk.asyncRouteManager.cancelRouteSession(ses) }
                    if (session === ses) session = null
                }
            }

            currentCollectJob = routeFlow
                .onEach { _events.emit(it) }
                .launchIn(scope)
        }
    }

    private fun emit(event: RouteMonitorEvent) {
        _events.tryEmit(event)
    }

    private fun buildRouteOptions() =  RouteOptions(0.0, 3.0, 2.0)

}