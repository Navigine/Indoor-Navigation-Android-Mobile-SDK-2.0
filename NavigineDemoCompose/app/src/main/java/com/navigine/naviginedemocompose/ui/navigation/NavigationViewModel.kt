package com.navigine.naviginedemocompose.ui.navigation

import android.graphics.PointF
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.idl.java.GlobalPoint
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Point
import com.navigine.idl.java.Polyline
import com.navigine.idl.java.Position
import com.navigine.idl.java.RouteLayerStatus
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.RouteStatus
import com.navigine.idl.java.Sublocation
import com.navigine.idl.java.Venue
import com.navigine.naviginedemocompose.core.log.AppLogger
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.domain.model.LocationModel
import com.navigine.naviginedemocompose.domain.monitor.LocationMonitor
import com.navigine.naviginedemocompose.domain.monitor.PositionMonitor
import com.navigine.naviginedemocompose.domain.monitor.RouteMonitor
import com.navigine.naviginedemocompose.domain.monitor.RouteMonitorEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val locationMonitor: LocationMonitor,
    private val positionMonitor: PositionMonitor,
    private val sdk: NavigineSdkManager,
    private val savedStateHandle: SavedStateHandle,
    private val log: AppLogger
) : ViewModel() {

    companion object {
        private const val WALK_SPEED_MPS = 1.4f
        private const val KEY_INITIAL_SUBLOCATION = "initial_subloc"
        private const val KEY_INITIAL_VENUE = "initial_venue_id"
    }

    val mapRecomposeKey = locationMonitor.mapRecomposeKey.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0
    )
    val currentLocation = locationMonitor.currentLocation

    private var _state = MutableStateFlow(NavigationState())
    val state = _state.asStateFlow()

    private val _effects = Channel<NavigationEffect>(capacity = Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        reduce { it.copy(loadingVisible = true) }

        readQrParams()

        sdk.state
            .onEach { st ->
                val ready = st is NavigineSdkManager.SdkState.Ready
                reduce { it.copy(sdkReady = ready, loadingVisible = !ready) }
            }
            .launchIn(viewModelScope)

        mapRecomposeKey
            .onEach { k -> reduce { it.copy(isGlobalMapEnabled = false, mapKey = k) } }
            .launchIn(viewModelScope)

        locationMonitor.currentLocation.onEach { loc ->
            if (loc != null)
                onEvent(NavigationEvent.LocationLoaded(loc))
        }.launchIn(viewModelScope)

        positionMonitor.positions.onEach { pos ->
            onEvent(NavigationEvent.PositionUpdated(pos))
        }.launchIn(viewModelScope)

    }

    fun onEvent(event: NavigationEvent) {
        when (event) {
            NavigationEvent.Init -> onInit()
            is NavigationEvent.LocationLoaded -> onLocationLoaded(event.location)
            is NavigationEvent.SwitchFloor -> onSwitchFloor(event.sublocationId)

            NavigationEvent.MapTapped -> onMapTapped()
            is NavigationEvent.LongPressAt -> onLongPress(event.global, event.sublocationId)
            is NavigationEvent.VenueFeaturePicked -> onVenueFeaturePicked(
                event.venueName,
                event.sublocationId
            )

            NavigationEvent.CancelPin -> onCancelPin()
            is NavigationEvent.VenuePickedOnMap -> onVenuePicked(event.venue)

            is NavigationEvent.PositionUpdated -> onPositionUpdated(event.snapshot)
            is NavigationEvent.FollowMyLocationToggle -> reduce { it.copy(followMyLocation = event.enabled) }


            NavigationEvent.BuildRoute -> onBuildRoute()
            NavigationEvent.CancelRoute -> onCancelRoute()
            is NavigationEvent.RouteChanged -> onRouteChanged(event.status, event.lengthMeters)
            is NavigationEvent.RouteAdvanced -> onRouteAdvanced(
                event.remainingMeters,
                event.remainingSeconds
            )

            NavigationEvent.RouteTargetReached -> onRouteTargetReached()


            NavigationEvent.HideMakeRouteSheet -> reduce { it.copy(makeRouteSheetVisible = false) }
            NavigationEvent.HideRouteInfo -> reduce { it.copy(routeInfoVisible = false) }
            NavigationEvent.HideFinish -> reduce { it.copy(isFinishNear = false) }
            NavigationEvent.HideVenueSheet -> reduce { it.copy(venueSheet = null) }
            NavigationEvent.OnRouteVenue -> onRouteVenue()
            NavigationEvent.HideMapSettings -> reduce { it.copy(mapSettingsSheetVisible = false) }
            NavigationEvent.ShowMapSettings -> reduce { it.copy(mapSettingsSheetVisible = true) }

            is NavigationEvent.MapSettingsChanged -> reduce { it.copy(locationUiSettings = event.settings) }
            is NavigationEvent.GlobalMapToggled -> reduce { it.copy(isGlobalMapEnabled = event.enabled) }
        }
    }

    private fun onInit() {
        val ready = sdk.state.value is NavigineSdkManager.SdkState.Ready
        reduce { it.copy(sdkReady = ready, loadingVisible = !ready) }
    }

    private fun onLocationLoaded(model: LocationModel) {
        reduce { st ->
            val sdkLocation = model.location
            val requested = st.requestedSublocationId
                ?.takeIf { id -> sdkLocation?.getSublocationById(id) != null }
            st.copy(
                location = model,
                requestedSublocationId = requested ?: sdkLocation?.sublocations?.firstOrNull()?.id,
                loadingVisible = false
            )
        }
    }

    private fun onSwitchFloor(id: Int) {
        if (_state.value.followMyLocation) {
            emitEffect(NavigationEffect.ShowToast("Turn of follow mode to change floor"))
            return
        }
        showSublocation(id)
    }

    private fun showSublocation(id: Int) {
        reduce { it.copy(requestedSublocationId = id) }
    }

    private fun onMapTapped() {
        if (_state.value.isRouting) return
        onCancelPin()
    }

    private fun onLongPress(global: GlobalPoint?, sublocationId: Int) {
        val prev = _state.value
        if (prev.isRouting || global == null) return
        val from = prev.position?.locationPoint ?: return
        val target = sublocation(sublocationId)?.globalToLocal(global) ?: return
        reduce {
            it.copy(
                makeRouteSheetVisible = true,
                fromPoint = from,
                toPoint = target,
                toPin = MapPosition(global, sublocationId),
                toVenue = null
            )
        }
    }

    private fun onVenueFeaturePicked(name: String, sublocationId: Int) {
        val venue = sublocation(sublocationId)?.venues?.firstOrNull { it.name == name } ?: return
        onVenuePicked(venue)
    }

    private fun onVenuePicked(venue: Venue) {
        reduce {
            it.copy(
                toVenue = venue,
                toPoint = null,
                toPin = null,
                makeRouteSheetVisible = false,
                venueSheet = VenueSheetState(venue)
            )
        }
        venue.toLocationPoint().toMapPosition()
            ?.let { emitEffect(NavigationEffect.MoveCameraToPoint(it.point)) }
    }

    private fun onRouteVenue() {
        if (_state.value.isRouting) {
            onBuildRoute()
        } else {
            reduce { it.copy(makeRouteSheetVisible = true) }
        }
    }

    private fun onPositionUpdated(position: Position?) {
        val prev = _state.value
        reduce {
            it.copy(position = position, fromPoint = position?.locationPoint)
        }

        if (position == null || !prev.followMyLocation) return

        position.locationPoint?.sublocationId?.let(::showSublocation)
        position.point?.let { emitEffect(NavigationEffect.MoveCameraToPoint(it)) }
    }


    private fun onBuildRoute() {
        val prev = _state.value
        val destination = prev.toVenue?.toLocationPoint() ?: prev.toPoint
        val target = destination?.toMapPosition()
        if (target == null) {
            emitEffect(NavigationEffect.ShowSnackbar("No destination selected"))
            return
        }
        reduce {
            it.copy(
                isRouting = true,
                routeTarget = target,
                makeRouteSheetVisible = false,
                routeInfoVisible = true,
                isFinishNear = false
            )
        }
    }

    private fun onRouteChanged(status: RouteLayerStatus, lengthMeters: Float?) {
        if (!_state.value.isRouting) return
        when (status) {
            RouteLayerStatus.ROUTE_UPDATED -> if (lengthMeters != null) {
                reduce {
                    it.copy(
                        routeDistanceMeters = lengthMeters,
                        etaSeconds = (lengthMeters / WALK_SPEED_MPS).roundToInt()
                    )
                }
            }

            RouteLayerStatus.MISSING_POSITION ->
                emitEffect(NavigationEffect.ShowSnackbar("Route status: $status"))

            RouteLayerStatus.MISSING_GRAPH,
            RouteLayerStatus.MISSING_LOCATION,
            RouteLayerStatus.MISSING_ROUTE,
            RouteLayerStatus.MISSING_PROJECTION -> {
                reduce { it.withoutRoute() }
                emitEffect(NavigationEffect.ShowSnackbar("Route status: $status"))
            }

            RouteLayerStatus.IDLE -> Unit
        }
    }

    private fun onRouteAdvanced(remainingMeters: Float, remainingSeconds: Float) {
        if (!_state.value.isRouting) return
        reduce {
            it.copy(
                routeDistanceMeters = remainingMeters,
                etaSeconds = remainingSeconds.roundToInt()
            )
        }
    }

    private fun onRouteTargetReached() {
        if (!_state.value.isRouting) return
        reduce { it.withoutRoute().withoutDestination().copy(isFinishNear = true) }
    }

    private fun onCancelPin() {
        reduce { it.withoutDestination().copy(makeRouteSheetVisible = false) }
    }

    private fun onCancelRoute() {
        reduce { it.withoutRoute().withoutDestination().copy(isFinishNear = false) }
    }

    private fun sublocation(id: Int): Sublocation? =
        _state.value.location?.location?.getSublocationById(id)

    private fun LocationPoint.toMapPosition(): MapPosition? =
        sublocation(sublocationId)?.localToGlobal(this)?.let { MapPosition(it, sublocationId) }

    private fun Venue.toLocationPoint(): LocationPoint =
        LocationPoint(point, locationId, sublocationId)

    private fun NavigationState.withoutRoute() = copy(
        isRouting = false,
        routeTarget = null,
        routeInfoVisible = false,
        routeDistanceMeters = null,
        etaSeconds = null
    )

    private fun NavigationState.withoutDestination() = copy(
        toPoint = null,
        toPin = null,
        toVenue = null
    )

    private fun readQrParams() {
        val initialSublocation = savedStateHandle.get<Int>(KEY_INITIAL_SUBLOCATION)?.takeIf { it > 0 }
        savedStateHandle.remove<Int>(KEY_INITIAL_SUBLOCATION)
        savedStateHandle.remove<Int>(KEY_INITIAL_VENUE)

        if (initialSublocation != null) reduce { it.copy(requestedSublocationId = initialSublocation) }
    }

    private inline fun reduce(block: (NavigationState) -> NavigationState) {
        _state.update { block(it) }
    }

    private fun emitEffect(effect: NavigationEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

}
