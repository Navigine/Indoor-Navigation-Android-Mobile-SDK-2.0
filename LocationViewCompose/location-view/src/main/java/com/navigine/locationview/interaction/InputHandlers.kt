package com.navigine.locationview.interaction

import android.graphics.PointF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import com.navigine.idl.java.Point
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.effects.LocalLocationWindow
import com.navigine.locationview.internal.listeners.InputListenerBridge

/**
 * A simple tap input handler for Navigine.
 *
 * Registers an SDK [com.navigine.idl.java.InputListener] on the active [LocationWindow] and
 * forwards raw tap events to the provided lambdas. For convenience, each callback receives the
 * screen [viewPoint] and the corresponding [meters] obtained via
 * [LocationWindow.screenPositionToMeters].
 *
 * Optionally, you can trigger SDK picking automatically on single tap by setting
 * [autoPickObjectOnTap] and/or [autoPickFeatureOnTap] to true. In that case we will invoke
 * [LocationWindow.pickMapObjectAt] and/or [LocationWindow.pickMapFeatureAt] with the tapped
 * [PointF]. Attach [PickHandlers] to receive the results.
 */
@Composable
@NavigineMapComposable
public fun InputHandlers(
    onTap: ((viewPoint: PointF, meters: Point?) -> Unit)? = null,
    onDoubleTap: ((viewPoint: PointF, meters: Point?) -> Unit)? = null,
    onLongTap: ((viewPoint: PointF, meters: Point?) -> Unit)? = null,
    autoPickObjectOnTap: Boolean = false,
    autoPickFeatureOnTap: Boolean = false,
){
    val window = LocalLocationWindow.current ?: return

    val onTapState = rememberUpdatedState(onTap)
    val onDoubleTapState = rememberUpdatedState(onDoubleTap)
    val onLongTapState = rememberUpdatedState(onLongTap)

    DisposableEffect(window,autoPickObjectOnTap, autoPickFeatureOnTap) {
        val listener = InputListenerBridge(
            window = window,
            onTap = { point, meters ->
                onTapState.value?.invoke(point, meters)
                if (autoPickObjectOnTap) runCatching{ window.pickMapObjectAt(point) }
                if (autoPickFeatureOnTap) runCatching{ window.pickMapFeatureAt(point) }
            },
            onDoubleTap = { p, m -> onDoubleTapState.value?.invoke(p, m) },
            onLongTap = { p, m -> onLongTapState.value?.invoke(p, m) }
        )
        runCatching { window.addInputListener(listener) }
        onDispose { window.removeInputListener(listener) }
    }
}