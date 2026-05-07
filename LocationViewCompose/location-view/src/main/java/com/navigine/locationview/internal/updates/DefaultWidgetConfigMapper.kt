package com.navigine.locationview.internal.updates

import androidx.compose.ui.graphics.toArgb
import com.navigine.locationview.settings.DefaultNavigineWidgetConfig
import com.navigine.view.DefaultNavigationView
import com.navigine.view.DefaultNavigationViewConfig
import com.navigine.view.widgets.FloorSelectorViewConfig
import com.navigine.view.widgets.FollowMeButtonConfig
import com.navigine.view.widgets.ZoomControlsConfig

/**
 * Applies [DefaultNavigineWidgetConfig] to [DefaultNavigationView].
 * Call this from a SideEffect whenever [config] or the view changes.
 * Only applies when [config] differs from [prev].
 */
internal fun applyWidgetConfig(
    view: DefaultNavigationView,
    config: DefaultNavigineWidgetConfig,
    prev: DefaultNavigineWidgetConfig?,
) {
    if (config == prev) return
    val sdkConfigs = config.toSdkConfigs()
    runCatching {
        view.setConfig(
            sdkConfigs.viewConfig,
            sdkConfigs.zoomControlsConfig,
            sdkConfigs.followMeButtonConfig,
            sdkConfigs.floorSelectorConfig,
        )
    }
}

/**
 * Converts [DefaultNavigineWidgetConfig] to the four SDK config objects required by
 * [com.navigine.view.DefaultNavigationView.setConfig].
 */
internal fun DefaultNavigineWidgetConfig.toSdkConfigs(): SdkWidgetConfigs {
    val viewConfig = buildViewConfig()
    val zoomConfig = ZoomControlsConfig.defaultConfig()
    val followMeConfig = buildFollowMeConfig()
    val floorConfig = buildFloorSelectorConfig()
    return SdkWidgetConfigs(viewConfig, zoomConfig, followMeConfig, floorConfig)
}

internal data class SdkWidgetConfigs(
    val viewConfig: DefaultNavigationViewConfig,
    val zoomControlsConfig: ZoomControlsConfig,
    val followMeButtonConfig: FollowMeButtonConfig,
    val floorSelectorConfig: FloorSelectorViewConfig,
)

private fun DefaultNavigineWidgetConfig.buildViewConfig(): DefaultNavigationViewConfig {
    var mask = 0
    if (visibility.showZoomControls) mask = mask or DefaultNavigationViewConfig.WIDGET_ZOOM_CONTROLS
    if (visibility.showFollowMe) mask = mask or DefaultNavigationViewConfig.WIDGET_FOLLOW_ME
    if (visibility.showFloorSelector) mask = mask or DefaultNavigationViewConfig.WIDGET_FLOOR_SELECTOR
    return DefaultNavigationViewConfig.builder()
        .visibleWidgets(mask)
        .build()
}

private fun DefaultNavigineWidgetConfig.buildFollowMeConfig(): FollowMeButtonConfig {
    val b = FollowMeButtonConfig.builder()
    followMe.icon?.let { b.followMeIcon(it) }
    followMe.activeIcon?.let { b.followMeIconActive(it) }
    followMe.backgroundColor?.let { b.buttonBackgroundColor(it.toArgb()) }
    followMe.accentColor?.let { b.accentColor(it.toArgb()) }
    followMe.textColor?.let { b.textColor(it.toArgb()) }
    followMe.width?.let { b.buttonWidth(it.value) }
    followMe.height?.let { b.buttonHeight(it.value) }
    b.marginRight(followMe.marginRight.value.toInt())
    b.marginBottom(followMe.marginBottom.value.toInt())
    return b.build()
}

private fun DefaultNavigineWidgetConfig.buildFloorSelectorConfig(): FloorSelectorViewConfig {
    val b = FloorSelectorViewConfig.builder()
    floorSelector.accentColor?.let { b.accentColor(it.toArgb()) }
    floorSelector.textColor?.let { b.textColor(it.toArgb()) }
    floorSelector.backgroundColor?.let { b.buttonBackgroundColor(it.toArgb()) }
    b.marginLeft(floorSelector.marginLeft.value.toInt())
    b.marginTop(floorSelector.marginTop.value.toInt())
    return b.build()
}