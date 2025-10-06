package com.navigine.locationview.objects.circle

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navigine.idl.java.AnimationType
import com.navigine.idl.java.CircleMapObject
import com.navigine.idl.java.LocationPoint
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.utils.toRgbaF

internal class CircleNode(
    private val windowHandle: LocationApplier,
    val circle: CircleMapObject,
    private val state: CircleState?
) : LocationNode {
    override fun onRemoved() {
        state?.unbind()
        runCatching { windowHandle.window.removeCircleMapObject(circle) }
    }
    override fun onCleared() {
        state?.unbind()
        runCatching { windowHandle.window.removeCircleMapObject(circle) }
    }
}

/**
 * Declarative circle map object for Navigine.
 *
 * Lifecycle:
 * - Creates a [CircleMapObject] via [LocationApplier.window.addCircleMapObject] when this
 *   composable enters the composition, and removes it when it leaves (or the composition is cleared).
 * - Only changed properties are applied on recomposition.
 *
 * Color:
 * - [colorArgb] is converted to RGBA floats in [0f..1f] and passed to the SDK as (r,g,b,a).
 * - Outline color/alpha are set via [outlineColorArgb]/[outlineAlpha] when provided.
 *
 * Animation:
 * - If [animatedPosition] is true, position updates use
 *   [CircleMapObject.setPositionAnimated] with [positionAnimDuration] and [positionAnimType].
 *   Changing animation parameters affects subsequent position updates.
 *
 * Visibility & Interaction:
 * - [visible] toggles drawing; the object remains allocated until removed from composition.
 * - [interactive] enables pick/hit-test.
 *
 * Advanced:
 * - [state] provides a read-only handle with id/type/data. The raw SDK object should
 *   not be retained; composition owns its lifecycle.
 *
 * @param position Required center point of the circle.
 * @param radius Required radius of the circle in meters.
 * @param colorArgb Required fill color in ARGB format. Internally converted to RGBA float values for the SDK.
 * @param visible Whether the circle is visible on the map.
 * @param collisionEnabled Whether this object participates in collision detection.
 * @param priority Optional render priority (higher = drawn on top).
 * @param bufferWidth Optional width of a buffer zone around the object, used for interaction or layout purposes.
 * @param bufferHeight Optional height of a buffer zone around the object.
 * @param offsetWidth Optional horizontal offset of the object in meters.
 * @param offsetHeight Optional vertical offset of the object in meters.
 * @param animatedPosition If true, the circle will animate its position when `position` changes.
 * @param positionAnimDuration Duration of the position animation in seconds (only used if [animatedPosition] is true).
 * @param positionAnimType Type of animation used for animated movement ([AnimationType.LINEAR], [AnimationType.CUBIC], etc.).
 * @param outlineRadius Optional outline thickness in meters.
 * @param outlineColorArgb Optional outline color in ARGB format.
 * @param outlineAlpha Optional alpha (opacity) for the outline, in [0f..1f].
 * @param title Optional label or title for the circle.
 * @param alpha Optional overall opacity of the circle in range [0f..1f].
 * @param interactive Whether the object should be interactive (e.g., respond to taps).
 * @param state Optional [CircleState] to observe or access internal object properties or bind to raw SDK object.
 * @param onObjectReady Optional callback that is invoked when the circle is created, with the internal object ID.
 */
@Composable
@NavigineMapComposable
public fun Circle(
    position: LocationPoint,
    radius: Float,
    @ColorInt colorArgb: Int,
    visible: Boolean = true,
    collisionEnabled: Boolean = false,
    priority: Float? = null,
    bufferWidth: Float? = null,
    bufferHeight: Float? = null,
    offsetWidth: Float? = null,
    offsetHeight: Float? = null,
    animatedPosition: Boolean = false,
    positionAnimDuration: Float = 1f,
    positionAnimType: AnimationType = AnimationType.LINEAR,
    outlineRadius: Float? = null,
    @ColorInt outlineColorArgb: Int? = null,
    outlineAlpha: Float? = null,
    title: String? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    state: CircleState? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<CircleNode, LocationApplier>(
        factory = {
            val obj = applier.window.addCircleMapObject()
                ?: error("Error adding CircleMapObject")

            // Initial apply
            if(animatedPosition) obj.setPositionAnimated(position, positionAnimDuration, positionAnimType)
            else obj.setPosition(position)
            obj.setRadius(radius)
            run {
                val (r, g, b, a) = colorArgb.toRgbaF()
                obj.setColor(r, g, b, a)
            }
            obj.setVisible(visible)
            title?.let { runCatching { obj.setTitle(it) } }
            alpha?.let { obj.setAlpha(it) }
            interactive?.let { obj.setInteractive(it) }
            obj.setCollisionEnabled(collisionEnabled)
            priority?.let { obj.setPriority(it) }
            if(bufferWidth != null && bufferHeight != null) obj.setBuffer(bufferWidth, bufferHeight)
            if(offsetWidth != null && offsetHeight != null) obj.setOffset(offsetWidth, offsetHeight)
            outlineRadius?.let { obj.setOutlineRadius(it) }
            outlineAlpha?.let { obj.setOutlineAlpha(it) }
            outlineColorArgb?.let { run {
                val (r, g, b, a) = it.toRgbaF()
                obj.setOutlineColor(r, g, b, a)
            } }

            state?.bind(obj)
            onObjectReady?.invoke(obj.id)
            CircleNode(applier, obj, state)
        },
        update = {
            update(position){ p ->
                if(animatedPosition) circle.setPositionAnimated(p, positionAnimDuration, positionAnimType)
                else circle.setPosition(p)
            }
            update(radius) { r -> circle.setRadius(r) }
            update(colorArgb) { cInt ->
                val (r, g, b, a) = cInt.toRgbaF()
                circle.setColor(r, g, b, a)
            }
            update(visible) { v -> circle.setVisible(v) }
            update(title) { t -> t?.let { runCatching { circle.setTitle(it) } } }
            update(alpha) { a -> a?.let { circle.setAlpha(a) } }
            update(interactive) { i -> i?.let { circle.setInteractive(i) } }
            update(collisionEnabled) { circle.setCollisionEnabled(it) }
            update(priority) { v -> v?.let { circle.setPriority(it) } }
            update(bufferWidth to bufferHeight) { (bw, bh) ->
                if (bw != null && bh != null) circle.setBuffer(bw, bh)
            }
            update(offsetWidth to offsetHeight) { (ox, oy) ->
                if (ox != null && oy != null) circle.setOffset(ox, oy)
            }
            update(outlineRadius) { r -> r?.let { circle.setOutlineRadius(it) } }
            update(outlineColorArgb) { cInt ->
                cInt?.let{
                    val (r, g, b, a) = it.toRgbaF()
                    circle.setOutlineColor(r, g, b, a)
                }
            }
            update(outlineAlpha) { a -> a?.let { circle.setOutlineAlpha(it) } }
        }
    )
}

/**
 * Declarative circle map object for Navigine.
 *
 * Lifecycle:
 * - Creates a [CircleMapObject] via [LocationApplier.window.addCircleMapObject] when this
 *   composable enters the composition, and removes it when it leaves (or the composition is cleared).
 * - Only changed properties are applied on recomposition.
 *
 * Color:
 * - [color] is converted to RGBA floats in [0f..1f] and passed to the SDK as (r,g,b,a).
 * - Outline color/alpha are set via [outlineColor]/[outlineAlpha] when provided.
 *
 * Animation:
 * - If [animatedPosition] is true, position updates use
 *   [CircleMapObject.setPositionAnimated] with [positionAnimDuration] and [positionAnimType].
 *   Changing animation parameters affects subsequent position updates.
 *
 * Visibility & Interaction:
 * - [visible] toggles drawing; the object remains allocated until removed from composition.
 * - [interactive] enables pick/hit-test.
 *
 * Advanced:
 * - [state] provides a read-only handle with id/type/data. The raw SDK object should
 *   not be retained; composition owns its lifecycle.
 *
 * @param position Required center point of the circle.
 * @param radius Required radius of the circle in meters.
 * @param color Required fill color of type [Color]. Internally converted to RGBA float values for the SDK.
 * @param visible Whether the circle is visible on the map.
 * @param collisionEnabled Whether this object participates in collision detection.
 * @param priority Optional render priority (higher = drawn on top).
 * @param bufferWidth Optional width of a buffer zone around the object, used for interaction or layout purposes.
 * @param bufferHeight Optional height of a buffer zone around the object.
 * @param offsetWidth Optional horizontal offset of the object in meters.
 * @param offsetHeight Optional vertical offset of the object in meters.
 * @param animatedPosition If true, the circle will animate its position when `position` changes.
 * @param positionAnimDuration Duration of the position animation in seconds (only used if [animatedPosition] is true).
 * @param positionAnimType Type of animation used for animated movement ([AnimationType.LINEAR], [AnimationType.CUBIC], etc.).
 * @param outlineRadius Optional outline thickness in meters.
 * @param outlineColor Optional outline color of type [Color].
 * @param outlineAlpha Optional alpha (opacity) for the outline, in [0f..1f].
 * @param title Optional label or title for the circle.
 * @param alpha Optional overall opacity of the circle in range [0f..1f].
 * @param interactive Whether the object should be interactive (e.g., respond to taps).
 * @param state Optional [CircleState] to observe or access internal object properties or bind to raw SDK object.
 * @param onObjectReady Optional callback that is invoked when the circle is created, with the internal object ID.
 */
@Composable
@NavigineMapComposable
public fun Circle(
    position: LocationPoint,
    radius: Float,
    color: Color,
    visible: Boolean = true,
    collisionEnabled: Boolean = false,
    priority: Float? = null,
    bufferWidth: Float? = null,
    bufferHeight: Float? = null,
    offsetWidth: Float? = null,
    offsetHeight: Float? = null,
    animatedPosition: Boolean = false,
    positionAnimDuration: Float = 1f,
    positionAnimType: AnimationType = AnimationType.LINEAR,
    outlineRadius: Float? = null,
    outlineColor: Color? = null,
    outlineAlpha: Float? = null,
    title: String? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    state: CircleState? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) : Unit = Circle(
    position = position,
    radius = radius,
    colorArgb = color.toArgb(),
    visible = visible,
    collisionEnabled = collisionEnabled,
    priority = priority,
    bufferWidth = bufferWidth,
    bufferHeight = bufferHeight,
    offsetWidth = offsetWidth,
    offsetHeight = offsetHeight,
    animatedPosition = animatedPosition,
    positionAnimDuration = positionAnimDuration,
    positionAnimType = positionAnimType,
    outlineRadius = outlineRadius,
    outlineColorArgb = outlineColor?.toArgb(),
    outlineAlpha = outlineAlpha,
    title = title,
    alpha = alpha,
    interactive = interactive,
    state = state,
    onObjectReady = onObjectReady,
)