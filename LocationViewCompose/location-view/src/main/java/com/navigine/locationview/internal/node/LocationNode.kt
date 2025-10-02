package com.navigine.locationview.internal.node

internal interface LocationNode {
    fun onAttached() {}
    fun onRemoved() {}
    fun onCleared() {}
}