package com.navigine.locationview.settings

import androidx.compose.runtime.Immutable
import com.navigine.idl.java.AttributionAlignment
import com.navigine.idl.java.AttributionHorizontalAlignment
import com.navigine.idl.java.AttributionVerticalAlignment
import com.navigine.idl.java.MapTheme
import com.navigine.idl.java.OperatingMode

/**
 * Outdoor basemap appearance. Has no effect while [OperatingMode.INDOOR_ONLY] is active -
 * both the theme and the attribution overlay only apply to the outdoor vector basemap.
 */
@Immutable
public data class OutdoorMapConfig(
    public val theme: MapTheme = MapTheme.LIGHT,
    public val attributionAlignment: AttributionAlignment = AttributionAlignment(
        AttributionHorizontalAlignment.RIGHT,
        AttributionVerticalAlignment.BOTTOM,
    ),
) {
    public companion object {
        public val Default: OutdoorMapConfig = OutdoorMapConfig()
    }
}
