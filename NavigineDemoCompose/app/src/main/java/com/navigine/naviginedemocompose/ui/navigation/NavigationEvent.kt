package com.navigine.naviginedemocompose.ui.navigation

import android.graphics.PointF
import com.navigine.idl.java.GlobalPoint
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Point
import com.navigine.idl.java.Position
import com.navigine.idl.java.RouteLayerStatus
import com.navigine.idl.java.Venue
import com.navigine.locationview.settings.LocationUiSettings
import com.navigine.naviginedemocompose.domain.model.LocationModel
import com.navigine.naviginedemocompose.domain.monitor.RouteMonitorEvent

sealed interface NavigationEffect {

    data class ShowToast(val message: String) : NavigationEffect
    data class ShowSnackbar(val message: String) : NavigationEffect
    data class MoveCameraToPoint(val point: GlobalPoint, val animateMs: Int = 700) : NavigationEffect

    data class ApplyVenueLayerFilter(val expression: String?) : NavigationEffect
}


sealed interface NavigationEvent {
    // Lifecycle/UI
    data object Init : NavigationEvent

    // Location/Floor
    data class LocationLoaded(val location: LocationModel) : NavigationEvent
    data class SwitchFloor(val sublocationId: Int) : NavigationEvent

    //POI basic
    data class VenuePickedOnMap(val venue: Venue) : NavigationEvent

    // Pins / long tap
    data object MapTapped : NavigationEvent
    data class LongPressAt(val global: GlobalPoint?, val sublocationId: Int) : NavigationEvent
    data class VenueFeaturePicked(val venueName: String, val sublocationId: Int) : NavigationEvent

    data object CancelPin : NavigationEvent

    // Position
    data class PositionUpdated(val snapshot: Position?) : NavigationEvent
    data class FollowMyLocationToggle(val enabled: Boolean) : NavigationEvent

    // Route
    data object BuildRoute : NavigationEvent
    data object CancelRoute : NavigationEvent
    data class RouteChanged(val status: RouteLayerStatus, val lengthMeters: Float?) : NavigationEvent
    data class RouteAdvanced(val remainingMeters: Float, val remainingSeconds: Float) : NavigationEvent
    data object RouteTargetReached : NavigationEvent

    // Sheets
    data object HideMakeRouteSheet : NavigationEvent
    data object HideRouteInfo : NavigationEvent
    data object HideFinish : NavigationEvent
    data object HideVenueSheet : NavigationEvent
    data object OnRouteVenue : NavigationEvent
    data object ShowMapSettings : NavigationEvent
    data object HideMapSettings : NavigationEvent
    data class MapSettingsChanged(val settings: LocationUiSettings) : NavigationEvent
    data class GlobalMapToggled(val enabled: Boolean) : NavigationEvent
}