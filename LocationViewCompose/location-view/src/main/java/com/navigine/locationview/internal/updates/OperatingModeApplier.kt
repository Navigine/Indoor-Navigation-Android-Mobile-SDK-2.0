package com.navigine.locationview.internal.updates

import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.OperatingMode

/**
 * Applies operating mode to [LocationWindow]. Diffed against [prev] to avoid
 * redundant renderer pipeline rebuilds.
 */
internal fun applyOperatingMode(
    window: LocationWindow,
    mode: OperatingMode,
    prev: OperatingMode?
) {
    if (prev == null || mode != prev) {
        runCatching { window.operatingMode = mode }
    }
}