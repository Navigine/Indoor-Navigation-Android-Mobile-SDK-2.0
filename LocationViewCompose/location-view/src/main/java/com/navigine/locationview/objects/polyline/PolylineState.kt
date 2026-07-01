package com.navigine.locationview.objects.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.navigine.idl.java.MapObjectType
import com.navigine.idl.java.PolylineMapObject
import com.navigine.locationview.ExperimentalNavigineApi

/**
 * Read-only handle to a polyline map object created by the Polyline composable.
 *
 * Ownership:
 * - The underlying SDK object is owned by the composition and can be destroyed at any moment
 *   when the composable leaves the composition. Use [isAttached] to check liveness.
 * - Prefer using [id], [type], [data]. Raw access requires an explicit opt-in.
 */
@Stable
public class PolylineState {
    internal var obj: PolylineMapObject? = null
        private set

    /** True while this handle is attached to a live SDK object. */
    public var isAttached: Boolean by mutableStateOf(false)
        internal set

    /** SDK-assigned unique id of the object (available once attached). */
    public val id: Int? get() = obj?.id

    /** SDK type (should be MapObjectType.POLYLINE), available once attached. */
    public val type: MapObjectType? get() = obj?.type

    /**
     * Arbitrary payload set on the SDK object. Be mindful of allocations and size.
     * Only read this if you truly need it.
     */
    public val data: ByteArray? get() = obj?.data

    @ExperimentalNavigineApi
    public val mapObject: PolylineMapObject? get() = obj

    internal fun bind(o: PolylineMapObject) {
        obj = o
        isAttached = true
    }
    internal fun unbind() {
        obj = null
        isAttached = false
    }
}

@Composable
public fun rememberPolylineState(): PolylineState = remember { PolylineState() }