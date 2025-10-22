package com.navigine.naviginedemocompose.ui.navigation

import android.graphics.PointF
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Point
import com.navigine.idl.java.Position
import com.navigine.idl.java.Venue
import com.navigine.naviginedemocompose.domain.model.LocationModel
import com.navigine.naviginedemocompose.domain.monitor.RouteMonitorEvent

sealed interface NavigationEffect {

    data class ShowToast(val message: String) : NavigationEffect
    data class ShowSnackbar(val message: String) : NavigationEffect
    data class MoveCameraToPoint(val point: Point, val animateMs: Int = 700) : NavigationEffect

    data class ApplyVenueLayerFilter(val expression: String?) : NavigationEffect
}


sealed interface NavigationEvent {
    // Lifecycle/UI
    data object Init : NavigationEvent
    data class WindowReady(val window: LocationWindow) : NavigationEvent

    // Location/Floor
    data class LocationLoaded(val location: LocationModel) : NavigationEvent
    data class SwitchFloor(val sublocationId: Int) : NavigationEvent

    // Search/POI basic
    data class SearchQueryChanged(val query: String) : NavigationEvent
    data class SearchFocusChanged(val focused: Boolean) : NavigationEvent
    data object SearchClear : NavigationEvent
    data class VenuePickedOnMap(val venue: Venue) : NavigationEvent

    // Pins / long tap
    data class LongPressAt(val point: PointF, val meters: Point?) : NavigationEvent
    data object CancelPin : NavigationEvent

    // Position
    data class PositionUpdated(val snapshot: Position?) : NavigationEvent
    data class FollowMyLocationToggle(val enabled: Boolean) : NavigationEvent

    // Route
    data object BuildRoute : NavigationEvent
    data object CancelRoute : NavigationEvent
    data class RouteUpdate(val event : RouteMonitorEvent) : NavigationEvent

    // Sheets
    data object HideMakeRouteSheet : NavigationEvent
    data object HideRouteInfo : NavigationEvent
    data object HideFinish : NavigationEvent
}