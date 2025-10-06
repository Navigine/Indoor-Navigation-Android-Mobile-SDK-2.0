package com.navigine.locationview.objects.circle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.navigine.idl.java.CircleMapObject
import com.navigine.idl.java.MapObjectType
import com.navigine.locationview.ExperimentalNavigineApi

@Stable
public class CircleState internal constructor(){
    internal var obj : CircleMapObject? = null
        private set

    /** True while this handle is attached to a live SDK object. */
    public val isAttached: Boolean get() = obj != null

    /** SDK-assigned unique id of the object (available once attached). */
    public val id: Int? get() = obj?.id

    /** SDK type (should be MapObjectType.CIRCLE), available once attached. */
    public val type: MapObjectType? get() = obj?.type

    /**
     * Arbitrary payload set on the SDK object. Be mindful of allocations and size.
     * Only read this if you truly need it.
     */
    public val data: ByteArray? get() = obj?.data

    @ExperimentalNavigineApi
    public val mapObject: CircleMapObject? get() = obj

    internal fun bind(o: CircleMapObject) { obj = o }
    internal fun unbind() { obj = null }
}

@Composable
public fun rememberCircleState(): CircleState = remember { CircleState() }