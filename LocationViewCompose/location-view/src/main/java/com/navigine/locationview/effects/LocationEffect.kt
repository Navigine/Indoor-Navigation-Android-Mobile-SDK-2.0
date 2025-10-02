package com.navigine.locationview.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.navigine.idl.java.LocationWindow
import kotlinx.coroutines.CoroutineScope

public val LocalLocationWindow: ProvidableCompositionLocal<LocationWindow?> = staticCompositionLocalOf { null }

/**
 * Launches a composable side-effect using [LaunchedEffect] to execute the provided [block],
 * passing the managed [LocationWindow] as a parameter to the [CoroutineScope]. The effect
 * relaunches whenever the provided [keys] change.
 *
 * **Caution:** Use this effect judiciously, as the [LocationWindow]'s properties are managed
 * internally by the [com.navigine.locationview.NavigineLocation] composable.
 */
@Composable
public fun LocationEffect(
    vararg keys: Any?,
    block: suspend CoroutineScope.(LocationWindow) -> Unit
) {
    val window = LocalLocationWindow.current
    if (window != null) {
        LaunchedEffect(keys = keys) {
            block(window)
        }
    }
}