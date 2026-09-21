package com.navigine.locationview.tracking

import androidx.compose.runtime.Immutable
import com.navigine.idl.java.LiveObjectsLayerFilter
import com.navigine.idl.java.LiveObjectsLayerMode
import com.navigine.view.widgets.FloorSelectorViewConfig
import com.navigine.view.widgets.ZoomControlsConfig

@Immutable
public data class LiveObjectsLayerConfig(
    val visible: Boolean = true,
    val clusteringEnabled: Boolean = true,
    val tracksEnabled: Boolean = false,
    val mode: LiveObjectsLayerMode = LiveObjectsLayerMode.OBJECTS,
    val filter: LiveObjectsLayerFilter? = null,
) {
    public companion object {
        public val Default: LiveObjectsLayerConfig = LiveObjectsLayerConfig()
    }
}

/**
 * Creation-time only — [DefaultTrackingView] takes zoom/floor chrome config via
 * constructor, with no post-creation setter. Changing these after first composition
 * has no effect; only the values captured on the first frame are used.
 */
@Immutable
public data class DefaultTrackingWidgetConfig(
    val zoomConfig: ZoomControlsConfig? = null,
    val floorConfig: FloorSelectorViewConfig? = null,
) {
    public companion object {
        public val Default: DefaultTrackingWidgetConfig = DefaultTrackingWidgetConfig()
    }
}