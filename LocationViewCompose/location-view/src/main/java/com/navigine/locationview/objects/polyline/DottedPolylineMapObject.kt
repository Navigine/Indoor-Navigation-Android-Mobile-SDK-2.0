package com.navigine.locationview.objects.polyline

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navigine.idl.java.DottedPolylineMapObject
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.Placement
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.utils.toRgbaF

/** Internal node owning a single dotted polyline object. */
internal class DottedPolylineNode(
    private val windowHandle: LocationApplier,
    val dotted: DottedPolylineMapObject,
    private val state: DottedPolylineState? = null
) : LocationNode {
    override fun onRemoved() {
        state?.unbind()
        runCatching { windowHandle.window.removeDottedPolylineMapObject(dotted) }
    }
    override fun onCleared() {
        state?.unbind()
        runCatching { windowHandle.window.removeDottedPolylineMapObject(dotted) }
    }
}

/**
 * Declarative dotted polyline.
 *
 * Creates a [DottedPolylineMapObject] via LocationWindow.addDottedPolylineMapObject() and
 * removes it when leaving composition. Properties are applied idempotently; updates are
 * sent only when values change.
 *
 * Color:
 * - [colorArgb] is converted to RGBA floats in [0f..1f] and passed to the SDK as (r,g,b,a).
 *
 * Visibility & Interaction:
 * - [visible] toggles drawing; the object remains allocated until removed from composition.
 * - [interactive] enables pick/hit-test.
 *
 * Advanced:
 * - [state] provides a read-only handle with id/type/data. The raw SDK object should
 *   not be retained; composition owns its lifecycle.
 *
 * @param locationPolyline polyline geometry in map meters (required).
 * @param colorArgb ARGB color; converted internally to RGBA floats required by SDK.
 * @param sizeWidth dot width.
 * @param sizeHeight dot height.
 * @param visible visibility flag (default true).
 * @param placement dot placement mode (default Placement.SPACED).
 * @param placementMinRatio minimal placement ratio (default 0f).
 * @param collisionEnabled enable/disable collision (optional).
 * @param placementSpacing spacing between dots (optional).
 * @param repeatDistance repeat distance along the line (optional).
 * @param repeatGroup repeat group id (optional).
 * @param priority draw priority (optional).
 * @param title optional title/label if supported by SDK.
 * @param alpha overall alpha [0..1] (optional).
 * @param interactive if true, object can be picked/hit-tested (optional).
 * @param state optional handle for id/type/data (and opt-in raw object).
 * @param onObjectReady optional callback fired once the object is created.
 */
@Composable
@NavigineMapComposable
public fun DottedPolyline(
    locationPolyline: LocationPolyline,
    @ColorInt colorArgb: Int,
    sizeWidth: Float,
    sizeHeight: Float,
    visible: Boolean = true,
    placement: Placement = Placement.SPACED,
    placementMinRatio: Float = 0f,
    collisionEnabled: Boolean? = null,
    placementSpacing: Float? = null,
    repeatDistance: Float? = null,
    repeatGroup: Int? = null,
    priority: Float? = null,
    title: String? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    state: DottedPolylineState? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<DottedPolylineNode, LocationApplier>(
        factory = {
            val obj = applier.window.addDottedPolylineMapObject()
                ?: error("Error adding DottedPolylineMapObject")


            obj.setPolyLine(locationPolyline)
            run {
                val (r, g, b, a) = colorArgb.toRgbaF()
                obj.setColor(r, g, b, a)
            }
            obj.setSize(sizeWidth, sizeHeight)
            obj.setVisible(visible)
            collisionEnabled?.let { obj.setCollisionEnabled(it) }
            placement.let { obj.setPlacement(it) }
            placementMinRatio.let { obj.setPlacementMinRatio(it) }
            placementSpacing?.let { obj.setPlacementSpacing(it) }
            repeatDistance?.let { obj.setRepeatDistance(it) }
            repeatGroup?.let { obj.setRepeatGroup(it) }
            priority?.let { obj.setPriority(it) }
            title?.let { runCatching { obj.setTitle(it) } }
            alpha?.let { obj.setAlpha(it) }
            interactive?.let { obj.setInteractive(it) }

            state?.bind(obj)
            onObjectReady?.invoke(obj.id)

            DottedPolylineNode(applier, obj, state)
        },
        update = {
            update(locationPolyline) { p -> dotted.setPolyLine(p) }
            update(colorArgb) { c ->
                val (r, g, b, a) = c.toRgbaF()
                dotted.setColor(r, g, b, a)
            }
            update(sizeWidth) { w -> dotted.setSize(w, sizeHeight) }
            update(sizeHeight) { h -> dotted.setSize(sizeWidth, h) }
            update(visible) { v -> dotted.setVisible(v) }
            update(collisionEnabled) { v -> v?.let { dotted.setCollisionEnabled(it) } }
            update(placement) { dotted.setPlacement(it) }
            update(placementMinRatio) { dotted.setPlacementMinRatio(it) }
            update(placementSpacing) { v -> v?.let { dotted.setPlacementSpacing(it) } }
            update(repeatDistance) { v -> v?.let { dotted.setRepeatDistance(it) } }
            update(repeatGroup) { v -> v?.let { dotted.setRepeatGroup(it) } }
            update(priority) { v -> v?.let { dotted.setPriority(it) } }
            update(title) { t -> t?.let { runCatching { dotted.setTitle(it) } } }
            update(alpha) { a -> a?.let { dotted.setAlpha(it) } }
            update(interactive) { i -> i?.let { dotted.setInteractive(it) } }
        }
    )
}

/**
 * Declarative dotted polyline.
 *
 * Creates a [DottedPolylineMapObject] via LocationWindow.addDottedPolylineMapObject() and
 * removes it when leaving composition. Properties are applied idempotently; updates are
 * sent only when values change.
 *
 * Color:
 * - [color] is converted to RGBA floats in [0f..1f] and passed to the SDK as (r,g,b,a).
 *
 * Visibility & Interaction:
 * - [visible] toggles drawing; the object remains allocated until removed from composition.
 * - [interactive] enables pick/hit-test.
 *
 * Advanced:
 * - [state] provides a read-only handle with id/type/data. The raw SDK object should
 *   not be retained; composition owns its lifecycle.
 *
 * @param locationPolyline polyline geometry in map meters (required).
 * @param color color of type [Color]; converted internally to RGBA floats required by SDK.
 * @param sizeWidth dot width.
 * @param sizeHeight dot height.
 * @param visible visibility flag (default true).
 * @param placement dot placement mode (default Placement.SPACED).
 * @param placementMinRatio minimal placement ratio (default 0f).
 * @param collisionEnabled enable/disable collision (optional).
 * @param placementSpacing spacing between dots (optional).
 * @param repeatDistance repeat distance along the line (optional).
 * @param repeatGroup repeat group id (optional).
 * @param priority draw priority (optional).
 * @param title optional title/label if supported by SDK.
 * @param alpha overall alpha [0..1] (optional).
 * @param interactive if true, object can be picked/hit-tested (optional).
 * @param state optional handle for id/type/data (and opt-in raw object).
 * @param onObjectReady optional callback fired once the object is created.
 */
@Composable
@NavigineMapComposable
public fun DottedPolyline(
    locationPolyline: LocationPolyline,
    color: Color,
    sizeWidth: Float,
    sizeHeight: Float,
    visible: Boolean = true,
    placement: Placement = Placement.SPACED,
    placementMinRatio: Float = 0f,
    collisionEnabled: Boolean? = null,
    placementSpacing: Float? = null,
    repeatDistance: Float? = null,
    repeatGroup: Int? = null,
    priority: Float? = null,
    title: String? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    state: DottedPolylineState? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
): Unit = DottedPolyline(
    locationPolyline = locationPolyline,
    colorArgb = color.toArgb(),
    sizeWidth = sizeWidth,
    sizeHeight = sizeHeight,
    visible = visible,
    placement = placement,
    placementMinRatio = placementMinRatio,
    collisionEnabled = collisionEnabled,
    placementSpacing = placementSpacing,
    repeatDistance = repeatDistance,
    repeatGroup = repeatGroup,
    priority = priority,
    title = title,
    alpha = alpha,
    interactive = interactive,
    state = state,
    onObjectReady = onObjectReady
)
