package com.navigine.locationview

import android.content.ComponentCallbacks2
import android.content.res.Configuration
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
import com.navigine.locationview.utils.findGlChild


/**
 * Core Navigine map container for Jetpack Compose.
 *
 * Embeds the SDK's [com.navigine.view.LocationView] into Compose and provides full control
 * over the map UI. Unlike [DefaultNavigineLocation], this composable ships with no built-in
 * widgets — zoom controls, floor selector, and follow-me button are your responsibility.
 *
 * Lifecycle (onStart/onStop/onLowMemory) is handled automatically and mirrors
 * the host [androidx.lifecycle.LifecycleOwner].
 *
 * ## When to use
 *
 * Use [NavigineLocation] when:
 * - You need full control over UI layout and styling
 * - You want custom zoom controls, floor selector, or follow-me button
 * - You need to handle building/sublocation events manually via [com.navigine.locationview.interaction.BuildingHandlers]
 *
 * Use [DefaultNavigineLocation] when:
 * - You want a quick setup with standard built-in UI controls
 * - Custom styling of widgets is sufficient via [com.navigine.locationview.settings.DefaultNavigineWidgetConfig]
 *
 * ## Basic usage
 * ```kotlin
 * NavigineLocation(modifier = Modifier.fillMaxSize())
 * ```
 *
 * ## With map objects
 * ```kotlin
 * NavigineLocation(modifier = Modifier.fillMaxSize()) {
 *     Circle(
 *         position = LocationPoint(100.0, 200.0),
 *         radius = 50f,
 *         color = Color.Blue
 *     )
 *     InputHandlers(
 *         onTap = { viewPoint, meters -> /* handle tap */ }
 *     )
 * }
 * ```
 *
 * ## Camera control
 * ```kotlin
 * val cameraState = rememberNavCameraPositionState()
 *
 * NavigineLocation(
 *     modifier = Modifier.fillMaxSize(),
 *     cameraPositionState = cameraState
 * )
 *
 * LaunchedEffect(Unit) {
 *     cameraState.flyTo(
 *         Camera(point = Point(100.0, 200.0), zoom = 18f, rotation = 0f, tilt = 0f),
 *         durationMs = 1000
 *     )
 * }
 * ```
 *
 * ## Campus mode — handling building events
 * ```kotlin
 * NavigineLocation(modifier = Modifier.fillMaxSize()) {
 *     BuildingHandlers(
 *         onBuildingFocused = { sublocations, activeId, switchSublocation ->
 *             // Show your custom floor selector
 *             // Call switchSublocation(id) to change the active floor
 *         },
 *         onBuildingLeft = {
 *             // Hide floor selector
 *         }
 *     )
 * }
 * ```
 *
 * ## Gesture and rendering settings
 * ```kotlin
 * NavigineLocation(
 *     modifier = Modifier.fillMaxSize(),
 *     uiSettings = LocationUiSettings(
 *         rotateGesturesEnabled = false,
 *         is3dEnabled = true
 *     )
 * )
 * ```
 *
 * @param modifier Modifier for the map container.
 * @param cameraPositionState Camera state holder, two-way synced with the SDK.
 * Use [rememberNavCameraPositionState] to create one.
 * @param properties Map configuration such as zoom limits and pick radius.
 * @param uiSettings Gesture toggles and rendering options such as 3D mode.
 * @param isVisible Controls map visibility without destroying the underlying view.
 * Useful for temporarily hiding the map while preserving its state.
 * @param onWindowReady Escape hatch — invoked once when [LocationWindow] is ready.
 * Prefer higher-level APIs where possible; use this only for SDK features
 * not yet exposed by the library.
 * @param content Map objects and interaction handlers declared as composables
 * ([com.navigine.locationview.objects.circle.Circle],
 * [com.navigine.locationview.interaction.InputHandlers],
 * [com.navigine.locationview.interaction.BuildingHandlers], etc.).
 *
 * @see DefaultNavigineLocation for a ready-to-use map with built-in UI controls.
 * @since 2.24.4
 */
@Composable
public fun NavigineLocation(
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
    val viewHolder = remember(context) { LocationViewHolder() }
    val windowState: MutableState<LocationWindow?> = remember { mutableStateOf(null) }
    val onWindowReadyState = rememberUpdatedState(onWindowReady)

    var glChild by remember { mutableStateOf<View?>(null) }

    AndroidView(
        modifier = modifier,
        factory = remember(context) {
            { ctx ->
                viewHolder.createView(ctx).also { lv ->
                    glChild = findGlChild(lv)
                    val win = lv.locationWindow
                    windowState.value = win
                    onWindowReadyState.value.invoke(win)
                }
            }
        },
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