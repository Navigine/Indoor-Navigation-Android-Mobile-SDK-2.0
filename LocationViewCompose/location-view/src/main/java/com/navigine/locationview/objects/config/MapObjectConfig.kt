package com.navigine.locationview.objects.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.navigine.idl.java.AnimationType
import com.navigine.idl.java.CapType
import com.navigine.idl.java.JoinType
import com.navigine.idl.java.Placement

// common configs - used across all map objects

/**
 * Appearance settings for map objects.
 *
 * @property visible Whether the object is visible on the map
 * @property alpha Overall opacity [0f..1f]
 * @property title Optional label/title for the object
 */
@Immutable
public data class AppearanceConfig(
    val visible: Boolean = true,
    val alpha: Float = 1f,
    val title: String? = null,
) {
    init {
        require(alpha in 0f..1f) { "Alpha must be in range [0f..1f], got $alpha" }
    }

    public companion object {
        public val Default: AppearanceConfig = AppearanceConfig()
    }
}

/**
 * Interaction settings for map objects.
 *
 * @property interactive Whether object responds to user taps/picks
 * @property collisionEnabled Whether object participates in collision detection
 */
@Immutable
public data class InteractionConfig(
    val interactive: Boolean = true,
    val collisionEnabled: Boolean = false,
) {
    public companion object {
        public val Default: InteractionConfig = InteractionConfig()
    }
}

/**
 * Rendering priority settings.
 *
 * @property priority Draw priority (higher = drawn on top)
 */
@Immutable
public data class RenderingConfig(
    val priority: Float = 0f,
) {
    public companion object {
        public val Default: RenderingConfig = RenderingConfig()
    }
}

/**
 * 2D size (width, height).
 */
@Immutable
public data class Size(
    val width: Float,
    val height: Float,
) {
    init {
        require(width >= 0) { "Width must be non-negative, got $width" }
        require(height >= 0) { "Height must be non-negative, got $height" }
    }
}

/**
 * 2D offset (x, y).
 */
@Immutable
public data class Offset(
    val x: Float,
    val y: Float,
)

/**
 * Position animation configuration.
 *
 * @property duration Animation duration in seconds
 * @property type Animation easing type
 */
@Immutable
public data class PositionAnimationConfig(
    val duration: Float = 0.5f,
    val type: AnimationType = AnimationType.LINEAR,
) {
    init {
        require(duration >= 0) { "Duration must be non-negative, got $duration" }
    }

    public companion object {
        public val Default: PositionAnimationConfig = PositionAnimationConfig()
    }
}

// icon configs

/**
 * Icon rotation configuration.
 *
 * @property angle Rotation angle in degrees
 * @property animated Whether rotation changes should be animated
 * @property duration Animation duration in seconds (if animated)
 * @property type Animation easing type (if animated)
 */
@Immutable
public data class RotationConfig(
    val angle: Float,
    val animated: Boolean = false,
    val duration: Float = 0.5f,
    val type: AnimationType = AnimationType.LINEAR,
) {
    init {
        if (animated) {
            require(duration >= 0) { "Duration must be non-negative, got $duration" }
        }
    }
}

/**
 * Icon-specific style settings.
 *
 * @property flat Whether to draw the icon flat (no perspective)
 */
@Immutable
public data class IconStyleConfig(
    val flat: Boolean = false,
) {
    public companion object {
        public val Default: IconStyleConfig = IconStyleConfig()
    }
}

/**
 * Complete icon configuration.
 *
 * Example:
 * ```kotlin
 * Icon(
 *     position = point,
 *     bitmap = bitmap,
 *     config = IconConfig(
 *         size = Size(64f, 64f),
 *         rotation = RotationConfig(angle = 45f),
 *         appearance = AppearanceConfig(alpha = 0.8f),
 *         animation = PositionAnimationConfig(
 *             duration = 0.5f,
 *             type = AnimationType.EASE_IN_OUT
 *         )
 *     )
 * )
 * ```
 *
 * @property appearance Visibility, alpha, title
 * @property interaction Interactive, collision
 * @property rendering Priority
 * @property size Icon dimensions in pixels
 * @property offset Position offset in pixels
 * @property buffer Buffer zone for interaction/layout
 * @property rotation Rotation angle and animation
 * @property animation Position animation settings
 * @property style Icon-specific style (flat, etc.)
 */
@Immutable
public data class IconConfig(
    val appearance: AppearanceConfig = AppearanceConfig.Default,
    val interaction: InteractionConfig = InteractionConfig.Default,
    val rendering: RenderingConfig = RenderingConfig.Default,
    val size: Size? = null,
    val offset: Offset? = null,
    val buffer: Size? = null,
    val rotation: RotationConfig? = null,
    val animation: PositionAnimationConfig? = null,
    val style: IconStyleConfig = IconStyleConfig.Default,
) {
    public companion object {
        public val Default: IconConfig = IconConfig()
    }
}

// circle configs

/**
 * Outline configuration for circles.
 *
 * @property radius Outline thickness in meters
 * @property color Outline color
 * @property alpha Outline opacity [0f..1f]
 */
@Immutable
public data class CircleOutlineConfig(
    val radius: Float,
    val color: Color = Color.Black,
    val alpha: Float = 1f,
) {
    init {
        require(radius >= 0) { "Radius must be non-negative, got $radius" }
        require(alpha in 0f..1f) { "Alpha must be in range [0f..1f], got $alpha" }
    }
}

/**
 * Complete circle configuration.
 *
 * Example:
 * ```kotlin
 * Circle(
 *     position = point,
 *     radius = 50f,
 *     color = Color.Blue,
 *     config = CircleConfig(
 *         appearance = AppearanceConfig(alpha = 0.5f),
 *         outline = CircleOutlineConfig(
 *             radius = 2f,
 *             color = Color.Red
 *         ),
 *         animation = PositionAnimationConfig(duration = 1f)
 *     )
 * )
 * ```
 *
 * @property appearance Visibility, alpha, title
 * @property interaction Interactive, collision
 * @property rendering Priority
 * @property offset Position offset in meters
 * @property buffer Buffer zone in meters
 * @property outline Outline configuration
 * @property animation Position animation settings
 */
@Immutable
public data class CircleConfig(
    val appearance: AppearanceConfig = AppearanceConfig.Default,
    val interaction: InteractionConfig = InteractionConfig.Default,
    val rendering: RenderingConfig = RenderingConfig.Default,
    val offset: Offset? = null,
    val buffer: Size? = null,
    val outline: CircleOutlineConfig? = null,
    val animation: PositionAnimationConfig? = null,
) {
    public companion object {
        public val Default: CircleConfig = CircleConfig()
    }
}

// polyline configs

/**
 * Polyline style configuration (cap, join, miter).
 *
 * @property capType Line cap style
 * @property joinType Line join style
 * @property miterLimit Miter limit for sharp angles
 * @property order Draw order
 */
@Immutable
public data class PolylineStyleConfig(
    val capType: CapType = CapType.BUTT,
    val joinType: JoinType = JoinType.MITER,
    val miterLimit: Float = 4f,
    val order: Int = 0,
) {
    init {
        require(miterLimit >= 1f) { "Miter limit must be >= 1, got $miterLimit" }
    }

    public companion object {
        public val Default: PolylineStyleConfig = PolylineStyleConfig()
    }
}

/**
 * Polyline outline configuration.
 *
 * @property width Outline width
 * @property color Outline color
 * @property alpha Outline opacity [0f..1f]
 * @property capType Outline cap style
 * @property joinType Outline join style
 * @property miterLimit Outline miter limit
 * @property order Outline draw order
 */
@Immutable
public data class PolylineOutlineConfig(
    val width: Float,
    val color: Color = Color.Black,
    val alpha: Float = 1f,
    val capType: CapType = CapType.BUTT,
    val joinType: JoinType = JoinType.MITER,
    val miterLimit: Float = 4f,
    val order: Int = 0,
) {
    init {
        require(width >= 0) { "Width must be non-negative, got $width" }
        require(alpha in 0f..1f) { "Alpha must be in range [0f..1f], got $alpha" }
        require(miterLimit >= 1f) { "Miter limit must be >= 1, got $miterLimit" }
    }
}

/**
 * Complete polyline configuration.
 *
 * Example:
 * ```kotlin
 * Polyline(
 *     points = points,
 *     color = Color.Blue,
 *     width = 3f,
 *     config = PolylineConfig(
 *         appearance = AppearanceConfig(alpha = 0.8f),
 *         style = PolylineStyleConfig(
 *             capType = CapType.ROUND,
 *             joinType = JoinType.ROUND
 *         ),
 *         outline = PolylineOutlineConfig(
 *             width = 5f,
 *             color = Color.Black
 *         )
 *     )
 * )
 * ```
 *
 * @property appearance Visibility, alpha, title
 * @property interaction Interactive
 * @property style Cap, join, miter, order
 * @property outline Outline configuration
 */
@Immutable
public data class PolylineConfig(
    val appearance: AppearanceConfig = AppearanceConfig.Default,
    val interaction: InteractionConfig = InteractionConfig(interactive = true, collisionEnabled = false),
    val style: PolylineStyleConfig = PolylineStyleConfig.Default,
    val outline: PolylineOutlineConfig? = null,
) {
    public companion object {
        public val Default: PolylineConfig = PolylineConfig()
    }
}

// dotted polyline configs

/**
 * Dot placement configuration.
 *
 * @property mode Placement mode (SPACED, etc.)
 * @property minRatio Minimum placement ratio
 * @property spacing Spacing between dots
 */
@Immutable
public data class PlacementConfig(
    val mode: Placement = Placement.SPACED,
    val minRatio: Float = 0f,
    val spacing: Float? = null,
) {
    init {
        require(minRatio >= 0) { "MinRatio must be non-negative, got $minRatio" }
        spacing?.let { require(it >= 0) { "Spacing must be non-negative, got $it" } }
    }

    public companion object {
        public val Default: PlacementConfig = PlacementConfig()
    }
}

/**
 * Dot repeat configuration.
 *
 * @property distance Repeat distance along the line
 * @property group Repeat group ID
 */
@Immutable
public data class RepeatConfig(
    val distance: Float,
    val group: Int? = null,
) {
    init {
        require(distance >= 0) { "Distance must be non-negative, got $distance" }
    }
}

/**
 * Complete dotted polyline configuration.
 *
 * Example:
 * ```kotlin
 * DottedPolyline(
 *     points = points,
 *     color = Color.Blue,
 *     dotSize = Size(4f, 4f),
 *     config = DottedPolylineConfig(
 *         appearance = AppearanceConfig(alpha = 0.8f),
 *         placement = PlacementConfig(
 *             mode = Placement.SPACED,
 *             spacing = 10f
 *         ),
 *         repeat = RepeatConfig(distance = 100f)
 *     )
 * )
 * ```
 *
 * @property appearance Visibility, alpha, title
 * @property interaction Interactive, collision
 * @property rendering Priority
 * @property placement Dot placement settings
 * @property repeat Dot repeat settings
 */
@Immutable
public data class DottedPolylineConfig(
    val appearance: AppearanceConfig = AppearanceConfig.Default,
    val interaction: InteractionConfig = InteractionConfig.Default,
    val rendering: RenderingConfig = RenderingConfig.Default,
    val placement: PlacementConfig = PlacementConfig.Default,
    val repeat: RepeatConfig? = null,
) {
    public companion object {
        public val Default: DottedPolylineConfig = DottedPolylineConfig()
    }
}

// polygon configs

/**
 * Complete polygon configuration.
 *
 * Example:
 * ```kotlin
 * Polygon(
 *     polygon = polygon,
 *     color = Color.Green,
 *     config = PolygonConfig(
 *         appearance = AppearanceConfig(
 *             alpha = 0.5f,
 *             title = "Building A"
 *         ),
 *         order = 10
 *     )
 * )
 * ```
 *
 * @property appearance Visibility, alpha, title
 * @property interaction Interactive
 * @property order Draw order
 */
@Immutable
public data class PolygonConfig(
    val appearance: AppearanceConfig = AppearanceConfig.Default,
    val interaction: InteractionConfig = InteractionConfig(interactive = true, collisionEnabled = false),
    val order: Int = 0,
) {
    public companion object {
        public val Default: PolygonConfig = PolygonConfig()
    }
}


// 3d model config

/**
 * Configuration for the [com.navigine.locationview.objects.model.Model] composable.
 *
 * Groups all optional parameters for a 3D model map object.
 * Only non-null fields are applied to the SDK object.
 *
 * ## Example
 * ```kotlin
 * ModelConfig(
 *     size = Size(1f, 1f),
 *     rotation = RotationConfig(angle = 90f),
 *     appearance = AppearanceConfig(alpha = 0.8f, title = "Chair"),
 *     rendering = RenderingConfig(priority = 5f)
 * )
 * ```
 *
 * @param size Width and height of the model in meters. If null, SDK default is used.
 * @param rotation Rotation angle configuration. Supports animated rotation.
 * @param appearance Visibility, alpha, and title of the object.
 * @param interaction Whether the object is interactive and participates in collision detection.
 * @param rendering Rendering priority of the object.
 * @param buffer Invisible buffer zone around the object for collision purposes.
 * @param animation Position change animation configuration.
 */
@Immutable
public data class ModelConfig(
    val size: Size? = null,
    val rotation: RotationConfig? = null,
    val appearance: AppearanceConfig = AppearanceConfig(),
    val interaction: InteractionConfig = InteractionConfig(),
    val rendering: RenderingConfig = RenderingConfig(),
    val buffer: Size? = null,
    val animation: PositionAnimationConfig? = null,
) {
    public companion object {
        public val Default: ModelConfig = ModelConfig()
    }
}