package com.navigine.locationview.internal.listeners

import android.graphics.PointF
import com.navigine.idl.java.GlobalPoint
import com.navigine.idl.java.InputListener
import com.navigine.idl.java.LocationWindow

/**
 * Bridges Navigine [InputListener] to Kotlin lambdas. Also computes meters from screen coordinates
 * using [LocationWindow.screenPositionToGlobal].
 */
internal class InputListenerBridge(
    private val window: LocationWindow,
    private val onTap: (viewPoint: PointF, global: GlobalPoint?) -> Unit,
    private val onDoubleTap: (viewPoint: PointF, global: GlobalPoint?) -> Unit,
    private val onLongTap: (viewPoint: PointF, global: GlobalPoint?) -> Unit,
) : InputListener() {

    override fun onViewTap(point: PointF) {
        val global = runCatching { window.screenPositionToGlobal(point) }.getOrNull()
        onTap(point, global)
    }

    override fun onViewDoubleTap(point: PointF) {
        val global = runCatching { window.screenPositionToGlobal(point) }.getOrNull()
        onDoubleTap(point, global)
    }

    override fun onViewLongTap(point: PointF) {
        val global = runCatching { window.screenPositionToGlobal(point) }.getOrNull()
        onLongTap(point, global)
    }
}