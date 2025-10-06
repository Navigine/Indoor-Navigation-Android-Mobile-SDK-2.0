package com.navigine.locationview.interaction

import android.graphics.PointF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import com.navigine.idl.java.MapObjectPickResult
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.effects.LocalLocationWindow
import com.navigine.locationview.internal.listeners.PickListenerBridge

/**
 * Pick result handlers for Navigine.
 *
 * Registers an SDK [com.navigine.idl.java.PickListener] on the active window and forwards:
 * - map object picks via [onObjectPicked]
 * - map feature (attributes) picks via [onFeaturePicked]
 *
 * To trigger picking, call [LocationWindow.pickMapObjectAt] / [pickMapFeatureAt] yourself,
 * or enable auto-pick on tap in [InputHandlers].
 */
@Composable
@NavigineMapComposable
public fun PickHandlers(
    onObjectPicked: ((result: MapObjectPickResult, viewPoint: PointF) -> Unit)? = null,
    onFeaturePicked: ((attributes: Map<String?, String?>, viewPoint: PointF) -> Unit)? = null,
) {
    val window = LocalLocationWindow.current
        ?: return

    val objCb = rememberUpdatedState(onObjectPicked)
    val featCb = rememberUpdatedState(onFeaturePicked)

    DisposableEffect(window) {
        val listener = PickListenerBridge(
            onObject = { res, p -> objCb.value?.invoke(res, p) },
            onFeature = { attrs, p -> featCb.value?.invoke(attrs, p) },
        )
        runCatching { window.addPickListener(listener) }
        onDispose { runCatching { window.removePickListener(listener) } }
    }
}