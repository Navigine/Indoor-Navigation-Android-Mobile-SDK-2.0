package com.navigine.locationview.objects.icon

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import com.navigine.idl.java.AnimationType
import com.navigine.idl.java.IconMapObject
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.MapObjectType
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode

/**
 * Declarative icon map object for Navigine.
 *
 * Lifecycle
 * - Creates an SDK [IconMapObject] via `LocationWindow.addIconMapObject()` when this composable
 *   enters the composition and removes it via `LocationWindow.removeIconMapObject()` when it
 *   leaves the composition (or when the map composition is cleared).
 * - Only changed properties are applied on recomposition; nullable params mean "do not change".
 *
 * Animation
 * - If [animatedPosition] is true, position updates will use
 *   `IconMapObject.setPositionAnimated()` with [positionAnimDuration] and [positionAnimType].
 * - If [animatedAngle] is true, angle updates will use
 *   `IconMapObject.setAngleAnimated()` with [angleAnimDuration] and [angleAnimType].
 * - Changing animation parameters affects subsequent updates.
 *
 * Sizing & layout
 * - [sizeWidth]/[sizeHeight], [bufferWidth]/[bufferHeight], [offsetX]/[offsetY] are applied only
 *   when both components of the pair are non-null.
 *
 * Ownership & state
 * - The underlying SDK object is owned by the composition. Pass [state] if you need to read
 *   the object's metadata (id/type/data) during its lifetime.
 * - [onObjectReady] is invoked once the object is created; it provides the SDK id and type.
 *
 * Visibility & interaction
 * - [visible] toggles drawing; the object remains allocated until removed from composition.
 * - [interactive] enables pick/hit-test. [collisionEnabled] toggles collision processing.
 *
 * @param position Icon position in location coordinates..
 * @param bitmap Bitmap image for the icon..
 * @param sizeWidth Icon width in pixels; applied only together with [sizeHeight].
 * @param sizeHeight Icon height in pixels; applied only together with [sizeWidth].
 * @param state Optional handle to observe id/type/data (the composition owns lifecycle).
 * @param angle Icon rotation in degrees. If null, the current SDK value is kept.
 * @param alpha Icon opacity [0f..1f]. If null, the current SDK value is kept.
 * @param interactive Enables hit-testing/picking for this icon.
 * @param visible Controls visibility without removing the object from the map.
 * @param title Optional title/label (no-op if unsupported on current SDK build).
 * @param bufferWidth Horizontal buffer in pixels; applied only with [bufferHeight].
 * @param bufferHeight Vertical buffer in pixels; applied only with [bufferWidth].
 * @param offsetX Horizontal offset in pixels; applied only with [offsetY].
 * @param offsetY Vertical offset in pixels; applied only with [offsetX].
 * @param priority Draw priority/ordering as defined by the SDK (higher may be drawn above).
 * @param collisionEnabled Enables SDK collision handling for this icon.
 * @param flat Draw the icon flat (no perspective).
 * @param animatedPosition If true, subsequent [position] updates are animated.
 * @param positionAnimDuration Duration (seconds) for animated position updates.
 * @param positionAnimType Animation easing for position updates.
 * @param animatedAngle If true, subsequent [angle] updates are animated.
 * @param angleAnimDuration Duration (seconds) for animated angle updates.
 * @param angleAnimType Animation easing for angle updates.
 * @param onObjectReady Optional callback invoked once after creation with (id, type).
 */
@Composable
@NavigineMapComposable
public fun Icon(
    position: LocationPoint,
    bitmap: Bitmap? = null,
    sizeWidth: Float? = null,
    sizeHeight: Float? = null,
    state: IconState? = null,
    angle: Float? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    visible: Boolean? = null,
    title: String? = null,
    bufferWidth: Float? = null,
    bufferHeight: Float? = null,
    offsetX: Float? = null,
    offsetY: Float? = null,
    priority: Float? = null,
    collisionEnabled: Boolean? = null,
    flat: Boolean? = null,
    // Optional animation for position/angle if you want animated updates
    animatedPosition: Boolean = false,
    positionAnimDuration: Float = 0f,
    positionAnimType: AnimationType = AnimationType.LINEAR,
    animatedAngle: Boolean = false,
    angleAnimDuration: Float = 0f,
    angleAnimType: AnimationType = AnimationType.LINEAR,
    onObjectReady: ((id: Int, type: MapObjectType) -> Unit)? = null
) {

    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<IconNode, LocationApplier>(
        factory = {
            val icon = applier.window.addIconMapObject() ?: error("Failed to add icon")
            icon.apply {
                position.let {
                    if (animatedPosition) setPositionAnimated(
                        it,
                        positionAnimDuration,
                        positionAnimType
                    )
                    else setPosition(it)
                }
                bitmap?.let { setBitmap(it) }
                if (sizeWidth != null && sizeHeight != null) setSize(sizeWidth, sizeHeight)
                angle?.let {
                    if (animatedAngle) setAngleAnimated(it, angleAnimDuration, angleAnimType)
                    else setAngle(it)
                }
                alpha?.let { setAlpha(it) }
                interactive?.let { setInteractive(it) }
                visible?.let { setVisible(it) }
                title?.let { setTitle(it) }
                if (bufferWidth != null && bufferHeight != null) setBuffer(
                    bufferWidth,
                    bufferHeight
                )
                if (offsetX != null && offsetY != null) setOffset(offsetX, offsetY)
                priority?.let { setPriority(it) }
                collisionEnabled?.let { setCollisionEnabled(it) }
                flat?.let { setFlat(it) }
            }
            state?.bind(icon)
            onObjectReady?.invoke(icon.id, icon.type)
            IconNode(applier, icon, state)
        },
        update = {
            update(position) { p ->
                if (animatedPosition) icon.setPositionAnimated(
                    p,
                    positionAnimDuration,
                    positionAnimType
                )
                else icon.setPosition(p)
            }
            update(bitmap) { b -> if (b != null) icon.setBitmap(b) }
            update(angle) { a ->
                if (a != null) {
                    if (animatedAngle) icon.setAngleAnimated(a, angleAnimDuration, angleAnimType)
                    else icon.setAngle(a)
                }
            }
            update(alpha) { v -> if (v != null) icon.setAlpha(v) }
            update(interactive) { v -> if (v != null) icon.setInteractive(v) }
            update(visible) { v -> if (v != null) icon.setVisible(v) }
            update(title) { t -> if (t != null) runCatching { icon.setTitle(t) } }
            update(priority) { v -> if (v != null) icon.setPriority(v) }
            update(flat) { v -> if (v != null) icon.setFlat(v) }
            update(sizeWidth to sizeHeight) { (w, h) ->
                if (w != null && h != null) icon.setSize(w, h)
            }
            update(bufferWidth to bufferHeight) { (bw, bh) ->
                if (bw != null && bh != null) icon.setBuffer(bw, bh)
            }
            update(offsetX to offsetY) { (ox, oy) ->
                if (ox != null && oy != null) icon.setOffset(ox, oy)
            }
        }
    )
}

internal class IconNode(
    /** We keep a handle to the applier to access its LocationWindow on removal. */
    val windowHandle: LocationApplier,
    val icon: IconMapObject,
    private val state: IconState?
) : LocationNode {
    override fun onRemoved() {
        // Remove the object from the SDK when this node leaves composition
        state?.unbind()
        runCatching { windowHandle.window.removeIconMapObject(icon) }
    }

    override fun onCleared() {
        // As a safety net, also try to remove on full clear
        state?.unbind()
        runCatching { windowHandle.window.removeIconMapObject(icon) }
    }
}
