package com.navigine.locationview.objects.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.navigine.idl.java.MapObjectType
import com.navigine.idl.java.ModelMapObject
import com.navigine.locationview.ExperimentalNavigineApi

/**
 * State handle for a [Model] composable.
 *
 * Provides read-only access to SDK metadata (id, type, data) of the underlying
 * [ModelMapObject]. Use [rememberModelState] to create an instance.
 *
 * ## Example
 * ```kotlin
 * val state = rememberModelState()
 *
 * Model(
 *     position = point,
 *     model = ModelProvider.fromAsset(context, "models/chair.glb", texture),
 *     state = state
 * )
 *
 * if (state.isAttached) {
 *     println("Model id: ${state.id}")
 * }
 * ```
 */
@Stable
public class ModelState internal constructor() {

    internal var obj: ModelMapObject? = null
        private set

    /** True while this handle is attached to a live SDK object. */
    public var isAttached: Boolean by mutableStateOf(false)
        internal set

    /** SDK-assigned unique id of the object (available once attached). */
    public val id: Int? get() = obj?.id

    /** SDK type (should be [MapObjectType.MODEL]), available once attached. */
    public val type: MapObjectType? get() = obj?.type

    /**
     * Arbitrary payload set on the SDK object.
     * Only read this if you truly need it.
     */
    public val data: ByteArray? get() = obj?.data

    /** Direct access to the underlying SDK object. Prefer metadata fields when possible. */
    @ExperimentalNavigineApi
    public val mapObject: ModelMapObject? get() = obj

    internal fun bind(o: ModelMapObject) {
        obj = o
        isAttached = true
    }
    internal fun unbind() {
        obj = null
        isAttached = false
    }
}

/** Creates and remembers a [ModelState] instance. */
@Composable
public fun rememberModelState() : ModelState = remember { ModelState() }