package com.navigine.locationview.objects.polyline


import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.PolylineMapObject
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.internal.node.ifValid
import com.navigine.locationview.objects.config.PolylineConfig
import com.navigine.locationview.utils.toRgbaF

/**
 * Declarative polyline.
 *
 * ## Basic Usage
 * ```kotlin
 * Polyline(
 *     points = polyline,
 *     color = Color.Blue,
 *     width = 3f
 * )
 * ```
 *
 * ## Advanced Usage
 * ```kotlin
 * Polyline(
 *     points = polyline,
 *     color = Color.Blue,
 *     width = 3f,
 *     config = PolylineConfig(
 *         appearance = AppearanceConfig(
 *             alpha = 0.8f,
 *             title = "Route 1"
 *         ),
 *         style = PolylineStyleConfig(
 *             capType = CapType.ROUND,
 *             joinType = JoinType.ROUND,
 *             miterLimit = 10f,
 *             order = 5
 *         ),
 *         outline = PolylineOutlineConfig(
 *             width = 5f,
 *             color = Color.Black,
 *             alpha = 0.5f
 *         )
 *     )
 * )
 * ```
 *
 * ## Lifecycle
 * - Creates SDK [PolylineMapObject] when entering composition
 * - Removes it when leaving composition
 * - Only changed properties trigger SDK updates on recomposition
 *
 * @param points Polyline geometry in map coordinates (required)
 * @param color Line color (required)
 * @param width Line width in pixels (required)
 * @param config Polyline configuration grouping all optional parameters
 * @param state Optional handle to observe id/type/data
 * @param onObjectReady Callback invoked once after creation with (id)
 *
 * @since 2.24.4
 */
@Composable
@NavigineMapComposable
public fun Polyline(
    points: LocationPolyline,
    color: Color,
    width: Float,
    config: PolylineConfig = PolylineConfig.Default,
    state: PolylineState? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<PolylineNode, LocationApplier>(
        factory = {
            val polyline = applier.window.addPolylineMapObject()
                ?: error("Error adding PolylineMapObject")

            // Apply geometry
            polyline.setPolyLine(points)

            // Apply color and width
            val (r, g, b, a) = color.toArgb().toRgbaF()
            polyline.setColor(r, g, b, a)
            polyline.setWidth(width)

            // Apply appearance
            polyline.setVisible(config.appearance.visible)
            polyline.setAlpha(config.appearance.alpha)
            config.appearance.title?.let { runCatching { polyline.setTitle(it) } }

            // Apply interaction
            polyline.setInteractive(config.interaction.interactive)

            // Apply style
            polyline.setOrder(config.style.order)
            polyline.setCapType(config.style.capType)
            polyline.setJoinType(config.style.joinType)
            polyline.setMiterLimit(config.style.miterLimit)

            // Apply outline
            config.outline?.let { outline ->
                polyline.setOutlineWidth(outline.width)
                val (or, og, ob, oa) = outline.color.toArgb().toRgbaF()
                polyline.setOutlineColor(or, og, ob, oa)
                polyline.setOutlineAlpha(outline.alpha)
                polyline.setOutlineCapType(outline.capType)
                polyline.setOutlineJoinType(outline.joinType)
                polyline.setOutlineMiterLimit(outline.miterLimit)
                polyline.setOutlineOrder(outline.order)
            }


            // bind optional handle
            state?.bind(polyline)
            onObjectReady?.invoke(polyline.id)

            PolylineNode(applier, polyline, state)
        },
        update = {
            // Geometry updates
            update(points) { p -> polyline.ifValid { setPolyLine(p) } }

            // Color and width updates
            update(color) { c ->
                polyline.ifValid {
                    val (r, g, b, a) = c.toArgb().toRgbaF()
                    setColor(r, g, b, a)
                }
            }
            update(width) { w -> polyline.ifValid { setWidth(w) } }

            // Config updates
            update(config.appearance) { appearance ->
                polyline.ifValid {
                    setVisible(appearance.visible)
                    setAlpha(appearance.alpha)
                    appearance.title?.let { runCatching { setTitle(it) } }
                }
            }

            update(config.interaction) { interaction ->
                polyline.ifValid { setInteractive(interaction.interactive) }
            }

            update(config.style) { style ->
                polyline.ifValid {
                    setOrder(style.order)
                    setCapType(style.capType)
                    setJoinType(style.joinType)
                    setMiterLimit(style.miterLimit)
                }
            }

            update(config.outline) { outline ->
                polyline.ifValid {
                    outline?.let { o ->
                        setOutlineWidth(o.width)
                        val (r, g, b, a) = o.color.toArgb().toRgbaF()
                        setOutlineColor(r, g, b, a)
                        setOutlineAlpha(o.alpha)
                        setOutlineCapType(o.capType)
                        setOutlineJoinType(o.joinType)
                        setOutlineMiterLimit(o.miterLimit)
                        setOutlineOrder(o.order)
                    }
                }
            }
        }
    )
}

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