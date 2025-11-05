package com.navigine.naviginedemocompose.ui.navigation

import androidx.compose.runtime.Immutable
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Position
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.Venue
import com.navigine.naviginedemocompose.domain.model.LocationModel

@Immutable
data class NavigationState(
    val sdkReady: Boolean = false,
    val location: LocationModel? = null,
    val locationWindow: LocationWindow? = null,
    val currentSublocationId: Int = -1,

    val followMyLocation: Boolean = false,
    val zoomDefaults: ZoomDefaults? = null,

    val searchQuery: String = "",
    val isSearchFocused: Boolean = false,
    val filteredVenues: List<Venue> = emptyList(),
    val layerFilterExpr: String? = null,

    val isRouting: Boolean = false,
    val routePath: RoutePath? = null,
    val routeDistanceMeters: Float? = null,
    val etaSeconds: Int? = null,
    val isFinishNear: Boolean = false,
    val routePolylines: List<LocationPolyline> = emptyList(),

    val position: Position? = null,

    // Route points
    val fromPoint: LocationPoint? = null,
    val toPoint : LocationPoint? = null,
    val toVenue : Venue? = null,

    // sheets/panels
    val makeRouteSheetVisible: Boolean = false,
    val routeInfoVisible: Boolean = false,
    val venueSheet: VenueSheetState? = null,

    val warningMessage: String? = null,
    val loadingVisible: Boolean = false,
    val mapKey: Int = 0,
)


data class ZoomDefaults(val min: Float, val max: Float, val default: Float)

data class VenueSheetState(
    val venue: Venue,
    val isVisible: Boolean = true
)