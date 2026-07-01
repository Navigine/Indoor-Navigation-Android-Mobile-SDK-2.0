package com.navigine.locationview.objects.polygon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.navigine.idl.java.MapObjectType
import com.navigine.idl.java.PolygonMapObject
import com.navigine.locationview.ExperimentalNavigineApi

/**
 * Read-only handle to a polygon created by the [Polygon] composable.
 *
 * Ownership:
 * - The underlying SDK object is owned by the composition and can be destroyed
 *   when the composable leaves the composition. Use [isAttached] to check liveness.
 * - Prefer using [id], [type], [data]. Raw access requires an explicit opt-in.
 */
@Stable
public class PolygonState internal constructor() {
    internal var obj : PolygonMapObject? = null
        private set

    /** True while this handle is attached to a live SDK object. */
    public var isAttached: Boolean by mutableStateOf(false)
        internal set

    /** SDK-assigned unique id of the object (available once attached). */
    public val id: Int? get() = obj?.id

    /** SDK type (should be MapObjectType.POLYGON), available once attached. */
    public val type: MapObjectType? get() = obj?.type

    /**
     * Arbitrary payload set on the SDK object. Be mindful of allocations and size.
     * Only read this if you truly need it.
     */
    public val data: ByteArray? get() = obj?.data

    @ExperimentalNavigineApi
    public val mapObject: PolygonMapObject? get() = obj

    internal fun bind(o: PolygonMapObject) {
        obj = o
        isAttached = true
    }
    internal fun unbind() {
        obj = null
        isAttached = false
    }
}

@Composable
public fun rememberPolygonState(): PolygonState = remember { PolygonState() }