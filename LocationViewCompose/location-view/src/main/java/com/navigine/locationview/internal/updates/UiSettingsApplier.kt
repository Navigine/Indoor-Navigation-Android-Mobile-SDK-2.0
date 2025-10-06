package com.navigine.locationview.internal.updates

import com.navigine.idl.java.LocationWindow
import com.navigine.locationview.settings.LocationUiSettings

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
}