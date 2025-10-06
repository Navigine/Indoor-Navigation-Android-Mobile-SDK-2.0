package com.navigine.locationview.objects.polygon

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navigine.idl.java.LocationPolygon
import com.navigine.idl.java.PolygonMapObject
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.utils.toRgbaF


internal class PolygonNode(
    private val windowHandle: LocationApplier,
    val polygonObj: PolygonMapObject,
    private val state: PolygonState? = null
) : LocationNode {
    override fun onRemoved() {
        state?.unbind()
        runCatching { windowHandle.window.removePolygonMapObject(polygonObj) }
    }
    override fun onCleared() {
        state?.unbind()
        runCatching { windowHandle.window.removePolygonMapObject(polygonObj) }
    }
}

/**
 * Declarative polygon.
 *
 * Creates an SDK [PolygonMapObject] via LocationWindow.addPolygonMapObject()
 * and removes it via LocationWindow.removePolygonMapObject() when leaving composition.
 * Properties are applied idempotently; updates are sent only when values change.
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
 * @param state Optional handle to access SDK id/type/data or (opt-in) raw object.
 * @param polygon Geometry to render (required).
 * @param fillColorArgb Fill color as ARGB Int. Converted internally to RGBA floats.
 * @param visible Visibility flag (default true).
 * @param order Optional draw order.
 * @param title Optional label/title.
 * @param alpha Optional overall alpha [0..1].
 * @param interactive Optional pick/hit-test flag.
 * @param onObjectReady Optional callback fired once the object is created (passes SDK id).
 */
@Composable
@NavigineMapComposable
public fun Polygon(
    polygon: LocationPolygon,
    @ColorInt fillColorArgb: Int,
    visible: Boolean = true,
    state: PolygonState? = null,
    order: Int? = null,
    title: String? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<PolygonNode, LocationApplier>(
        factory = {
            val obj = applier.window.addPolygonMapObject()
                ?: error("Error adding PolygonMapObject")

            obj.setPolygon(polygon)
            run {
                val (r, g, b, a) = fillColorArgb.toRgbaF()
                obj.setColor(r, g, b, a)
            }
            obj.setVisible(visible)
            order?.let { obj.setOrder(it) }
            title?.let { runCatching { obj.setTitle(it) } }
            alpha?.let { obj.setAlpha(it) }
            interactive?.let { obj.setInteractive(it) }

            state?.bind(obj)
            onObjectReady?.invoke(obj.id)
            PolygonNode(applier, obj, state)
        },
        update = {
            update(polygon) { p -> polygonObj.setPolygon(p) }

            update(fillColorArgb) { cInt ->
                val (r, g, b, a) = cInt.toRgbaF()
                polygonObj.setColor(r, g, b, a)
            }

            update(visible) { v -> polygonObj.setVisible(v) }
            update(order) { o -> o?.let { polygonObj.setOrder(it) } }
            update(title) { t -> t?.let { runCatching { polygonObj.setTitle(it) } } }
            update(alpha) { a -> a?.let { polygonObj.setAlpha(a) } }
            update(interactive) { i -> i?.let { polygonObj.setInteractive(i) } }
        }
    )
}

/**
 * Declarative polygon.
 *
 * Creates an SDK [PolygonMapObject] via LocationWindow.addPolygonMapObject()
 * and removes it via LocationWindow.removePolygonMapObject() when leaving composition.
 * Properties are applied idempotently; updates are sent only when values change.
 *
 * Color:
 * - [fillColor] is converted to RGBA floats in [0f..1f] and passed to the SDK as (r,g,b,a).
 *
 * Visibility & Interaction:
 * - [visible] toggles drawing; the object remains allocated until removed from composition.
 * - [interactive] enables pick/hit-test.
 *
 * Advanced:
 * - [state] provides a read-only handle with id/type/data. The raw SDK object should
 *   not be retained; composition owns its lifecycle.
 *
 * @param state Optional handle to access SDK id/type/data or (opt-in) raw object.
 * @param polygon Geometry to render (required).
 * @param fillColor Fill color of type [Color]. Converted internally to RGBA floats.
 * @param visible Visibility flag (default true).
 * @param order Optional draw order.
 * @param title Optional label/title.
 * @param alpha Optional overall alpha [0..1].
 * @param interactive Optional pick/hit-test flag.
 * @param onObjectReady Optional callback fired once the object is created (passes SDK id).
 */
@Composable
@NavigineMapComposable
public fun Polygon(
    polygon: LocationPolygon,
    fillColor: Color,
    visible: Boolean = true,
    state: PolygonState? = null,
    order: Int? = null,
    title: String? = null,
    alpha: Float? = null,
    interactive: Boolean? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
): Unit = Polygon(
    polygon = polygon,
    fillColorArgb = fillColor.toArgb(),
    visible = visible,
    state = state,
    order = order,
    title = title,
    alpha = alpha,
    interactive = interactive,
    onObjectReady = onObjectReady
)

