package com.navigine.locationview.objects.polyline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navigine.idl.java.DottedPolylineMapObject
import com.navigine.idl.java.LocationPolyline
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.objects.config.DottedPolylineConfig
import com.navigine.locationview.objects.config.Size
import com.navigine.locationview.utils.toRgbaF

/**
 * Declarative dotted polyline.
 *
 * ## Basic Usage
 * ```kotlin
 * DottedPolyline(
 *     points = polyline,
 *     color = Color.Blue,
 *     dotSize = Size(4f, 4f)
 * )
 * ```
 *
 * ## Advanced Usage
 * ```kotlin
 * DottedPolyline(
 *     points = polyline,
 *     color = Color.Blue,
 *     dotSize = Size(4f, 4f),
 *     config = DottedPolylineConfig(
 *         appearance = AppearanceConfig(
 *             alpha = 0.8f,
 *             title = "Dashed Route"
 *         ),
 *         placement = PlacementConfig(
 *             mode = Placement.SPACED,
 *             spacing = 10f,
 *             minRatio = 0.5f
 *         ),
 *         repeat = RepeatConfig(
 *             distance = 100f,
 *             group = 1
 *         ),
 *         rendering = RenderingConfig(priority = 10f)
 *     )
 * )
 * ```
 *
 * ## Lifecycle
 * - Creates SDK [DottedPolylineMapObject] when entering composition
 * - Removes it when leaving composition
 * - Only changed properties trigger SDK updates on recomposition
 *
 * @param points Polyline geometry in map coordinates (required)
 * @param color Dot color (required)
 * @param dotSize Dot dimensions (width, height) (required)
 * @param config Dotted polyline configuration grouping all optional parameters
 * @param state Optional handle to observe id/type/data
 * @param onObjectReady Callback invoked once after creation with (id)
 *
 * @since 2.24.4
 */
@Composable
@NavigineMapComposable
public fun DottedPolyline(
    points: LocationPolyline,
    color: Color,
    dotSize: Size,
    config: DottedPolylineConfig = DottedPolylineConfig.Default,
    state: DottedPolylineState? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<DottedPolylineNode, LocationApplier>(
        factory = {
            val dotted = applier.window.addDottedPolylineMapObject()
                ?: error("Error adding DottedPolylineMapObject")

            dotted.setPolyLine(points)

            val (r, g, b, a) = color.toArgb().toRgbaF()
            dotted.setColor(r, g, b, a)
            dotted.setSize(dotSize.width, dotSize.height)

            dotted.setVisible(config.appearance.visible)
            dotted.setAlpha(config.appearance.alpha)
            config.appearance.title?.let { runCatching { dotted.setTitle(it) } }

            dotted.setInteractive(config.interaction.interactive)
            dotted.setCollisionEnabled(config.interaction.collisionEnabled)

            dotted.setPriority(config.rendering.priority)

            dotted.setPlacement(config.placement.mode)
            dotted.setPlacementMinRatio(config.placement.minRatio)
            config.placement.spacing?.let { dotted.setPlacementSpacing(it) }

            config.repeat?.let { repeat ->
                dotted.setRepeatDistance(repeat.distance)
                repeat.group?.let { dotted.setRepeatGroup(it) }
            }

            state?.bind(dotted)
            onObjectReady?.invoke(dotted.id)

            DottedPolylineNode(applier, dotted, state)
        },
        update = {
            update(points) { p -> dotted.setPolyLine(p) }

            update(color) { c ->
                val (r, g, b, a) = c.toArgb().toRgbaF()
                dotted.setColor(r, g, b, a)
            }
            update(dotSize) { size ->
                dotted.setSize(size.width, size.height)
            }

            update(config.appearance) { appearance ->
                dotted.setVisible(appearance.visible)
                dotted.setAlpha(appearance.alpha)
                appearance.title?.let { runCatching { dotted.setTitle(it) } }
            }

            update(config.interaction) { interaction ->
                dotted.setInteractive(interaction.interactive)
                dotted.setCollisionEnabled(interaction.collisionEnabled)
            }

            update(config.rendering) { rendering ->
                dotted.setPriority(rendering.priority)
            }

            update(config.placement) { placement ->
                dotted.setPlacement(placement.mode)
                dotted.setPlacementMinRatio(placement.minRatio)
                placement.spacing?.let { dotted.setPlacementSpacing(it) }
            }

            update(config.repeat) { repeat ->
                repeat?.let { r ->
                    dotted.setRepeatDistance(r.distance)
                    r.group?.let { dotted.setRepeatGroup(it) }
                }
            }
        }
    )
}

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