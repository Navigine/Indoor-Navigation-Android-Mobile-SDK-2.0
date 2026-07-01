package com.navigine.locationview.objects.icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.navigine.idl.java.IconMapObject
import com.navigine.idl.java.MapObjectType
import com.navigine.locationview.ExperimentalNavigineApi

/**
 * Read-only handle to an Icon map object created by the Icon composable.
 *
 * Notes:
 * - The underlying SDK object is *owned* by the composition. Do not retain strong
 *   references to it. Use [id], [type], [data] for comparisons or logging.
 * - The handle is re-bound when the map window is recreated; treat values as
 *   per-attachment snapshot.
 */
@Stable
public class IconState internal constructor() {
    internal var obj: IconMapObject? = null
        private set

    /** True while this handle is attached to a live SDK object. */
    public var isAttached: Boolean by mutableStateOf(false)
        internal set

    /** SDK-assigned unique id of the object (available once attached). */
    public val id: Int? get() = obj?.id

    /** SDK type (should be MapObjectType.ICON), available once attached. */
    public val type: MapObjectType? get() = obj?.type

    /**
     * Arbitrary payload set on the SDK object. Be mindful of allocations and size.
     * Only read this if you truly need it.
     */
    public val data: ByteArray? get() = obj?.data

    @ExperimentalNavigineApi
    public val mapObject: IconMapObject? get() = obj

    internal fun bind(o: IconMapObject) {
        obj = o
        isAttached = true
    }
    internal fun unbind() {
        obj = null
        isAttached = false
    }
}

@Composable
public fun rememberIconState(): IconState = remember { IconState() }