package com.navigine.naviginedemocompose.ui.navigation

import android.graphics.PointF
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Point
import com.navigine.idl.java.Polyline
import com.navigine.idl.java.Position
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.RouteStatus
import com.navigine.idl.java.Venue
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

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val locationMonitor: LocationMonitor,
    private val positionMonitor: PositionMonitor,
    private val routeMonitor: RouteMonitor,
    private val sdk: NavigineSdkManager,
    private val saveStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val WALK_SPEED_MPS = 1.43f
        private const val FINISH_THRESHOLD_M = 1.5f
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

        getQrParams()

        sdk.state
            .onEach { st ->
                val ready = st is NavigineSdkManager.SdkState.Ready
                reduce { it.copy(sdkReady = ready, loadingVisible = !ready) }
            }
            .launchIn(viewModelScope)

        mapRecomposeKey
            .onEach { k -> _state.update { it.copy(mapKey = k) } }
            .launchIn(viewModelScope)

        locationMonitor.currentLocation.onEach { loc ->
            if (loc != null)
                onEvent(NavigationEvent.LocationLoaded(loc))
        }.launchIn(viewModelScope)

        positionMonitor.positions.onEach { pos ->
            onEvent(NavigationEvent.PositionUpdated(pos))
        }.launchIn(viewModelScope)

        routeMonitor.events.onEach {
            onEvent(NavigationEvent.RouteUpdate(it))
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: NavigationEvent) {
        when (event) {
            NavigationEvent.Init -> onInit()
            is NavigationEvent.WindowReady -> onWindowReady(event.window)
            is NavigationEvent.LocationLoaded -> onLocationLoaded(event.location)

            is NavigationEvent.SwitchFloor -> onSwitchFloor(event.sublocationId)
            is NavigationEvent.PositionUpdated -> onPositionUpdated(event.snapshot)
            NavigationEvent.CancelPin -> onCancelPin()
            is NavigationEvent.FollowMyLocationToggle -> reduce { it.copy(followMyLocation = event.enabled) }
            is NavigationEvent.LongPressAt -> onLongPress(event.point)

            NavigationEvent.SearchClear -> TODO()
            is NavigationEvent.SearchFocusChanged -> TODO()
            is NavigationEvent.SearchQueryChanged -> TODO()

            is NavigationEvent.VenuePickedOnMap -> onVenuePicked(event.venue)

            NavigationEvent.BuildRoute -> onBuildRoute()
            is NavigationEvent.RouteUpdate -> onRouteUpdate(event.event)
            NavigationEvent.CancelRoute -> onCancelRoute()
            NavigationEvent.HideMakeRouteSheet -> reduce { it.copy(makeRouteSheetVisible = false) }
            NavigationEvent.HideRouteInfo -> reduce { it.copy(routeInfoVisible = false) }
            NavigationEvent.HideFinish -> reduce { it.copy(isFinishNear = false) }
            NavigationEvent.HideVenueSheet -> reduce { it.copy(venueSheet = null) }
            NavigationEvent.OnRouteVenue -> reduce { it.copy(makeRouteSheetVisible = true) }
        }
    }

    private fun onInit() {
        val ready = sdk.state.value is NavigineSdkManager.SdkState.Ready
        reduce { it.copy(sdkReady = ready, loadingVisible = !ready) }
    }

    private fun onLocationLoaded(model: LocationModel){
        reduce {
            it.copy(
                location = model,
                currentSublocationId = model.location?.sublocations?.firstOrNull()?.id ?: -1,
                loadingVisible = false
            )
        }
    }

    private fun onWindowReady(window: LocationWindow) {
        reduce { it.copy(locationWindow = window) }
        _state.value.currentSublocationId.let { id ->
            window.setSublocationId(id)
        }

        emitEffect(NavigationEffect.ApplyVenueLayerFilter(state.value.layerFilterExpr))
    }

    private fun onSwitchFloor(id: Int) {
        Log.d("NavigationViewModel", "onSwitchFloor: $id")
        if(_state.value.followMyLocation) {
            emitEffect(NavigationEffect.ShowToast("Turn of follow mode to change floor"))
            return
        }
        _state.value.locationWindow?.let { win ->
            win.setSublocationId(id)
        }
        reduce { it.copy(currentSublocationId = id) }
    }

    private fun onVenuePicked( venue: Venue){
        val prev = _state.value
        reduce { it.copy(
            toVenue = venue,
            toPoint = null,
            makeRouteSheetVisible = false,
            venueSheet = VenueSheetState(venue, true)
        ) }
        venue.point?.let { emitEffect(NavigationEffect.MoveCameraToPoint(it)) }
    }

    private fun onPositionUpdated(position: Position?) {
        val prev = _state.value
        reduce {
            it.copy(position = position, fromPoint = position?.locationPoint)
        }

        if (position == null) return

        if (prev.followMyLocation) {
            val sublocId = position.locationPoint?.sublocationId
            if (sublocId != null && sublocId != prev.currentSublocationId) {
                onSwitchFloor(sublocId)
            }
            position.locationPoint?.point?.let { pt ->
                emitEffect(NavigationEffect.MoveCameraToPoint(pt))
            }
        }
    }

    private fun onLongPress(point: PointF) {
        val prev = _state.value
        val win = prev.locationWindow ?: return
        val loc = prev.location ?: return
        reduce {
            it.copy(
                makeRouteSheetVisible = true,
                fromPoint = prev.position?.locationPoint ?: return,
                toPoint = LocationPoint(
                    win.screenPositionToMeters(point),
                    loc.id,
                    _state.value.currentSublocationId
                ),
                toVenue = null
            )
        }
    }

    private fun onBuildRoute(){
        val prev = _state.value
        val targetVenue = prev.toVenue
        val targetPoint = prev.toPoint
        viewModelScope.launch {
            val res = runCatching {
                when {
                    targetVenue != null -> routeMonitor.startRouteTo(targetVenue)
                    targetPoint != null -> routeMonitor.startRouteTo(targetPoint)
                    else -> error("No destination selected")
                }
            }
            res.onSuccess {
                reduce {
                    it.copy(
                        isRouting = true,
                        makeRouteSheetVisible = false,
                        routeInfoVisible = true
                    )
                }
            }.onFailure {
                emitEffect(NavigationEffect.ShowSnackbar("Failed to start route: ${it.message}"))
            }
        }
    }

    private fun onRouteUpdate(event: RouteMonitorEvent){
        when(event){
            RouteMonitorEvent.Cancelled -> {}
            is RouteMonitorEvent.Advanced -> {
                handleRouteAdvanced(event.progress, event.point)
            }
            is RouteMonitorEvent.Changed -> {
                when(event.status){
                    RouteStatus.MISSING_ROUTE,
                    RouteStatus.MISSING_GRAPH,
                    RouteStatus.MISSING_POSITION,
                    RouteStatus.MISSING_PROJECTION -> {
                        reduce { it.copy(isRouting = false, routePath = null, routeDistanceMeters = null, etaSeconds = null) }
                        emitEffect(NavigationEffect.ShowSnackbar("Route status: ${event.status}"))
                    }
                    RouteStatus.NEW_ROUTE -> handleNewRoute(event.path)
                }
            }
        }
    }

    private fun handleNewRoute(path: RoutePath?){
        if (path == null) {
            emitEffect(NavigationEffect.ShowToast("Empty route"))
            return
        }
        val length = path.length
        val eta = (length / WALK_SPEED_MPS).toInt()

        val pointsBySublocation = path.points.reversed()
            .groupBy { it.sublocationId }
        val polys = buildPolylines(pointsBySublocation)

        if (length <= FINISH_THRESHOLD_M) { onReachedDestination(); return }

        reduce {
            it.copy(
                isRouting = true,
                routePath = path,
                routePolylines = polys,
                routeDistanceMeters = length,
                etaSeconds = eta,
                isFinishNear = length < FINISH_THRESHOLD_M
            )
        }
    }

    private fun handleRouteAdvanced(distance : Float, point : LocationPoint) {
        val path = _state.value.routePath ?: return
        val tail = path.tail(distance) ?: return

        val pointsBySublocation = tail.points
            .groupBy { it.sublocationId }

        val left = tail.length
        val eta = (left / WALK_SPEED_MPS).toInt()
        val polys = buildPolylines(pointsBySublocation)

        if (left <= FINISH_THRESHOLD_M) { onReachedDestination(); return }

        reduce {
            it.copy(
                routePath = tail,
                routeDistanceMeters = left,
                routePolylines = polys,
                etaSeconds = eta
            )
        }
    }

    private fun onReachedDestination() {
        viewModelScope.launch { runCatching { routeMonitor.cancel() } }
        reduce {
            it.copy(
                isRouting = false,
                routeInfoVisible = false,
                routePolylines = emptyList(),
                routePath = null,
                routeDistanceMeters = null,
                etaSeconds = null,
                isFinishNear = true
            )
        }
    }

    private fun onCancelPin() {
        reduce {
            it.copy(
                makeRouteSheetVisible = false,
                toPoint = null,
                toVenue = null
            )
        }
    }

    private fun onCancelRoute() {
        viewModelScope.launch {
            routeMonitor.cancel()
            reduce {
                it.copy(
                    isRouting = false,
                    routeInfoVisible = false,
                    routePath = null,
                    routePolylines = emptyList(),
                    routeDistanceMeters = null,
                    etaSeconds = null,
                    isFinishNear = false,
                    toPoint = null,
                    toVenue = null
                )
            }
        }
    }

    private fun buildPolylines(pointsByFloor :  Map<Int, List<LocationPoint>>) : List<LocationPolyline>{
        val locId = _state.value.location?.id ?: return emptyList()
        return pointsByFloor.mapNotNull { (sid, list) ->
                if (list.isEmpty()) return@mapNotNull null
                val arr = ArrayList<Point>(list.size)
                list.forEach { arr.add(it.point) }
                LocationPolyline(Polyline(arr), locId, sid)
            }
    }

    private fun getQrParams(){
        val initSubloc: Int? =
            saveStateHandle.get<Int>("initial_subloc")?.takeIf { it > 0 }
        val initVenueId : Int? =
            saveStateHandle.get<Int>("initial_venue_id")?.takeIf { it > 0}

        saveStateHandle.remove<Int>("initial_subloc")
        saveStateHandle.remove<Int>("initial_venue_id")

        if (initSubloc != null) reduce { st -> st.copy(currentSublocationId = initSubloc) }
//        if (initVenueId != null) reduce { st -> st.copy(toVenue = initVenueId) }

    }

    private inline fun reduce(block: (NavigationState) -> NavigationState) {
        _state.update { block(it) }
    }

    private fun emitEffect(effect: NavigationEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

