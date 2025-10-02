package com.navigine.locationview.internal.node

import com.navigine.idl.java.LocationWindow

internal class LocationRootNode (
    val window : LocationWindow
) : LocationNode {

    val children = mutableListOf<LocationNode>()

    override fun onCleared() {
        children.forEach { it.onCleared() }
        children.clear()
    }
}