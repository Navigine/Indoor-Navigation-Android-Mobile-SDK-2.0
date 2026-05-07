package com.navigine.locationview.internal.updates

import com.navigine.idl.java.LocationWindow
import com.navigine.locationview.settings.LocationProperties

/**
 * Applies map properties to [LocationWindow].
 * Only non-null properties are applied. Diffed against [prev] to avoid redundant calls.
 */
internal fun applyProperties(
    window: LocationWindow,
    props: LocationProperties,
    prev: LocationProperties?
) {
    props.minZoomFactor?.let { value ->
        val changed = prev?.minZoomFactor != value
        if (prev == null || changed) runCatching { window.minZoomFactor = value }
    }
    props.maxZoomFactor?.let { value ->
        val changed = prev?.maxZoomFactor != value
        if (prev == null || changed) runCatching { window.maxZoomFactor = value }
    }
    props.pickRadius?.let { value ->
        val changed = prev?.pickRadius != value
        if (prev == null || changed) runCatching { window.pickRadius = value }
    }
    props.stickToBorder?.let { value ->
        val changed = prev?.stickToBorder != value
        if (prev == null || changed) runCatching { window.stickToBorder = value }
    }
    props.sublocationId?.let { value ->
        val changed = prev?.sublocationId != value
        if (prev == null || changed) runCatching { window.sublocationId = value }
    }
}