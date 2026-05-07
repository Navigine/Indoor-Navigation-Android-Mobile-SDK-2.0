package com.navigine.locationview.objects.polygon

import android.R.attr.order
import android.R.attr.visible
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
import com.navigine.locationview.objects.config.PolygonConfig
import com.navigine.locationview.utils.toRgbaF


/**
 * Declarative polygon.
 *
 * ## Basic Usage
 * ```kotlin
 * Polygon(
 *     polygon = polygon,
 *     color = Color.Green
 * )
 * ```
 *
 * ## Advanced Usage
 * ```kotlin
 * Polygon(
 *     polygon = polygon,
 *     color = Color.Green,
 *     config = PolygonConfig(
 *         appearance = AppearanceConfig(
 *             alpha = 0.5f,
 *             title = "Building A"
 *         ),
 *         interaction = InteractionConfig(
 *             interactive = true,
 *             collisionEnabled = false
 *         ),
 *         order = 10
 *     )
 * )
 * ```
 *
 * ## Lifecycle
 * - Creates SDK [PolygonMapObject] when entering composition
 * - Removes it when leaving composition
 * - Only changed properties trigger SDK updates on recomposition
 *
 * @param polygon Polygon geometry in map coordinates (required)
 * @param color Fill color (required)
 * @param config Polygon configuration grouping all optional parameters
 * @param state Optional handle to observe id/type/data
 * @param onObjectReady Callback invoked once after creation with (id)
 *
 * @since 2.24.4
 */
@Composable
@NavigineMapComposable
public fun Polygon(
    polygon: LocationPolygon,
    color: Color,
    config: PolygonConfig = PolygonConfig.Default,
    state: PolygonState? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<PolygonNode, LocationApplier>(
        factory = {
            val obj = applier.window.addPolygonMapObject()
                ?: error("Error adding PolygonMapObject")

            obj.setPolygon(polygon)

            val (r, g, b, a) = color.toArgb().toRgbaF()
            obj.setColor(r, g, b, a)

            obj.setVisible(config.appearance.visible)
            obj.setAlpha(config.appearance.alpha)
            config.appearance.title?.let { runCatching { obj.setTitle(it) } }

            obj.setInteractive(config.interaction.interactive)

            obj.setOrder(config.order)

            state?.bind(obj)
            onObjectReady?.invoke(obj.id)
            PolygonNode(applier, obj, state)
        },
        update = {
            update(polygon) { p -> polygonObj.setPolygon(p) }

            update(color) { c ->
                val (r, g, b, a) = c.toArgb().toRgbaF()
                polygonObj.setColor(r, g, b, a)
            }

            update(config.appearance) { appearance ->
                polygonObj.setVisible(appearance.visible)
                polygonObj.setAlpha(appearance.alpha)
                appearance.title?.let { runCatching { polygonObj.setTitle(it) } }
            }

            update(config.interaction) { interaction ->
                polygonObj.setInteractive(interaction.interactive)
            }

            update(config.order) { order ->
                polygonObj.setOrder(order)
            }
        }
    )
}

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