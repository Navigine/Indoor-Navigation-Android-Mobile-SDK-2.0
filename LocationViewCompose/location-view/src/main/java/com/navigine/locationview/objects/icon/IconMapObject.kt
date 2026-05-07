package com.navigine.locationview.objects.icon

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import com.navigine.idl.java.IconMapObject
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.MapObjectType
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.objects.config.IconConfig

/**
 * Declarative icon map object for Navigine.
 *
 * ## Basic Usage
 * ```kotlin
 * Icon(
 *     position = LocationPoint(100.0, 200.0),
 *     bitmap = myBitmap
 * )
 * ```
 *
 * ## Advanced Usage
 * ```kotlin
 * Icon(
 *     position = point,
 *     bitmap = bitmap,
 *     config = IconConfig(
 *         size = Size(64f, 64f),
 *         rotation = RotationConfig(
 *             angle = 45f,
 *             animated = true,
 *             duration = 0.5f
 *         ),
 *         appearance = AppearanceConfig(
 *             visible = true,
 *             alpha = 0.8f,
 *             title = "My Location"
 *         ),
 *         interaction = InteractionConfig(
 *             interactive = true,
 *             collisionEnabled = false
 *         ),
 *         animation = PositionAnimationConfig(
 *             duration = 0.5f,
 *             type = AnimationType.EASE_IN_OUT
 *         ),
 *         style = IconStyleConfig(flat = false)
 *     )
 * )
 * ```
 *
 * ## Lifecycle
 * - Creates SDK [IconMapObject] when entering composition
 * - Removes it when leaving composition
 * - Only changed properties trigger SDK updates on recomposition
 *
 * @param position Icon position in location coordinates (required)
 * @param bitmap Icon image (optional, can be set later)
 * @param config Icon configuration grouping all optional parameters
 * @param animatePosition If true, position changes will be animated
 * @param state Optional handle to observe id/type/data
 * @param onObjectReady Callback invoked once after creation with (id, type)
 *
 * @since 2.24.4
 */
@Composable
@NavigineMapComposable
public fun Icon(
    position: LocationPoint,
    bitmap: Bitmap? = null,
    config: IconConfig = IconConfig.Default,
    animatePosition: Boolean = false,
    state: IconState? = null,
    onObjectReady: ((id: Int, type: MapObjectType) -> Unit)? = null
) {

    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<IconNode, LocationApplier>(
        factory = {
            val icon = applier.window.addIconMapObject() ?: error("Failed to add icon")

            if (animatePosition && config.animation != null) {
                icon.setPositionAnimated(
                    position,
                    config.animation.duration,
                    config.animation.type
                )
            } else {
                icon.setPosition(position)
            }

            bitmap?.let { icon.setBitmap(it) }

            config.size?.let { icon.setSize(it.width, it.height) }

            config.rotation?.let { rotation ->
                if (rotation.animated) {
                    icon.setAngleAnimated(rotation.angle, rotation.duration, rotation.type)
                } else {
                    icon.setAngle(rotation.angle)
                }
            }

            icon.setVisible(config.appearance.visible)
            icon.setAlpha(config.appearance.alpha)
            config.appearance.title?.let { icon.setTitle(it) }

            icon.setInteractive(config.interaction.interactive)
            icon.setCollisionEnabled(config.interaction.collisionEnabled)

            icon.setPriority(config.rendering.priority)

            config.offset?.let { icon.setOffset(it.x, it.y) }

            config.buffer?.let { icon.setBuffer(it.width, it.height) }

            icon.setFlat(config.style.flat)

            state?.bind(icon)
            onObjectReady?.invoke(icon.id, icon.type)
            IconNode(applier, icon, state)
        },
        update = {
            update(position) { p ->
                if (animatePosition && config.animation != null) {
                    icon.setPositionAnimated(p, config.animation.duration, config.animation.type)
                } else {
                    icon.setPosition(p)
                }
            }
            update(bitmap) { b -> if (b != null) icon.setBitmap(b) }

            // Config updates - only update if config changed
            update(config.size) { size ->
                size?.let { icon.setSize(it.width, it.height) }
            }

            update(config.rotation) { rotation ->
                rotation?.let { r ->
                    if (r.animated) {
                        icon.setAngleAnimated(r.angle, r.duration, r.type)
                    } else {
                        icon.setAngle(r.angle)
                    }
                }
            }

            update(config.appearance) { appearance ->
                icon.setVisible(appearance.visible)
                icon.setAlpha(appearance.alpha)
                appearance.title?.let { icon.setTitle(it) }
            }

            update(config.interaction) { interaction ->
                icon.setInteractive(interaction.interactive)
                icon.setCollisionEnabled(interaction.collisionEnabled)
            }

            update(config.rendering) { rendering ->
                icon.setPriority(rendering.priority)
            }

            update(config.offset) { offset ->
                offset?.let { icon.setOffset(it.x, it.y) }
            }

            update(config.buffer) { buffer ->
                buffer?.let { icon.setBuffer(it.width, it.height) }
            }

            update(config.style) { style ->
                icon.setFlat(style.flat)
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
