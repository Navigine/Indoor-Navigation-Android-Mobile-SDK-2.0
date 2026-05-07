package com.navigine.locationview.settings

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Controls which built-in widgets are shown in [DefaultNavigineLocation].
 *
 * @param showZoomControls Whether to show zoom in/out buttons.
 * @param showFollowMe Whether to show the "follow me" button.
 * @param showFloorSelector Whether to show the floor selector (only visible when a building with
 * multiple floors is in focus).
 */
@Immutable
public data class DefaultWidgetVisibility(
    val showZoomControls: Boolean = true,
    val showFollowMe: Boolean = true,
    val showFloorSelector: Boolean = true,
)

/**
 * Appearance configuration for the "follow me" button.
 *
 * All parameters are optional — null means "use SDK default".
 *
 * @param icon Drawable shown when not following.
 * @param activeIcon Drawable shown when following is active.
 * @param backgroundColor Button background color.
 * @param accentColor Accent color (e.g. icon tint when active).
 * @param textColor Text color.
 * @param width Button width in dp.
 * @param height Button height in dp.
 * @param marginRight Right margin from the screen edge in dp.
 * @param marginBottom Bottom margin from the screen edge in dp.
 */
@Immutable
public data class FollowMeButtonAppearance(
    val icon: Drawable? = null,
    val activeIcon: Drawable? = null,
    val backgroundColor: Color? = null,
    val accentColor: Color? = null,
    val textColor: Color? = null,
    val width: Dp? = null,
    val height: Dp? = null,
    val marginRight: Dp = 4.dp,
    val marginBottom: Dp = 48.dp,
)

/**
 * Appearance configuration for the floor selector widget.
 *
 * All color parameters are optional — null means "use SDK default".
 *
 * @param accentColor Accent color for the selected floor indicator.
 * @param textColor Floor label text color.
 * @param backgroundColor Button background color.
 * @param marginLeft Left margin from the screen edge in dp.
 * @param marginTop Top margin from the screen edge in dp.
 */
@Immutable
public data class FloorSelectorAppearance(
    val accentColor: Color? = null,
    val textColor: Color? = null,
    val backgroundColor: Color? = null,
    val marginLeft: Dp = 16.dp,
    val marginTop: Dp = 145.dp,
)

/**
 * Aggregated widget configuration for [DefaultNavigineLocation].
 *
 * Groups visibility and appearance settings for all built-in UI controls.
 *
 * ## Basic usage — hide floor selector:
 * ```kotlin
 * DefaultNavigineLocation(
 *     widgetConfig = DefaultNavigineWidgetConfig(
 *         visibility = DefaultWidgetVisibility(showFloorSelector = false)
 *     )
 * )
 * ```
 *
 * ## Customize follow me button colors:
 * ```kotlin
 * DefaultNavigineLocation(
 *     widgetConfig = DefaultNavigineWidgetConfig(
 *         followMe = FollowMeButtonAppearance(
 *             accentColor = Color.Blue,
 *             backgroundColor = Color.White
 *         )
 *     )
 * )
 * ```
 *
 * @param visibility Controls which widgets are visible.
 * @param followMe Appearance of the "follow me" button.
 * @param floorSelector Appearance of the floor selector.
 */
@Immutable
public data class DefaultNavigineWidgetConfig(
    val visibility: DefaultWidgetVisibility = DefaultWidgetVisibility(),
    val followMe: FollowMeButtonAppearance = FollowMeButtonAppearance(),
    val floorSelector: FloorSelectorAppearance = FloorSelectorAppearance(),
) {
    public companion object {
        public val Default: DefaultNavigineWidgetConfig = DefaultNavigineWidgetConfig()
    }
}