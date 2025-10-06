package com.navigine.locationview.settings

import androidx.compose.runtime.Immutable
import java.util.Objects

/**
 * Map properties.
 *
 * We expose commonly changed properties that live on [com.navigine.idl.java.LocationWindow]:
 * - [minZoomFactor] -> setMinZoomFactor(...)
 * - [maxZoomFactor] -> setMaxZoomFactor(...)
 * - [pickRadius] -> setPickRadius(...)
 * - [stickToBorder] -> setStickToBorder(...)
 * - [sublocationId] -> setSublocationId(...)

 */

@Immutable
public class LocationProperties(
    public val minZoomFactor: Float? = null,
    public val maxZoomFactor: Float? = null,
    public val pickRadius: Float? = null,
    public val stickToBorder: Boolean? = null,
    public val sublocationId: Int? = null,
) {

    override fun toString(): String = "LocationProperties(" +
            "minZoomFactor=$minZoomFactor, " +
            "maxZoomFactor=$maxZoomFactor, " +
            "pickRadius=$pickRadius, " +
            "stickToBorder=$stickToBorder, " +
            "sublocationId=$sublocationId)"

    override fun equals(other: Any?): Boolean = other is LocationProperties &&
            minZoomFactor == other.minZoomFactor &&
            maxZoomFactor == other.maxZoomFactor &&
            pickRadius == other.pickRadius &&
            stickToBorder == other.stickToBorder &&
            sublocationId == other.sublocationId

    override fun hashCode(): Int = Objects.hash(
        minZoomFactor, maxZoomFactor, pickRadius, stickToBorder, sublocationId
    )

    public fun copy(
        minZoomFactor: Float? = this.minZoomFactor,
        maxZoomFactor: Float? = this.maxZoomFactor,
        pickRadius: Float? = this.pickRadius,
        stickToBorder: Boolean? = this.stickToBorder,
        sublocationId: Int? = this.sublocationId,
    ): LocationProperties = LocationProperties(
        minZoomFactor = minZoomFactor,
        maxZoomFactor = maxZoomFactor,
        pickRadius = pickRadius,
        stickToBorder = stickToBorder,
        sublocationId = sublocationId,
    )
}

/** Equivalent to [LocationProperties] with all fields unset (no-ops). */
public val DefaultLocationProperties: LocationProperties = LocationProperties()