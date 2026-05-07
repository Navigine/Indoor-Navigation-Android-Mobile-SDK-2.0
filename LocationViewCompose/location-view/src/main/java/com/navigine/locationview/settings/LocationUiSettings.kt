package com.navigine.locationview.settings

import androidx.compose.runtime.Immutable
import java.util.Objects

/**
 * UI-related settings for the Navigine map.
 *
 * Mirrors Navigine's gesture toggles:
 * - [rotateGesturesEnabled] -> LocationWindow.setRotateGestureEnabled(...)
 * - [tiltGesturesEnabled] -> LocationWindow.setTiltGesturesEnabled(...)
 * - [scrollGesturesEnabled] -> LocationWindow.setScrollGesturesEnabled(...)
 * - [zoomGesturesEnabled] -> LocationWindow.setZoomGesturesEnabled(...)
 *
 * Rendering toggles:
 * - [is3dEnabled] -> LocationWindow.applyLayerFilter("barrier", ...)
 *
 * Note: This is intentionally a class (not a data class) to keep binary
 * compatibility flexibility for future changes.
 */

@Immutable
public class LocationUiSettings(
    public val rotateGesturesEnabled : Boolean = true,
    public val tiltGesturesEnabled : Boolean = true,
    public val scrollGesturesEnabled : Boolean = true,
    public val zoomGesturesEnabled : Boolean = true,
    public val is3dEnabled: Boolean = false,
) {

    override fun equals(other: Any?): Boolean = other is LocationUiSettings &&
            rotateGesturesEnabled == other.rotateGesturesEnabled &&
            tiltGesturesEnabled == other.tiltGesturesEnabled &&
            scrollGesturesEnabled == other.scrollGesturesEnabled &&
            zoomGesturesEnabled == other.zoomGesturesEnabled &&
            is3dEnabled == other.is3dEnabled

    override fun hashCode(): Int = Objects.hash(
        rotateGesturesEnabled,
        tiltGesturesEnabled,
        scrollGesturesEnabled,
        zoomGesturesEnabled,
        is3dEnabled
    )


    override fun toString(): String = "LocationUiSettings(" +
            "rotateGesturesEnabled=$rotateGesturesEnabled, " +
            "tiltGesturesEnabled=$tiltGesturesEnabled, " +
            "scrollGesturesEnabled=$scrollGesturesEnabled, " +
            "zoomGesturesEnabled=$zoomGesturesEnabled)" +
            "is3dEnabled=$is3dEnabled)"

    public fun copy(
        rotateGesturesEnabled: Boolean = this.rotateGesturesEnabled,
        tiltGesturesEnabled: Boolean = this.tiltGesturesEnabled,
        scrollGesturesEnabled: Boolean = this.scrollGesturesEnabled,
        zoomGesturesEnabled: Boolean = this.zoomGesturesEnabled,
        is3dEnabled: Boolean = this.is3dEnabled,
    ): LocationUiSettings = LocationUiSettings(
        rotateGesturesEnabled,
        tiltGesturesEnabled,
        scrollGesturesEnabled,
        zoomGesturesEnabled,
        is3dEnabled,
    )
}

public val DefaultLocationUiSettings : LocationUiSettings = LocationUiSettings()