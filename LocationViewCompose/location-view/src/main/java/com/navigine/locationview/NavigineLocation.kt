package com.navigine.locationview

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.navigine.idl.java.LocationWindow
import com.navigine.locationview.camera.NavCameraPositionState
import com.navigine.locationview.camera.rememberNavCameraPositionState
import com.navigine.locationview.effects.LocalLocationWindow
import com.navigine.locationview.internal.LocationViewHolder
import com.navigine.locationview.internal.listeners.CameraListenerBridge
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationRootNode
import com.navigine.locationview.internal.updates.applyProperties
import com.navigine.locationview.internal.updates.applyUiSettings
import com.navigine.locationview.settings.DefaultLocationProperties
import com.navigine.locationview.settings.DefaultLocationUiSettings
import com.navigine.locationview.settings.LocationProperties
import com.navigine.locationview.settings.LocationUiSettings


/**
 * High-level Navigine container for Jetpack Compose.
 *
 * Responsibilities:
 * - Embeds the SDK's [com.navigine.view.LocationView] into Compose using [AndroidView].
 * - Mirrors the lifecycle: calls onStart/onStop/onLowMemory on the underlying view.
 *
 * @param cameraPositionState camera state holder (two-way synced with the window).
 * @param properties map properties; nullable fields mean "do not change".
 * @param uiSettings UI gesture toggles.
 * @param onWindowReady single-shot callback when [LocationWindow] is created.
 * @param content Navigine map objects are declared here.
 *
 * Notes:
 * - Do not call LocationView's onStart/onStop manually outside of this composable; it is handled here.
 * - [onWindowReady] is invoked once per underlying Android view instance.
 */
@Composable
public fun NavigineLocation(
    modifier: Modifier = Modifier,
    cameraPositionState: NavCameraPositionState = rememberNavCameraPositionState(),
    properties: LocationProperties = DefaultLocationProperties,
    uiSettings: LocationUiSettings = DefaultLocationUiSettings,
    onWindowReady: (LocationWindow) -> Unit = {},
    content : @Composable @NavigineMapComposable () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewHolder = remember { LocationViewHolder() }
    val windowState: MutableState<LocationWindow?> = remember { mutableStateOf(null) }
    val onWindowReadyState = rememberUpdatedState(onWindowReady)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            viewHolder.createView(ctx).also { lv ->
                val win = lv.locationWindow
                windowState.value = win
                onWindowReadyState.value.invoke(win)
            }
        },
         update = {
         /* `update` will be added later for properties/uiSettings */
         }
    )

    DisposableEffect(lifecycleOwner, viewHolder) {
        val observer = LifecycleEventObserver{ _, event ->
            when(event){
                Lifecycle.Event.ON_START -> viewHolder.onStart()
                Lifecycle.Event.ON_STOP -> viewHolder.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
            viewHolder.onStart()

        onDispose {
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                viewHolder.onStop()
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewHolder.clear()
            windowState.value = null
        }
    }

    val appContext = context.applicationContext
    DisposableEffect(appContext, viewHolder) {
        val callbacks = object : ComponentCallbacks2 {
            override fun onLowMemory() {
                viewHolder.onLowMemory()
            }
            override fun onTrimMemory(level: Int) {
                if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
                    viewHolder.onLowMemory()
                }
            }
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
        }
        appContext.registerComponentCallbacks(callbacks)
        onDispose { appContext.unregisterComponentCallbacks(callbacks) }
    }

    val window = windowState.value
    CompositionLocalProvider(LocalLocationWindow provides window) {

        val parentComposition = rememberCompositionContext()
        val currentContent = rememberUpdatedState(content)

        DisposableEffect(window, cameraPositionState) {
            if (window == null) return@DisposableEffect onDispose {}

            cameraPositionState.window = window

            // Register SDK camera listener -> state.
            val cameraBridge = CameraListenerBridge(cameraPositionState)
            runCatching { window.addCameraListener(cameraBridge) }
                .onFailure { Log.e("NavigineLocation", "Failed to add camera listener", it) }

            onDispose {
                runCatching { window.removeCameraListener(cameraBridge) }
                cameraPositionState.window = null
            }
        }

        var prevProps by remember(window) { mutableStateOf<LocationProperties?>(null) }
        var prevUi by remember(window) { mutableStateOf<LocationUiSettings?>(null) }

        SideEffect {
            window?.let { win ->
                applyProperties(win, properties, prevProps)
                applyUiSettings(win, uiSettings, prevUi)
                prevProps = properties
                prevUi = uiSettings
            }
        }

        DisposableEffect(window, parentComposition) {
            if (window == null) return@DisposableEffect onDispose {}

            val root = LocationRootNode(window)
            val applier = LocationApplier(root)
            val composition = Composition(applier, parentComposition)

            composition.setContent {
                currentContent.value()
            }

            onDispose {
                composition.dispose()
            }
        }
    }
}