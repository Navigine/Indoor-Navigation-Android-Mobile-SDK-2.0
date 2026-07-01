package com.navigine.locationview.internal.node

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