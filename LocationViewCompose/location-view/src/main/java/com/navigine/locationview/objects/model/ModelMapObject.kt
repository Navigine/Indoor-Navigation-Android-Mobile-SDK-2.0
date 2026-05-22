package com.navigine.locationview.objects.model


import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.MapObjectType
import com.navigine.idl.java.ModelMapObject
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationNode
import com.navigine.locationview.objects.config.ModelConfig
import com.navigine.model.ModelProvider

/**
 * Declarative 3D model map object for Navigine.
 *
 * ## Basic Usage
 * ```kotlin
 * Model(
 *     position = LocationPoint(100.0, 200.0),
 *     model = ModelProvider.fromAsset(context, "models/chair.glb", texture)
 * )
 * ```
 *
 * ## Loading from different sources
 * ```kotlin
 * // From a raw resource
 * Model(
 *     position = point,
 *     model = ModelProvider.fromResource(context, R.raw.chair, texture)
 * )
 *
 * // From a file path
 * Model(
 *     position = point,
 *     model = ModelProvider.fromFile("/path/to/model.glb", texture)
 * )
 *
 * // From a byte array
 * Model(
 *     position = point,
 *     model = ModelProvider.fromByteArray(modelBytes, texture)
 * )
 * ```
 *
 * ## Advanced Usage
 * ```kotlin
 * Model(
 *     position = point,
 *     model = ModelProvider.fromAsset(context, "models/chair.glb", texture),
 *     config = ModelConfig(
 *         size = Size(1f, 1f),
 *         rotation = RotationConfig(angle = 90f),
 *         appearance = AppearanceConfig(alpha = 0.8f, title = "Chair"),
 *         interaction = InteractionConfig(interactive = true),
 *         rendering = RenderingConfig(priority = 5f),
 *         animation = PositionAnimationConfig(
 *             duration = 0.5f,
 *             type = AnimationType.EASE_IN_OUT
 *         )
 *     )
 * )
 * ```
 *
 * ## Lifecycle
 * - Creates SDK [ModelMapObject] when entering composition
 * - Removes it when leaving composition
 * - Only changed properties trigger SDK updates on recomposition
 *
 * @param position Model position in location coordinates (required)
 * @param model 3D model provider (optional, can be set later).
 * Use [ModelProvider.fromAsset], [ModelProvider.fromResource],
 * [ModelProvider.fromFile], or [ModelProvider.fromByteArray] to create one.
 * @param config Model configuration grouping all optional parameters
 * @param animatePosition If true, position changes will be animated
 * @param state Optional handle to observe id/type/data
 * @param onObjectReady Callback invoked once after creation with (id, type)
 *
 * @since 2.25.0
 */
@Composable
@NavigineMapComposable
public fun Model(
    position: LocationPoint,
    model: ModelProvider? = null,
    config: ModelConfig = ModelConfig.Default,
    animatePosition: Boolean = false,
    state: ModelState? = null,
    onObjectReady: ((id: Int, type: MapObjectType) -> Unit)? = null
) {
    val applier = currentComposer.applier as? LocationApplier
        ?: return

    ComposeNode<ModelNode, LocationApplier>(
        factory = {
            val obj = applier.window.addModelMapObject()
                ?: error("Failed to add ModelMapObject")

            if (animatePosition && config.animation != null) {
                obj.setPositionAnimated(
                    position,
                    config.animation.duration,
                    config.animation.type
                )
            } else {
                obj.setPosition(position)
            }

            model?.let { obj.setModel(it) }

            config.size?.let { obj.setSize(it.width, it.height) }

            config.rotation?.let { rotation ->
                if (rotation.animated) {
                    obj.setAngleAnimated(rotation.angle, rotation.duration, rotation.type)
                } else {
                    obj.setAngle(rotation.angle)
                }
            }

            obj.setVisible(config.appearance.visible)
            obj.setAlpha(config.appearance.alpha)
            config.appearance.title?.let { obj.setTitle(it) }

            obj.setInteractive(config.interaction.interactive)
            obj.setCollisionEnabled(config.interaction.collisionEnabled)

            obj.setPriority(config.rendering.priority)

            config.buffer?.let { obj.setBuffer(it.width, it.height) }

            state?.bind(obj)
            onObjectReady?.invoke(obj.id, obj.type)
            ModelNode(applier, obj, state)
        },
        update = {
            update(position) { p ->
                if (animatePosition && config.animation != null) {
                    obj.setPositionAnimated(p, config.animation.duration, config.animation.type)
                } else {
                    obj.setPosition(p)
                }
            }

            update(model) { m -> m?.let { obj.setModel(it) } }

            update(config.size) { size ->
                size?.let { obj.setSize(it.width, it.height) }
            }

            update(config.rotation) { rotation ->
                rotation?.let { r ->
                    if (r.animated) {
                        obj.setAngleAnimated(r.angle, r.duration, r.type)
                    } else {
                        obj.setAngle(r.angle)
                    }
                }
            }

            update(config.appearance) { appearance ->
                obj.setVisible(appearance.visible)
                obj.setAlpha(appearance.alpha)
                appearance.title?.let { obj.setTitle(it) }
            }

            update(config.interaction) { interaction ->
                obj.setInteractive(interaction.interactive)
                obj.setCollisionEnabled(interaction.collisionEnabled)
            }

            update(config.rendering) { rendering ->
                obj.setPriority(rendering.priority)
            }

            update(config.buffer) { buffer ->
                buffer?.let { obj.setBuffer(it.width, it.height) }
            }
        }
    )
}

internal class ModelNode(
    private val windowHandle: LocationApplier,
    val obj: ModelMapObject,
    private val state: ModelState?
) : LocationNode {
    override fun onRemoved() {
        state?.unbind()
        runCatching { windowHandle.window.removeModelMapObject(obj) }
    }

    override fun onCleared() {
        state?.unbind()
        runCatching { windowHandle.window.removeModelMapObject(obj) }
    }
}