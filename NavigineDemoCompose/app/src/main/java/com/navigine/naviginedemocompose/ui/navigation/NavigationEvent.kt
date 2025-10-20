package com.navigine.naviginedemocompose.ui.navigation

import android.graphics.PointF
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Point
import com.navigine.idl.java.Position
import com.navigine.idl.java.Venue
import com.navigine.naviginedemocompose.domain.model.LocationModel

sealed interface NavigationEffect {

    data class ShowToast(val message: String) : NavigationEffect
    data class MoveCameraToPoint(val point: Point, val animateMs: Int = 700) : NavigationEffect

    data class ApplyVenueLayerFilter(val expression: String?) : NavigationEffect

    data class DrawRoute(val points: List<Point>) : NavigationEffect
    data object HideRoute : NavigationEffect
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
    data class VenueSelected(val venue: Venue) : NavigationEvent
    data class VenuePickedOnMap(val venue: Venue) : NavigationEvent

    // Pins / long tap
    data class LongPressAt(val point: PointF, val meters: Point?) : NavigationEvent
    data object CancelPin : NavigationEvent

    // Position
    data class PositionUpdated(val snapshot: Position?) : NavigationEvent
    data class FollowMyLocationToggle(val enabled: Boolean) : NavigationEvent
    data object Recenter : NavigationEvent

    // Route
    data class BuildRoute(val alternative: Boolean = false) : NavigationEvent
    data object CancelRoute : NavigationEvent
}