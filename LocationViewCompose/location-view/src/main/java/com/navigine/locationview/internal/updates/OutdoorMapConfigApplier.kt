package com.navigine.locationview.internal.updates

import com.navigine.idl.java.LocationWindow
import com.navigine.locationview.settings.OutdoorMapConfig

internal fun applyOutdoorMapConfig(
    window: LocationWindow,
    config: OutdoorMapConfig,
    prev: OutdoorMapConfig?
) {
    if (prev == null || config.theme != prev.theme) {
        runCatching { window.mapTheme = config.theme }
    }
    if (prev == null || config.attributionAlignment != prev.attributionAlignment) {
        runCatching { window.attributionAlignment = config.attributionAlignment }
    }
}