package com.navigine.locationview.internal.updates

import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.MapFilterCondition
import com.navigine.locationview.settings.LocationUiSettings

private const val LAYER_BARRIER = "barrier"
private const val PROPERTY_GEOMETRY = "\$geometry"
private val GEOMETRY_3D_ON = arrayListOf("line", "polygon")
private val GEOMETRY_3D_OFF = arrayListOf("none")

/**
 * Applies UI settings to [LocationWindow].
 * Call this from a SideEffect whenever settings or window change.
 */
internal fun applyUiSettings(
    window: LocationWindow,
    ui: LocationUiSettings,
    prev: LocationUiSettings?
) {
    if (prev == null || ui.rotateGesturesEnabled != prev.rotateGesturesEnabled) {
        runCatching { window.rotateGestureEnabled = ui.rotateGesturesEnabled }
    }
    if (prev == null || ui.tiltGesturesEnabled != prev.tiltGesturesEnabled) {
        runCatching { window.tiltGesturesEnabled = ui.tiltGesturesEnabled }
    }
    if (prev == null || ui.scrollGesturesEnabled != prev.scrollGesturesEnabled) {
        runCatching { window.scrollGesturesEnabled = ui.scrollGesturesEnabled }
    }
    if (prev == null || ui.zoomGesturesEnabled != prev.zoomGesturesEnabled) {
        runCatching { window.zoomGesturesEnabled = ui.zoomGesturesEnabled }
    }
    if (prev == null || ui.is3dEnabled != prev.is3dEnabled) {
        val values = if (ui.is3dEnabled) GEOMETRY_3D_ON else GEOMETRY_3D_OFF
        runCatching {
            window.applyLayerFilter(
                LAYER_BARRIER,
                arrayListOf(MapFilterCondition(PROPERTY_GEOMETRY, values))
            )
        }
    }
}