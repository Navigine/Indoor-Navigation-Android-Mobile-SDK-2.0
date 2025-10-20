package com.navigine.naviginedemocompose.ui.navigation

import android.graphics.PointF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Position
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.domain.monitor.LocationMonitor
import com.navigine.naviginedemocompose.domain.monitor.PositionMonitor
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
    private val sdk: NavigineSdkManager
) : ViewModel() {

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

        locationMonitor.currentLocation.onEach { loc ->
            if (loc != null) {
                onEvent(NavigationEvent.LocationLoaded(loc))
            }
        }.launchIn(viewModelScope)

        positionMonitor.positions.onEach { pos ->
            onEvent(NavigationEvent.PositionUpdated(pos))
        }
            .launchIn(viewModelScope)

        mapRecomposeKey
            .onEach { k -> _state.update { it.copy(mapKey = k) } }
            .launchIn(viewModelScope)

        sdk.state
            .onEach { st ->
                val ready = st is NavigineSdkManager.SdkState.Ready
                reduce { it.copy(sdkReady = ready, loadingVisible = !ready) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: NavigationEvent) {
        when (event) {
            NavigationEvent.Init -> onInit()
            is NavigationEvent.WindowReady -> onWindowReady(event.window)
            is NavigationEvent.LocationLoaded -> reduce {
                it.copy(
                    location = event.location,
                    loadingVisible = false
                )
            }

            is NavigationEvent.SwitchFloor -> onSwitchFloor(event.sublocationId)
            is NavigationEvent.PositionUpdated -> onPositionUpdated(event.snapshot)
            is NavigationEvent.BuildRoute -> TODO()
            NavigationEvent.CancelPin -> onCancelPin()
            NavigationEvent.CancelRoute -> onCancelRoute()
            is NavigationEvent.FollowMyLocationToggle -> reduce { it.copy(followMyLocation = event.enabled) }
            is NavigationEvent.LongPressAt -> onLongPress(event.point)
            NavigationEvent.Recenter -> TODO()
            NavigationEvent.SearchClear -> TODO()
            is NavigationEvent.SearchFocusChanged -> TODO()
            is NavigationEvent.SearchQueryChanged -> TODO()
            is NavigationEvent.VenuePickedOnMap -> TODO()
            is NavigationEvent.VenueSelected -> TODO()
        }
    }


    private fun onInit() {
        val ready = sdk.state.value is NavigineSdkManager.SdkState.Ready
        reduce { it.copy(sdkReady = ready, loadingVisible = !ready) }
    }

    private fun onWindowReady(window: LocationWindow) {
        reduce { it.copy(locationWindow = window) }
        _state.value.currentSublocationId.let { id ->
            window.setSublocationId(id)
        }

        emitEffect(NavigationEffect.ApplyVenueLayerFilter(state.value.layerFilterExpr))
    }

    private fun onSwitchFloor(id: Int) {
        _state.value.locationWindow?.let { win ->
            win.setSublocationId(id)
        }
        reduce { it.copy(currentSublocationId = id) }
    }

    private fun onPositionUpdated(position: Position?) {
        val prev = _state.value
        reduce { it.copy(position = position) }

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
        val win = _state.value.locationWindow ?: return
        val loc = _state.value.location ?: return
        reduce {
            it.copy(
                makeRouteSheetVisible = true,
                pinPoint = LocationPoint(
                    win.screenPositionToMeters(point),
                    loc.id,
                    _state.value.currentSublocationId
                )
            )
        }
    }

    private fun onCancelPin() {
        reduce {
            it.copy(
                makeRouteSheetVisible = false,
                pinPoint = null
            )
        }
    }

    private fun onCancelRoute() {
        // TODO: cancel via RouteManager
        reduce {
            it.copy(
                isRouting = false,
                cancelRouteSheetVisible = false,
                routePath = null,
                routeDistanceMeters = null,
                etaSeconds = null,
                isFinishNear = false
            )
        }
        emitEffect(NavigationEffect.HideRoute)
    }

    private inline fun reduce(block: (NavigationState) -> NavigationState) {
        _state.update { block(it) }
    }

    private fun emitEffect(effect: NavigationEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

