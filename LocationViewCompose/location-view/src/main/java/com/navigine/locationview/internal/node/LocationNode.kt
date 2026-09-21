package com.navigine.locationview.internal.node

import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.MapObject

internal interface LocationNode {
    fun onAttached() {}
    fun onRemoved() {}
    fun onCleared() {}
}

/**
 *  Checks if map object, or it's child is existing in native code
 */
internal inline fun <T : MapObject> T.ifValid(block: T.() -> Unit) {
    if (isValid) block()
}

/**
 * Runs [block] only if this [LocationWindow] is still valid. Any other method on an
 * invalid LocationWindow throws RuntimeException per the SDK contract - this guard
 * prevents that in call sites where the window's native lifecycle can outrun Compose's
 * own state updates (e.g. window destroyed on the native side between composition and
 * effect execution, or during teardown races).
 *
 * Note: this narrows the window but doesn't eliminate it entirely — isValid can still
 * flip false between the check and the native call on another thread. Kept alongside
 * runCatching at call sites for genuinely unexpected native races, not as a replacement.
 */
internal inline fun LocationWindow.ifValid(block: LocationWindow.() -> Unit) {
    if (isValid) block()
}