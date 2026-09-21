package com.navigine.locationview.geometry

import androidx.compose.runtime.Immutable
import com.navigine.idl.java.GlobalPoint

/**
 * Object's position on map: WGS84 point and sublocation to which it referenced.
 * [sublocationId] == null - outdoor object (outside the building).
 */
@Immutable
public data class NavPosition(
    public val point: GlobalPoint,
    public val sublocationId: Int? = null,
) {
    public companion object {
        public fun indoor(point: GlobalPoint, sublocationId: Int): NavPosition =
            NavPosition(point, sublocationId)

        public fun outdoor(point: GlobalPoint): NavPosition =
            NavPosition(point, null)
    }
}
