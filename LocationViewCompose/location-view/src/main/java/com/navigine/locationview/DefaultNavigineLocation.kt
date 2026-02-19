package com.navigine.locationview

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import android.view.View
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
import com.navigine.locationview.internal.DefaultLocationViewHolder
import com.navigine.locationview.internal.listeners.CameraListenerBridge
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationRootNode
import com.navigine.locationview.internal.updates.applyProperties
import com.navigine.locationview.internal.updates.applyUiSettings
import com.navigine.locationview.settings.DefaultLocationProperties
import com.navigine.locationview.settings.DefaultLocationUiSettings
import com.navigine.locationview.settings.LocationProperties
import com.navigine.locationview.settings.LocationUiSettings
import com.navigine.locationview.utils.findGlChild

/**
 * Ready-to-use Navigine map with built-in UI controls.
 *
 * This is a convenience wrapper around [NavigineLocation] that uses
 * [DefaultNavigationView] from the SDK, which automatically includes:
 *
 * - **Zoom buttons** (top-right corner)
 * - **Floor selector** (top-left corner)
 * - **Follow me button** (bottom-right corner)
 * - **User location layer** (blue dot with accuracy circle)
 *
 * All UI elements and their listeners are managed automatically by the SDK.
 *
 * ## When to use
 *
 * Use [DefaultNavigineLocation] when:
 * - You want a quick setup with standard UI
 * - You don't need custom UI styling
 * - Standard zoom/floor/follow controls are sufficient
 *
 * Use [NavigineLocation] when:
 * - You need full control over UI
 * - You want custom styled controls
 * - You need specific widget positioning
 *
 * ## Basic Usage
 * ```kotlin
 * DefaultNavigineLocation(
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * ## With Map Objects
 * ```kotlin
 * DefaultNavigineLocation {
 *     Icon(
 *         position = LocationPoint(100.0, 200.0),
 *         bitmap = myBitmap
 *     )
 *     Circle(
 *         position = center,
 *         radius = 50f,
 *         color = Color.Blue
 *     )
 * }
 * ```
 *
 * ## Camera Control
 * ```kotlin
 * val cameraState = rememberNavCameraPositionState()
 *
 * DefaultNavigineLocation(
 *     cameraPositionState = cameraState
 * )
 *
 * LaunchedEffect(Unit) {
 *     cameraState.flyTo(
 *         Camera(point = Point(100.0, 200.0), zoom = 18f, rotation = 0f),
 *         durationMs = 1000
 *     )
 * }
 * ```
 *
 * @param modifier Modifier for the map container
 * @param cameraPositionState Camera state holder (two-way synced with SDK)
 * @param properties Map configuration (zoom limits, pick radius, etc.)
 * @param uiSettings Gesture controls (rotate, tilt, scroll, zoom)
 * @param isVisible Controls map visibility without destroying it
 * @param onWindowReady Callback when LocationWindow is created (called once)
 * @param content Map objects (Icon, Circle, Polyline, etc.)
 *
 * @see NavigineLocation for more control over UI
 * @since 2.24.4
 */
@Composable
public fun DefaultNavigineLocation(
    modifier: Modifier = Modifier,
    cameraPositionState: NavCameraPositionState = rememberNavCameraPositionState(),
    properties: LocationProperties = DefaultLocationProperties,
    uiSettings: LocationUiSettings = DefaultLocationUiSettings,
    isVisible: Boolean = true,
    onWindowReady: (LocationWindow) -> Unit = {},
    content: @Composable @NavigineMapComposable () -> Unit = {}
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewHolder = remember(context) { DefaultLocationViewHolder() }
    val windowState: MutableState<LocationWindow?> = remember { mutableStateOf(null) }
    val onWindowReadyState = rememberUpdatedState(onWindowReady)

    var glChild by remember { mutableStateOf<View?>(null) }

    AndroidView(
        modifier = modifier,
        factory = remember(context) { { ctx ->
            viewHolder.createView(ctx).also { lv ->
                glChild = findGlChild(lv)
                val win = lv.locationWindow
                windowState.value = win
                onWindowReadyState.value.invoke(win)
            }
        } },
        update = { lv ->
            if (glChild == null) glChild = findGlChild(lv)
            glChild?.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    )

    DisposableEffect(lifecycleOwner, viewHolder) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewHolder.onStart()
                Lifecycle.Event.ON_STOP -> viewHolder.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
            viewHolder.onStart()

        onDispose {
            val win = windowState.value
            windowState.value = null

            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                viewHolder.onStop()
            }

            win?.let { window ->
                runCatching {
                    window.removeAllMapObjects()
                }
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewHolder.clear()
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

            // Register sdk camera listener -> state.
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

        // compose tree for map objects
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
