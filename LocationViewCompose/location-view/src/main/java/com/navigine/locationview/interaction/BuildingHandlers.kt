package com.navigine.locationview.interaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import com.navigine.idl.java.Sublocation
import com.navigine.locationview.ExperimentalNavigineApi
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.effects.LocalLocationWindow
import com.navigine.locationview.internal.listeners.BuildingListenerBridge

/**
 * Building and sublocation event handlers for Navigine campus mode.
 *
 * Registers an SDK [com.navigine.idl.java.BuildingListener] on the active [LocationWindow]
 * and forwards building focus/leave events and sublocation changes to the provided lambdas.
 *
 * Primarily useful in campus mode where multiple buildings with their own sublocations
 * are present on the map simultaneously.
 *
 * ## Usage
 * ```kotlin
 * NavigineLocation {
 *     BuildingHandlers(
 *         onBuildingFocused = { sublocations, activeId, switchSublocation ->
 *             // Update your UI with the list of floors
 *             // Call switchSublocation(id) to change the active floor for this building
 *         },
 *         onBuildingLeft = {
 *             // Hide floor selector UI
 *         },
 *         onActiveSublocationChanged = { sublocationId ->
 *             // React to floor change
 *         }
 *     )
 * }
 * ```
 *
 * Note: [Sublocation] is a raw SDK type exposed here for its rich API
 * (coordinate conversion, venues, zones, etc.).
 *
 * Note: This composable is intended for use inside [NavigineLocation].
 * [DefaultNavigineLocation] already manages building events internally
 * for its floor selector — prefer [NavigineLocation] if you need manual
 * building event handling.
 *
 * @param onBuildingFocused Called when a building comes into focus. Provides:
 * - [sublocations] — list of all sublocations (floors) in this building
 * - [activeSublocationId] — currently active sublocation id
 * - [switchSublocation] — call with a sublocation id to switch the active floor
 * @param onBuildingLeft Called when the camera leaves a building's focus area.
 * @param onActiveSublocationChanged Called when the active sublocation changes,
 * either programmatically or by user interaction.
 */
@OptIn(ExperimentalNavigineApi::class)
@Composable
@NavigineMapComposable
public fun BuildingHandlers(
    onBuildingFocused: ((
        sublocations: List<Sublocation>,
        activeSublocationId: Int,
        switchSublocation: (sublocationId: Int) -> Unit,
    ) -> Unit)? = null,
    onBuildingLeft: (() -> Unit)? = null,
    onActiveSublocationChanged: ((sublocationId: Int) -> Unit)? = null,
) {
    val window = LocalLocationWindow.current ?: return

    val onBuildingFocusedState = rememberUpdatedState(onBuildingFocused)
    val onBuildingLeftState = rememberUpdatedState(onBuildingLeft)
    val onActiveSublocationChangedState = rememberUpdatedState(onActiveSublocationChanged)

    DisposableEffect(window) {
        val listener = BuildingListenerBridge(
            onBuildingFocused = { sublocations, activeId, switch ->
                onBuildingFocusedState.value?.invoke(sublocations, activeId, switch)
            },
            onBuildingLeft = {
                onBuildingLeftState.value?.invoke()
            },
            onActiveSubLocationChanged = { id ->
                onActiveSublocationChangedState.value?.invoke(id)
            },
        )
        runCatching { window.addBuildingListener(listener) }
        onDispose { runCatching { window.removeBuildingListener(listener) } }
    }
}