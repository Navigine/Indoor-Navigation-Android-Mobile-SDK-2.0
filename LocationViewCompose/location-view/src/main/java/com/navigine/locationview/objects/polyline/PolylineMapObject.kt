package com.navigine.locationview.objects.polyline

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navigine.idl.java.CapType
import com.navigine.idl.java.JoinType
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.PolylineMapObject
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.utils.toRgbaF

/** Internal node owning a single polyline object. */
internal class PolylineNode(
    private val windowHandle: LocationApplier,
    val polyline: PolylineMapObject,
    private val state: PolylineState? = null
) : LocationNode {
    override fun onRemoved() {
        state?.unbind()
        runCatching { windowHandle.window.removePolylineMapObject(polyline) }
    }
    override fun onCleared() {
        state?.unbind()
        runCatching { windowHandle.window.removePolylineMapObject(polyline) }
    }
}

/**
 * Declarative polyline.
 *
 * - Creates an SDK [PolylineMapObject] via LocationWindow.addPolylineMapObject()
 *   and removes it via LocationWindow.removePolylineMapObject() when leaving composition.
 * - Properties are applied idempotently; updates are sent only when values change.
 *
 * Color:
 * - [colorArgb] is converted to RGBA floats in [0f..1f] and passed to the SDK as (r,g,b,a).
 * - Outline color/alpha are set via [outlineColorArgb]/[outlineAlpha] when provided.
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
 * @param width dot width (px in SDK units).
 * @param visible visibility flag (default true).
 * @param state optional handle for id/type/data (and opt-in raw object).
 * @param title optional title/label if supported by SDK.
 * @param alpha overall alpha [0..1] (optional).
 * @param interactive if true, object can be picked/hit-tested (optional).
 * @param onObjectReady optional callback fired once the object is created.
 * @param order order (optional).
 * @param capType cap type (optional).
 * @param joinType join type (optional).
 * @param miterLimit miter limit (optional).
 * @param outlineWidth outline width (optional).
 * @param outlineColorArgb outline color; converted internally to RGBA floats required by SDK (optional).
 * @param outlineAlpha outline alpha (optional).
 * @param outlineCapType outline cap type (optional).
 * @param outlineJoinType outline join type (optional).
 * @param outlineMiterLimit outline miter limit (optional).
 * @param outlineOrder outline order (optional).
 * @param onObjectReady optional callback fired once the object is created.
 */
@Composable
@NavigineMapComposable
public fun Polyline(
    locationPolyline: LocationPolyline,
    @ColorInt colorArgb: Int,
    width: Float,
    visible: Boolean = true,
    state: PolylineState? = null,
    order: Int? = null,
    capType: CapType? = null,
    joinType: JoinType? = null,
    miterLimit: Float? = null,
    outlineWidth: Float? = null,
    @ColorInt outlineColorArgb: Int? = null,
    outlineAlpha: Float? = null,
    outlineCapType: CapType? = null,
    outlineJoinType: JoinType? = null,
    outlineMiterLimit: Float? = null,
    outlineOrder: Int? = null,
    title: String? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<PolylineNode, LocationApplier>(
        factory = {
            val poly = applier.window.addPolylineMapObject()
                ?: error("Error adding PolylineMapObject")


            // initial apply
            poly.setPolyLine(locationPolyline)
            run {
                val (r, g, b, a) = colorArgb.toRgbaF()
                poly.setColor(r, g, b, a)
            }
            poly.setWidth(width)
            poly.setVisible(visible)
            title?.let { runCatching { poly.setTitle(it) } }
            order?.let { poly.setOrder(it) }
            capType?.let { poly.setCapType(it) }
            joinType?.let { poly.setJoinType(it) }
            miterLimit?.let { poly.setMiterLimit(it) }
            outlineWidth?.let { poly.setOutlineWidth(it) }
            outlineColorArgb?.let { cInt ->
                val (r, g, b, a) = cInt.toRgbaF()
                poly.setOutlineColor(r, g, b, a)
            }
            outlineAlpha?.let { poly.setOutlineAlpha(it) }
            outlineCapType?.let { poly.setOutlineCapType(it) }
            outlineJoinType?.let { poly.setOutlineJoinType(it) }
            outlineMiterLimit?.let { poly.setOutlineMiterLimit(it) }
            outlineOrder?.let { poly.setOutlineOrder(it) }
            alpha?.let { poly.setAlpha(it) }
            interactive?.let { poly.setInteractive(it) }

            // bind optional handle
            state?.bind(poly)
            onObjectReady?.invoke(poly.id)

            PolylineNode(applier, poly, state)
        },
        update = {
            // Points (structural equality) — update only when changed
            update(locationPolyline) { p -> polyline.setPolyLine(p) }
            update(colorArgb) { cInt ->
                val (r, g, b, a) = cInt.toRgbaF()
                polyline.setColor(r, g, b, a)
            }
            update(width) { w -> polyline.setWidth(w) }
            update(visible) { v -> polyline.setVisible(v) }
            update(title) { t -> t?.let { runCatching { polyline.setTitle(it) } } }
            update(order) { o -> o?.let { polyline.setOrder(it) } }
            update(capType) { c -> c?.let { polyline.setCapType(it) } }
            update(joinType) { j -> j?.let { polyline.setJoinType(it) } }
            update(miterLimit) { m -> m?.let { polyline.setMiterLimit(it) } }
            update(outlineWidth) { w -> w?.let { polyline.setOutlineWidth(it) } }
            update(outlineColorArgb) { cInt ->
                if (cInt != null) {
                    val (r, g, b, a) = cInt.toRgbaF()
                    polyline.setOutlineColor(r, g, b, a)
                }
            }
            update(outlineAlpha) { a -> a?.let { polyline.setOutlineAlpha(it) } }
            update(outlineCapType) { c -> c?.let { polyline.setOutlineCapType(it) } }
            update(outlineJoinType) { j -> j?.let { polyline.setOutlineJoinType(it) } }
            update(outlineMiterLimit) { m -> m?.let { polyline.setOutlineMiterLimit(it) } }
            update(outlineOrder) { o -> o?.let { polyline.setOutlineOrder(it) } }
            update(alpha) { a -> a?.let { polyline.setAlpha(it) } }
            update(interactive) { i -> i?.let { polyline.setInteractive(it) } }
        }
    )
}

/**
 * Declarative polyline.
 *
 * - Creates an SDK [PolylineMapObject] via LocationWindow.addPolylineMapObject()
 *   and removes it via LocationWindow.removePolylineMapObject() when leaving composition.
 * - Properties are applied idempotently; updates are sent only when values change.
 *
 * Color:
 * - [color] is converted to RGBA floats in [0f..1f] and passed to the SDK as (r,g,b,a).
 * - Outline color/alpha are set via [outlineColor]/[outlineAlpha] when provided.
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
 * @param fillColor color of type [Color]; converted internally to RGBA floats required by SDK.
 * @param width dot width (px in SDK units).
 * @param visible visibility flag (default true).
 * @param state optional handle for id/type/data (and opt-in raw object).
 * @param title optional title/label if supported by SDK.
 * @param alpha overall alpha [0..1] (optional).
 * @param interactive if true, object can be picked/hit-tested (optional).
 * @param onObjectReady optional callback fired once the object is created.
 * @param order order (optional).
 * @param capType cap type (optional).
 * @param joinType join type (optional).
 * @param miterLimit miter limit (optional).
 * @param outlineWidth outline width (optional).
 * @param outlineColor outline color of type [Color]; converted internally to RGBA floats required by SDK (optional).
 * @param outlineAlpha outline alpha (optional).
 * @param outlineCapType outline cap type (optional).
 * @param outlineJoinType outline join type (optional).
 * @param outlineMiterLimit outline miter limit (optional).
 * @param outlineOrder outline order (optional).
 * @param onObjectReady optional callback fired once the object is created.
 */
@Composable
@NavigineMapComposable
public fun Polyline(
    locationPolyline: LocationPolyline,
    fillColor: Color,
    width: Float,
    visible: Boolean = true,
    state: PolylineState? = null,
    order: Int? = null,
    capType: CapType? = null,
    joinType: JoinType? = null,
    miterLimit: Float? = null,
    outlineWidth: Float? = null,
    outlineColor: Color? = null,
    outlineAlpha: Float? = null,
    outlineCapType: CapType? = null,
    outlineJoinType: JoinType? = null,
    outlineMiterLimit: Float? = null,
    outlineOrder: Int? = null,
    title: String? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) : Unit = Polyline(
    locationPolyline = locationPolyline,
    colorArgb = fillColor.toArgb(),
    width = width,
    visible = visible,
    state = state,
    order = order,
    capType = capType,
    joinType = joinType,
    miterLimit = miterLimit,
    outlineWidth = outlineWidth,
    outlineColorArgb = outlineColor?.toArgb(),
    outlineAlpha = outlineAlpha,
    outlineCapType = outlineCapType,
    outlineJoinType = outlineJoinType,
    outlineMiterLimit = outlineMiterLimit,
    outlineOrder = outlineOrder,
    title = title,
    alpha = alpha,
    interactive = interactive,
    onObjectReady = onObjectReady,
)
