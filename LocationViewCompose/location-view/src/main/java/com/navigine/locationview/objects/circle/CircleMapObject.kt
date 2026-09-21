package com.navigine.locationview.objects.circle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navigine.idl.java.CircleMapObject
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.geometry.NavPosition
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.internal.node.ifValid
import com.navigine.locationview.objects.config.CircleConfig


/**
 * Declarative circle map object for Navigine.
 *
 * ## Basic Usage — indoor
 * ```kotlin
 * Circle(
 *     position = NavPosition.indoor(GlobalPoint(55.751244, 37.618423), sublocationId = 3),
 *     radius = 50f,
 *     color = Color.Blue
 * )
 * ```
 *
 * ## Basic Usage — outdoor
 * ```kotlin
 * Circle(
 *     position = NavPosition.outdoor(GlobalPoint(55.751244, 37.618423)),
 *     radius = 50f,
 *     color = Color.Blue
 * )
 * ```
 *
 * ## Advanced Usage
 * ```kotlin
 * Circle(
 *     position = position,
 *     radius = 50f,
 *     color = Color.Blue,
 *     config = CircleConfig(
 *         appearance = AppearanceConfig(
 *             alpha = 0.5f,
 *             title = "Zone A"
 *         ),
 *         outline = CircleOutlineConfig(
 *             radius = 2f,
 *             color = Color.Red,
 *             alpha = 1f
 *         ),
 *         animation = PositionAnimationConfig(
 *             duration = 1f,
 *             type = AnimationType.EASE_IN_OUT
 *         )
 *     )
 * )
 * ```
 *
 * ## Lifecycle
 * - Creates SDK [CircleMapObject] when entering composition
 * - Removes it when leaving composition
 * - Only changed properties trigger SDK updates on recomposition
 *
 * @param position Circle center position. Use [NavPosition.indoor] for a point tied to a
 * specific floor, or [NavPosition.outdoor] for a point on the global (outdoor) map.
 * (required)
 * @param radius Circle radius in meters (required)
 * @param color Fill color (required)
 * @param config Circle configuration grouping all optional parameters
 * @param animatePosition If true, position changes will be animated
 * @param state Optional handle to observe id/type/data
 * @param onObjectReady Callback invoked once after creation with (id)
 *
 * @since 2.24.4
 */
@Composable
@NavigineMapComposable
public fun Circle(
    position: NavPosition,
    radius: Float,
    color: Color,
    config: CircleConfig = CircleConfig.Default,
    animatePosition: Boolean = false,
    state: CircleState? = null,
    onObjectReady: ((id: Int) -> Unit)? = null,
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<CircleNode, LocationApplier>(
        factory = {
            val circle = applier.window.addCircleMapObject()
                ?: error("Error adding CircleMapObject")

            if (animatePosition && config.animation != null) {
                circle.setPositionAnimated(
                    position.point,
                    position.sublocationId,
                    config.animation.duration,
                    config.animation.type
                )
            } else {
                circle.setPosition(position.point, position.sublocationId)
            }

            circle.setRadius(radius)

            circle.setColor(color.toArgb())

            circle.setVisible(config.appearance.visible)
            circle.setAlpha(config.appearance.alpha)
            config.appearance.title?.let { runCatching { circle.setTitle(it) } }

            circle.setInteractive(config.interaction.interactive)
            circle.setCollisionEnabled(config.interaction.collisionEnabled)

            circle.setPriority(config.rendering.priority)

            config.offset?.let { circle.setOffset(it.x, it.y) }

            config.buffer?.let { circle.setBuffer(it.width, it.height) }

            config.outline?.let { outline ->
                circle.setOutlineRadius(outline.radius)
                circle.setOutlineColor(outline.color.toArgb())
                circle.setOutlineAlpha(outline.alpha)
            }

            state?.bind(circle)
            onObjectReady?.invoke(circle.id)
            CircleNode(applier, circle, state)
        },
        update = {
            update(position) { p ->
                if (!circle.isValid) return@update
                if (animatePosition && config.animation != null) {
                    circle.setPositionAnimated(p.point, p.sublocationId, config.animation.duration, config.animation.type)
                } else {
                    circle.setPosition(p.point, p.sublocationId)
                }
            }

            update(radius) { r -> circle.ifValid { setRadius(r) } }
            update(color) { c ->
                circle.ifValid {
                    setColor(c.toArgb())
                }
            }

            update(config.appearance) { appearance ->
                circle.ifValid {
                    setVisible(appearance.visible)
                    setAlpha(appearance.alpha)
                    appearance.title?.let { runCatching { setTitle(it) } }
                }
            }

            update(config.interaction) { interaction ->
                circle.ifValid {
                    setInteractive(interaction.interactive)
                    setCollisionEnabled(interaction.collisionEnabled)
                }
            }

            update(config.rendering) { rendering ->
                circle.ifValid { setPriority(rendering.priority) }
            }

            update(config.offset) { offset ->
                circle.ifValid { offset?.let { setOffset(it.x, it.y) } }
            }

            update(config.buffer) { buffer ->
                circle.ifValid { buffer?.let { setBuffer(it.width, it.height) } }
            }

            update(config.outline) { outline ->
                circle.ifValid {
                    outline?.let { o ->
                        setOutlineRadius(o.radius)
                        setOutlineColor(o.color.toArgb())
                        setOutlineAlpha(o.alpha)
                    }
                }
            }
        }
    )
}

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