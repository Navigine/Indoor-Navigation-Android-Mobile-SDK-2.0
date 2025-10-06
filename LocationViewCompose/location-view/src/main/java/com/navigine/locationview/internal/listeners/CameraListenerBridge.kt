package com.navigine.locationview.internal.listeners

import com.navigine.idl.java.Camera
import com.navigine.idl.java.CameraListener
import com.navigine.idl.java.CameraUpdateReason
import com.navigine.locationview.camera.NavCameraPositionState

/**
 * Bridges SDK camera callbacks to [NavCameraPositionState].
 *
 * We ignore [CameraUpdateReason] for now and rely on the 'moving' boolean to reflect whether
 * the camera is currently being manipulated (by user or program).
 */
internal class CameraListenerBridge(
    private val state : NavCameraPositionState
) : CameraListener() {

    override fun onCameraPositionChanged(
        reason: CameraUpdateReason?,
        isFinished: Boolean
    ) {
        val win = state.window ?: return
        runCatching { win.camera }
            .onSuccess { cam : Camera ->
                state.updateFromSdk(cam, !isFinished)
            }
    }
}