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
 * Note: This is intentionally a class (not a data class) to keep binary
 * compatibility flexibility for future changes.
 */

@Immutable
public class LocationUiSettings(
    public val rotateGesturesEnabled : Boolean = true,
    public val tiltGesturesEnabled : Boolean = true,
    public val scrollGesturesEnabled : Boolean = true,
    public val zoomGesturesEnabled : Boolean = true,
) {

    override fun equals(other: Any?): Boolean = other is LocationUiSettings &&
            rotateGesturesEnabled == other.rotateGesturesEnabled &&
            tiltGesturesEnabled == other.tiltGesturesEnabled &&
            scrollGesturesEnabled == other.scrollGesturesEnabled &&
            zoomGesturesEnabled == other.zoomGesturesEnabled

    override fun hashCode(): Int = Objects.hash(
        rotateGesturesEnabled,
        tiltGesturesEnabled,
        scrollGesturesEnabled,
        zoomGesturesEnabled
    )


    override fun toString(): String = "LocationUiSettings(" +
            "rotateGesturesEnabled=$rotateGesturesEnabled, " +
            "tiltGesturesEnabled=$tiltGesturesEnabled, " +
            "scrollGesturesEnabled=$scrollGesturesEnabled, " +
            "zoomGesturesEnabled=$zoomGesturesEnabled)"

    public fun copy(
        rotateGesturesEnabled : Boolean = this.rotateGesturesEnabled,
        tiltGesturesEnabled : Boolean = this.tiltGesturesEnabled,
        scrollGesturesEnabled : Boolean = this.scrollGesturesEnabled,
        zoomGesturesEnabled : Boolean = this.zoomGesturesEnabled,
    ) : LocationUiSettings = LocationUiSettings(
        rotateGesturesEnabled,
        tiltGesturesEnabled,
        scrollGesturesEnabled,
        zoomGesturesEnabled
    )
}

public val DefaultLocationUiSettings : LocationUiSettings = LocationUiSettings()