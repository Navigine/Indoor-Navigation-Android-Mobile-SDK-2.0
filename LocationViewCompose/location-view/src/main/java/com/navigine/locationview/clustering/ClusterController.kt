package com.navigine.locationview.clustering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.navigine.idl.java.ClusterMapObject
import com.navigine.idl.java.ClusterMapObjectController
import com.navigine.idl.java.ClusterMapObjectControllerListener
import com.navigine.idl.java.ClusterMapObjectListener
import com.navigine.locationview.ExperimentalNavigineApi
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.effects.LocalLocationWindow
import com.navigine.locationview.objects.config.ClusterConfig
import com.navigine.locationview.objects.icon.IconState

/**
 * Groups [com.navigine.locationview.objects.icon.Icon] map objects into clusters based on
 * zoom level and proximity.
 *
 * Pass the [IconState] handles of the icons you want clustered — icons not passed here
 * render individually regardless of proximity. An icon only participates once its
 * [IconState.isAttached] becomes true; the controller automatically registers/unregisters
 * icons as the list changes or as icons attach/detach.
 *
 * Cluster picks are delivered through the existing [com.navigine.locationview.interaction.PickHandlers]
 * — [onClusterCreated] and [onClusterChanged] are only for customizing cluster appearance
 * (e.g. setting a badge bitmap with the current member count).
 *
 * ## Basic Usage
 * ```kotlin
 * NavigineLocation {
 *     val states = pins.map { pin ->
 *         val state = rememberIconState()
 *         Icon(position = pin.position, image = pin.image, state = state)
 *         state
 *     }
 *
 *     ClusterController(icons = states)
 * }
 * ```
 *
 * ## Custom cluster appearance
 * ```kotlin
 * ClusterController(
 *     icons = states,
 *     config = ClusterConfig(radius = 60f, clusterSize = Size(48f, 48f)),
 *     onClusterCreated = { cluster -> cluster.setBitmap(badge(cluster.count)) },
 *     onClusterChanged = { cluster -> cluster.setBitmap(badge(cluster.count)) }
 * )
 * ```
 *
 * @param icons Icon handles eligible for clustering.
 * @param config Cluster grouping radius, marker size, and interactivity.
 * @param onClusterCreated Invoked when a new cluster marker appears (≥2 icons grouped).
 * Use this to set an initial bitmap.
 * @param onClusterChanged Invoked when a visible cluster's member count changes.
 * Use this to refresh the bitmap.
 * @param onClusterDestroyed Invoked when a cluster is removed (fewer than two icons remain).
 *
 * @since 2.26.0
 */
@OptIn(ExperimentalNavigineApi::class)
@Composable
@NavigineMapComposable
public fun ClusterController(
    icons: List<IconState>,
    config: ClusterConfig = ClusterConfig.Default,
    onClusterCreated: ((cluster: ClusterMapObject) -> Unit)? = null,
    onClusterChanged: ((cluster: ClusterMapObject) -> Unit)? = null,
    onClusterDestroyed: ((clusterId: Int) -> Unit)? = null,
) {
    val window = LocalLocationWindow.current ?: return

    val onClusterCreatedState = rememberUpdatedState(onClusterCreated)
    val onClusterChangedState = rememberUpdatedState(onClusterChanged)
    val onClusterDestroyedState = rememberUpdatedState(onClusterDestroyed)

    val controller = remember(window) {
        window.addClusterMapObjectController()
    }

    DisposableEffect(window) {
        onDispose {
            runCatching { controller.clear() }
            runCatching { window.removeClusterMapObjectController(controller) }
        }
    }

    val prevConfig = remember(controller) { mutableStateOf<ClusterConfig?>(null) }
    SideEffect {
        if (config != prevConfig.value) {
            runCatching { controller.isEnabled = config.enabled }
            runCatching { controller.radius = config.radius }
            runCatching {
                controller.setClusterSize(
                    config.clusterSize.width,
                    config.clusterSize.height
                )
            }
            runCatching { controller.setInteractive(config.interactive) }
            prevConfig.value = config
        }
    }

    val clusterListeners =
        remember(controller) { mutableMapOf<Int, Pair<ClusterMapObject, ClusterMapObjectListener>>() }

    DisposableEffect(controller) {
        val listener = object : ClusterMapObjectControllerListener() {
            override fun onClusterCreated(
                ctrl: ClusterMapObjectController,
                cluster: ClusterMapObject
            ) {
                val clusterListener = object : ClusterMapObjectListener() {
                    override fun onClusterChanged(cluster: ClusterMapObject) {
                        onClusterChangedState.value?.invoke(cluster)
                    }
                }
                runCatching { cluster.addListener(clusterListener) }
                    .onSuccess {
                        clusterListeners[cluster.id] = cluster to clusterListener
                    }
                onClusterCreatedState.value?.invoke(cluster)
            }

            override fun onClusterDestroyed(ctrl: ClusterMapObjectController, clusterId: Int) {
                clusterListeners.remove(clusterId)?.let { (cluster, clusterListener) ->
                    runCatching { cluster.removeListener(clusterListener) }
                }
                onClusterDestroyedState.value?.invoke(clusterId)
            }
        }

        runCatching { controller.addListener(listener) }
        onDispose {
            runCatching { controller.removeListener(listener) }

            clusterListeners.values.forEach { (cluster, clusterListener) ->
                runCatching { cluster.removeListener(clusterListener) }
            }
            clusterListeners.clear()
        }
    }

    // Only attached icons participate; isAttached is observed via mutableStateOf
    // on IconState, so this recomposes as icons attach/detach.
    val attachedIcons = icons.filter { it.isAttached }
    val prevAttached = remember(controller) { mutableStateOf<Set<IconState>>(emptySet()) }

    SideEffect {
        val prev = prevAttached.value
        val curr = attachedIcons.toSet()

        val removed = prev - curr
        val added = curr - prev

        removed.forEach { state ->
            val icon = state.mapObject ?: return@forEach
            runCatching { controller.removeIconMapObject(icon) }
        }

        added.forEach { state ->
            val icon = state.mapObject ?: return@forEach
            runCatching { controller.addIconMapObject(icon) }
        }

        prevAttached.value = curr
    }
}