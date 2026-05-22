package com.navigine.locationview.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.navigine.idl.java.AnimationType
import com.navigine.idl.java.Camera
import com.navigine.idl.java.CameraCallback
import com.navigine.idl.java.LocationWindow

/**
 * Holds the current Navigine camera and exposes imperative controls
 * that can be triggered from Compose.
 *
 * Semantics:
 * - [position] is the single source of truth inside Compose.
 * - [move] applies an immediate change using LocationWindow.setCamera.
 * - [flyTo] and [animateTo] perform animated changes via SDK.
 *
 * Implementation details:
 * - We optimistically update [position] on every call.
 * - We bind to a [LocationWindow] when available and push any pending [position].
 * - A monotonically increasing [opSeq] is used to discard late callbacks.
 */

@Stable
public class NavCameraPositionState(initial: Camera? = null) {

    /** The last known camera. May be null until the map window is available. */
    public var position: Camera? by mutableStateOf(initial)
        internal set

    /** True while an animation or SDK-driven camera movement is in progress. */
    public var isMoving: Boolean by mutableStateOf(false)
        internal set

    /** Convenience accessor for the current zoom factor (if known). */
    public val zoomFactor: Float?
        get() = window?.zoomFactor

    // Bound LocationWindow; set/unset by the binder in NavigineLocation.
    internal var window: LocationWindow? = null
        set(value) {
            field = value
            // Push pending camera to the SDK as soon as we have a window.
            val cam = position
            if (value != null && cam != null) {
                runCatching { value.camera = cam } // immediate apply
            }
        }

    // "Newest wins" token to ignore stale callbacks
    private var opSeq: Long = 0

    /** Instantly moves the camera using LocationWindow.setCamera. */
    public fun move(camera: Camera) {
        position = camera
        opSeq++
        isMoving = false
        window?.let { win ->
            runCatching { win.camera = camera }
        }
    }

    /** Instantly set zoom only (keeps point/rotation). */
    public fun moveZoomTo(zoom: Float) {
        opSeq++
        isMoving = false

        val w = window
        if (w != null) {
            runCatching { w.zoomFactor = zoom }
            runCatching { position = w.camera }.onFailure {
                position = position?.let { Camera(it.point, zoom, it.rotation, it.tilt) }
            }
        } else {
            position = position?.let { Camera(it.point, zoom, it.rotation, it.tilt) }
        }
    }

    /**
     * Animates the camera using LocationWindow.flyTo (no animation type).
     *
     * @param durationMs duration ms.
     * @param callback Optional SDK callback that will be invoked after our state update.
     */
    public fun flyTo(
        camera: Camera,
        durationMs: Int = 350,
        callback: CameraCallback? = null
    ) {
        position = camera
        val w = window ?: return
        isMoving = true
        val myOp = ++opSeq
        val wrapped = wrapCallback(myOp, callback)
        runCatching {
            w.flyTo(camera, durationMs, wrapped)
        }.onFailure {
            if (opSeq == myOp) isMoving = false
        }
    }

    /**
     * Animates the camera using [LocationWindow.moveTo] (with animation type).
     *
     * @param durationMs Duration in milliseconds.
     * @param type type of the animation.
     * @param callback Optional SDK callback; will be invoked after our state update.
     */
    public fun animateTo(
        camera: Camera,
        durationMs: Int = 350,
        type: AnimationType = AnimationType.LINEAR,
        callback: CameraCallback? = null
    ) {
        position = camera
        val w = window ?: return
        isMoving = true
        val myOp = ++opSeq
        val wrapped = wrapCallback(myOp, callback)
        runCatching {
            w.moveTo(camera, durationMs, type, wrapped)
        }.onFailure {
            if (opSeq == myOp) isMoving = false
        }
    }

    /** Internal: called from the camera listener to mirror SDK state. */
    internal fun updateFromSdk(camera: Camera, moving: Boolean) {
        position = camera
        isMoving = moving
    }

    /** Finish motion for [token] and forward to [user] if present. */
    private fun wrapCallback(token : Long, user : CameraCallback?) : CameraCallback {
        return object : CameraCallback(){
            override fun onMoveFinished(isFinished: Boolean) {
                if(opSeq == token) isMoving = false
                user?.onMoveFinished(isFinished)
            }
        }
    }
}

/** Convenient factory for [NavCameraPositionState]. */
@Composable
public fun rememberNavCameraPositionState(
    initial: Camera? = null
) : NavCameraPositionState = remember { NavCameraPositionState(initial) }