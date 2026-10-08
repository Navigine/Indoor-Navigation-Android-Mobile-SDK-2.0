package com.navigine.naviginedemocompose.ui.navigation

import androidx.compose.runtime.Immutable
import com.navigine.idl.java.GlobalPoint
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Position
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.Venue
import com.navigine.locationview.settings.LocationUiSettings
import com.navigine.naviginedemocompose.domain.model.LocationModel

@Immutable
data class NavigationState(
    val sdkReady: Boolean = false,
    val location: LocationModel? = null,
    val requestedSublocationId: Int? = null,

    val followMyLocation: Boolean = false,
    val zoomDefaults: ZoomDefaults? = null,

    val locationUiSettings: LocationUiSettings = LocationUiSettings(),
    val isGlobalMapEnabled: Boolean = false,

    val searchQuery: String = "",
    val isSearchFocused: Boolean = false,
    val filteredVenues: List<Venue> = emptyList(),
    val layerFilterExpr: String? = null,

    val routeTarget: MapPosition? = null,
    val isRouting: Boolean = false,
    val routePath: RoutePath? = null,
    val routeDistanceMeters: Float? = null,
    val etaSeconds: Int? = null,
    val isFinishNear: Boolean = false,

    val position: Position? = null,

    // Route points
    val fromPoint: LocationPoint? = null,
    val toPoint : LocationPoint? = null,
    val toPin: MapPosition? = null,
    val toVenue : Venue? = null,

    // sheets/panels
    val makeRouteSheetVisible: Boolean = false,
    val routeInfoVisible: Boolean = false,
    val mapSettingsSheetVisible : Boolean = false,
    val venueSheet: VenueSheetState? = null,

    val warningMessage: String? = null,
    val loadingVisible: Boolean = false,
    val mapKey: Int = 0,
)

@Immutable
data class MapPosition(val point: GlobalPoint, val sublocationId: Int?)

data class ZoomDefaults(val min: Float, val max: Float, val default: Float)

data class VenueSheetState(
    val venue: Venue,
    val isVisible: Boolean = true
)