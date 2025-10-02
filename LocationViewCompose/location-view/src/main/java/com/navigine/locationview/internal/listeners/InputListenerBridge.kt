package com.navigine.locationview.internal.listeners

import android.graphics.PointF
import com.navigine.idl.java.InputListener
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Point

/**
 * Bridges Navigine [InputListener] to Kotlin lambdas. Also computes meters from screen coordinates
 * using [LocationWindow.screenPositionToMeters].
 */
internal class InputListenerBridge(
    private val window: LocationWindow,
    private val onTap: (viewPoint: PointF, meters: Point?) -> Unit,
    private val onDoubleTap: (viewPoint: PointF, meters: Point?) -> Unit,
    private val onLongTap: (viewPoint: PointF, meters: Point?) -> Unit,
) : InputListener() {

    override fun onViewTap(point: PointF) {
        val meters = runCatching { window.screenPositionToMeters(point) }.getOrNull()
        onTap(point, meters)
    }

    override fun onViewDoubleTap(point: PointF) {
        val meters = runCatching { window.screenPositionToMeters(point) }.getOrNull()
        onDoubleTap(point, meters)
    }

    override fun onViewLongTap(point: PointF) {
        val meters = runCatching { window.screenPositionToMeters(point) }.getOrNull()
        onLongTap(point, meters)
    }
}