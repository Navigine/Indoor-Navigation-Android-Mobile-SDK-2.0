package com.navigine.locationview.objects.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.navigine.idl.java.DottedPolylineMapObject
import com.navigine.idl.java.MapObjectType
import com.navigine.locationview.ExperimentalNavigineApi

/**
 * Read-only handle to a dotted polyline created by the [DottedPolyline] composable.
 *
 * Ownership:
 * - The underlying SDK object is owned by the composition and can be destroyed when
 *   the composable leaves the composition. Use [isAttached] to check liveness.
 * - Prefer using [id], [type], [data]. Raw access requires an explicit opt-in.
 */
@Stable
public class DottedPolylineState internal constructor() {
    internal var obj: DottedPolylineMapObject? = null
        private set

    /** True while this handle is bound to a live SDK object. */
    public val isAttached: Boolean get() = obj != null

    /** SDK-assigned id (available once attached). */
    public val id: Int? get() = obj?.id

    /** SDK map object type. */
    public val type: MapObjectType? get() = obj?.type

    /** Arbitrary payload set on the SDK object. */
    public val data: ByteArray? get() = obj?.data

    @ExperimentalNavigineApi
    public val mapObject: DottedPolylineMapObject? get() = obj

    internal fun bind(o: DottedPolylineMapObject) { obj = o }
    internal fun unbind() { obj = null }
}

@Composable
public fun rememberDottedPolylineState(): DottedPolylineState = remember { DottedPolylineState() }