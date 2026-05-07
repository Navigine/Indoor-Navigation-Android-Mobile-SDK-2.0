package com.navigine.locationview.internal.node

import androidx.compose.runtime.AbstractApplier

/**
 * Compose Applier that manages a tree of [LocationNode] under a [LocationRootNode].
 *
 * The applier is responsible for creating/removing/moving nodes to mirror the
 * Compose UI tree, and letting nodes attach/detach so they can allocate/free
 * native SDK objects.
 */
internal class LocationApplier(
    root : LocationRootNode
) : AbstractApplier<LocationNode>(root){

    val window get() = (root as LocationRootNode).window
    private val children get() = (root as LocationRootNode).children

    override fun insertTopDown(
        index: Int,
        instance: LocationNode
    ) {
        children.add(index, instance)
    }

    override fun insertBottomUp(
        index: Int,
        instance: LocationNode
    ) {
        // When the node is fully inserted, notify it to allocate SDK resources
        instance.onAttached()
    }

    override fun remove(index: Int, count: Int) {
        repeat(count) {
            val node = children.removeAt(index)
            node.onRemoved()
        }
    }

    override fun move(from: Int, to: Int, count: Int) {
        if (from == to || count == 0) return
        val moved = ArrayList<LocationNode>(count)
        repeat(count) { moved += children.removeAt(from) }
        children.addAll(to, moved)
    }

    override fun onClear() {
        (root as LocationRootNode).onCleared()
    }

}