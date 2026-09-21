package com.navigine.locationview.tracking

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.navigine.idl.java.LiveDataManager
import com.navigine.idl.java.LiveObjectsLayer
import com.navigine.idl.java.LiveObjectsLayerFilter
import com.navigine.idl.java.LiveObjectsLayerListener
import com.navigine.idl.java.LiveTrackedObjectInfo
import com.navigine.idl.java.LiveZoneClickInfo
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.ObjectListManager
import com.navigine.idl.java.OperatingMode
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.camera.NavCameraPositionState
import com.navigine.locationview.camera.rememberNavCameraPositionState
import com.navigine.locationview.effects.LocalLocationWindow
import com.navigine.locationview.internal.listeners.CameraListenerBridge
import com.navigine.locationview.internal.node.LocationApplier
import com.navigine.locationview.internal.node.LocationRootNode
import com.navigine.locationview.internal.node.ifValid
import com.navigine.locationview.internal.updates.applyOperatingMode
import com.navigine.locationview.internal.updates.applyOutdoorMapConfig
import com.navigine.locationview.internal.updates.applyProperties
import com.navigine.locationview.internal.updates.applyUiSettings
import com.navigine.locationview.settings.DefaultLocationProperties
import com.navigine.locationview.settings.DefaultLocationUiSettings
import com.navigine.locationview.settings.LocationProperties
import com.navigine.locationview.settings.LocationUiSettings
import com.navigine.locationview.settings.OutdoorMapConfig
import com.navigine.locationview.tracking.internal.DefaultTrackingViewHolder
import com.navigine.locationview.utils.findGlChild
import com.navigine.locationview.utils.setChildrenGone
import com.navigine.view.DefaultNavigineView
import com.navigine.view.DefaultNavigineViewConfig
import com.navigine.view.DefaultTrackingView
import java.util.ArrayList

private const val TAG = "DefaultTrackingLocation"

/**
 * Ready-to-use tracking map with built-in UI chrome (zoom controls, floor selector)
 * and a live objects layer bound to [LiveDataManager] / [ObjectListManager].
 *
 * This is a tracking-specific counterpart to
 * [com.navigine.locationview.DefaultNavigineLocation]. Both share the same base chrome
 * (zoom + floor selector), but differ in what's layered on top:
 *
 * - [DefaultTrackingNavigineLocation] renders live tracked objects
 *   ([LiveObjectsLayer]) — icons, clusters, optional footprint tracks, and zone
 *   count badges.
 * - [com.navigine.locationview.DefaultNavigineLocation] renders the follow-me button
 *   and user location (blue dot).
 *
 * Neither includes the other's feature. If you need both live tracking *and*
 * follow-me / user location in the same screen, compose your own view using
 * [com.navigine.locationview.NavigineLocation] instead of either default wrapper.
 *
 * **Tracking-only:** this composable exists exclusively in the `tracking` flavor of
 * this artifact (`navigine-locationview-compose-tracking`). It is not present in the
 * standard `navigine-locationview-compose` artifact published to Maven Central,
 * because it depends on `com.navigine.idl.java.LiveObjectsLayer` and related types
 * that only exist in `sdk-tracking`.
 *
 * ## Live layer lifecycle
 *
 * Unlike chrome config (zoom/floor), which [DefaultTrackingView] only accepts at
 * construction time, the live objects layer is bound *after* the underlying
 * Android view and its [LocationWindow] already exist — [LiveDataManager] and
 * [ObjectListManager] are supplied by the caller, not created internally, and
 * binding happens once both become non-null:
 *
 * ```kotlin
 * val liveDataManager by viewModel.liveDataManager.collectAsStateWithLifecycle()
 * val objectListManager by viewModel.objectListManager.collectAsStateWithLifecycle()
 *
 * DefaultTrackingNavigineLocation(
 *     modifier = Modifier.fillMaxSize(),
 *     liveDataManager = liveDataManager,
 *     objectListManager = objectListManager,
 * )
 * ```
 *
 * Passing `null` for either manager is valid and expected while the app hasn't
 * finished configuring the Tracking SDK (e.g. before login, or during a cold-start
 * session restore) — the map renders normally, just without the live layer, until
 * both managers become available. If either manager changes identity later (e.g.
 * after a reconfigure), the layer is rebound automatically; any previously bound
 * layer is replaced, not stacked.
 *
 * ## Basic usage
 * ```kotlin
 * DefaultTrackingNavigineLocation(
 *     modifier = Modifier.fillMaxSize(),
 *     liveDataManager = liveDataManager,
 *     objectListManager = objectListManager,
 * )
 * ```
 *
 * ## Zone occupancy mode with tap handling
 * ```kotlin
 * DefaultTrackingNavigineLocation(
 *     modifier = Modifier.fillMaxSize(),
 *     liveDataManager = liveDataManager,
 *     objectListManager = objectListManager,
 *     liveObjectsLayerConfig = LiveObjectsLayerConfig(
 *         mode = LiveObjectsLayerMode.ZONES,
 *     ),
 *     onZoneClick = { info ->
 *         selectedZone = info.zoneName to info.objects.size
 *     },
 * )
 * ```
 *
 * ## Filtering to a single group with footprint tracks
 * ```kotlin
 * DefaultTrackingNavigineLocation(
 *     modifier = Modifier.fillMaxSize(),
 *     liveDataManager = liveDataManager,
 *     objectListManager = objectListManager,
 *     liveObjectsLayerConfig = LiveObjectsLayerConfig(
 *         tracksEnabled = true,
 *         filter = LiveObjectsLayerFilter(
 *             zoneIds = null,
 *             groupIds = arrayListOf(equipmentGroupId),
 *             objectIds = null,
 *         ),
 *     ),
 *     onObjectClick = { info -> selectedObjectId = info.`object`.id },
 * )
 * ```
 *
 * ## With additional map objects
 * ```kotlin
 * DefaultTrackingNavigineLocation(
 *     modifier = Modifier.fillMaxSize(),
 *     liveDataManager = liveDataManager,
 *     objectListManager = objectListManager,
 * ) {
 *     Polyline(points = plannedRoute, color = Color.Blue, width = 3f)
 * }
 * ```
 *
 * @param modifier Modifier for the map container.
 * @param cameraPositionState Camera state holder (two-way synced with SDK), same
 * contract as [com.navigine.locationview.DefaultNavigineLocation].
 * @param properties Map configuration (zoom limits, pick radius, etc.), applied
 * reactively — safe to change after first composition.
 * @param uiSettings Gesture controls (rotate, tilt, scroll, zoom), applied reactively.
 * @param operatingMode Whether the map renders indoor content, the outdoor basemap,
 * or both. Defaults to [OperatingMode.INDOOR_ONLY] to preserve pre-2.27 behavior.
 * Reactive — safe to change after first composition.
 * @param outdoorMapConfig Outdoor basemap appearance (theme, attribution overlay placement).
 * Has no effect while the map is in [OperatingMode.INDOOR_ONLY]. Reactive — safe to
 * change after first composition.
 * @param viewConfig Base chrome visibility/appearance, forwarded to [DefaultTrackingView]
 * at construction. **Creation-time only** — changes after the first frame are not
 * applied, since [DefaultTrackingView] has no post-creation setter for it.
 * @param widgetConfig Zoom/floor-selector chrome appearance. **Creation-time only**,
 * for the same reason as [viewConfig] — see [DefaultTrackingWidgetConfig].
 * @param liveDataManager Configured [LiveDataManager] (app id / MQTT connection owned
 * by the caller). `null` until the caller's Tracking SDK setup is ready; the live
 * layer stays unbound until both this and [objectListManager] are non-null.
 * @param objectListManager Configured [ObjectListManager] — source of pin style
 * (color, size, icon URL) for live object icons. Same `null`-until-ready contract
 * as [liveDataManager].
 * @param liveObjectsLayerConfig Visibility, clustering, footprint tracks, rendering
 * mode ([LiveObjectsLayerMode.OBJECTS] vs [LiveObjectsLayerMode.ZONES]), and map
 * filter for the live layer. Applied reactively once the layer is bound; has no
 * effect before that (values are held and applied as soon as binding completes).
 * @param isVisible Controls the underlying view's visibility without destroying it
 * (e.g. when the tab holding this composable is backgrounded but not disposed).
 * Does not affect [liveObjectsLayerConfig]'s own `visible` flag — that one
 * toggles the live layer specifically; this one toggles the whole rendered view.
 * @param onObjectClick Invoked when the user taps a single live object icon.
 * Not invoked for taps on a cluster — see [onClusterClick].
 * @param onClusterClick Invoked when the user taps a cluster of live object icons
 * (only relevant when [LiveObjectsLayerConfig.clusteringEnabled] is true). Carries
 * the icons currently grouped into that cluster, in unspecified order.
 * @param onZoneClick Invoked when the user taps a zone occupancy badge — only
 * fires in [LiveObjectsLayerMode.ZONES]; badges (including zero-count ones) aren't
 * shown, and this callback isn't invoked, in [LiveObjectsLayerMode.OBJECTS].
 * @param onWindowReady Callback when [LocationWindow] is created (called once, on
 * first successful view creation — not re-invoked on recomposition).
 * @param content Additional map objects (Icon, Circle, Polyline, etc.), composed
 * into a separate SDK-backed composition layered on top of the live objects layer.
 *
 * @see com.navigine.locationview.DefaultNavigineLocation for the follow-me/user-location
 * counterpart without live tracking.
 * @see com.navigine.locationview.NavigineLocation for full manual control when you need
 * both live tracking and follow-me/user location together.
 *
 * @since 2.26.2
 */
@Composable
@NavigineMapComposable
public fun DefaultTrackingNavigineLocation(
    modifier: Modifier = Modifier,
    cameraPositionState: NavCameraPositionState = rememberNavCameraPositionState(),
    properties: LocationProperties = DefaultLocationProperties,
    uiSettings: LocationUiSettings = DefaultLocationUiSettings,
    operatingMode: OperatingMode = OperatingMode.INDOOR_ONLY,
    outdoorMapConfig: OutdoorMapConfig = OutdoorMapConfig.Default,
    viewConfig: DefaultNavigineViewConfig = DefaultNavigineViewConfig.defaultConfig(),
    widgetConfig: DefaultTrackingWidgetConfig = DefaultTrackingWidgetConfig.Default,
    liveDataManager: LiveDataManager?,
    objectListManager: ObjectListManager?,
    liveObjectsLayerConfig: LiveObjectsLayerConfig = LiveObjectsLayerConfig.Default,
    isVisible: Boolean = true,
    onObjectClick: ((LiveTrackedObjectInfo) -> Unit)? = null,
    onClusterClick: ((List<LiveTrackedObjectInfo>) -> Unit)? = null,
    onZoneClick: ((LiveZoneClickInfo) -> Unit)? = null,
    onWindowReady: (LocationWindow) -> Unit = {},
    content: @Composable @NavigineMapComposable () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewHolder = remember(context) { DefaultTrackingViewHolder() }
    val windowState: MutableState<LocationWindow?> = remember { mutableStateOf(null) }
    val onWindowReadyState = rememberUpdatedState(onWindowReady)

    // Creation-time only: DefaultTrackingView has no post-creation setter for chrome config.
    val initialViewConfig = remember { viewConfig }
    val initialZoomConfig = remember { widgetConfig.zoomConfig }
    val initialFloorConfig = remember { widgetConfig.floorConfig }

    AndroidView<DefaultTrackingView>(
        modifier = modifier,
        factory = remember(context) {
            {
                viewHolder.createView(context, initialViewConfig, initialZoomConfig, initialFloorConfig)
                    .also { lv ->
                        val win = lv.locationWindow
                        windowState.value = win
                        onWindowReadyState.value.invoke(win)
                    }
            }
        },
        update = { lv ->
            if (isVisible) {
                findGlChild(lv)?.visibility = View.VISIBLE
                lv.setViewConfig(lv.viewConfig)
            } else {
                setChildrenGone(lv)
            }
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

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            viewHolder.onStart()
        }

        onDispose {
            val win = windowState.value
            windowState.value = null

            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                viewHolder.onStop()
            }

            win?.ifValid { runCatching { removeAllMapObjects() } }
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
            val cameraBridge = CameraListenerBridge(cameraPositionState)
            window.ifValid {
                runCatching { addCameraListener(cameraBridge) }
                    .onFailure { Log.e(TAG, "Failed to add camera listener", it) }
            }

            onDispose {
                window.ifValid { runCatching { removeCameraListener(cameraBridge) } }
                cameraPositionState.window = null
            }
        }

        var prevProps by remember(window) { mutableStateOf<LocationProperties?>(null) }
        var prevUi by remember(window) { mutableStateOf<LocationUiSettings?>(null) }
        var prevOperatingMode by remember(window) { mutableStateOf<OperatingMode?>(null) }
        var prevOutdoorMapConfig by remember(window) { mutableStateOf<OutdoorMapConfig?>(null) }

        SideEffect {
            window?.ifValid {
                applyProperties(this, properties, prevProps)
                applyUiSettings(this, uiSettings, prevUi)
                applyOperatingMode(this, operatingMode, prevOperatingMode)
                applyOutdoorMapConfig(this, outdoorMapConfig, prevOutdoorMapConfig)
                prevProps = properties
                prevUi = uiSettings
                prevOperatingMode = operatingMode
                prevOutdoorMapConfig = outdoorMapConfig
            }
        }

        // Bind live layer once the view and both managers are available. bindLiveDataManager
        // replaces any previously bound layer, so this must not re-run on every recomposition —
        // gated by LaunchedEffect keys, not called from SideEffect.
        var liveObjectsLayer by remember { mutableStateOf<LiveObjectsLayer?>(null) }
        val trackingView = viewHolder.view

        LaunchedEffect(trackingView, liveDataManager, objectListManager) {
            if (trackingView != null && liveDataManager != null && objectListManager != null && windowState.value?.isValid == true) {
                trackingView.bindLiveDataManager(liveDataManager, objectListManager)
                liveObjectsLayer = trackingView.liveObjectsLayer
            }
        }

        var prevLayerConfig by remember { mutableStateOf<LiveObjectsLayerConfig?>(null) }

        SideEffect {
            val layer = liveObjectsLayer
            if (layer != null && layer.isValid) {
                val prev = prevLayerConfig
                if (prev == null || prev.visible != liveObjectsLayerConfig.visible) {
                    layer.setVisible(liveObjectsLayerConfig.visible)
                }
                if (prev == null || prev.clusteringEnabled != liveObjectsLayerConfig.clusteringEnabled) {
                    layer.setClusteringEnabled(liveObjectsLayerConfig.clusteringEnabled)
                }
                if (prev == null || prev.tracksEnabled != liveObjectsLayerConfig.tracksEnabled) {
                    layer.setTracksEnabled(liveObjectsLayerConfig.tracksEnabled)
                }
                if (prev == null || prev.mode != liveObjectsLayerConfig.mode) {
                    layer.setMode(liveObjectsLayerConfig.mode)
                }
                if (prev == null || prev.filter != liveObjectsLayerConfig.filter) {
                    // null filter object == "show all"; expressed as explicit null allow-lists,
                    // not passed as a null LiveObjectsLayerFilter reference (contract unconfirmed for null).
                    layer.setFilter(liveObjectsLayerConfig.filter ?: LiveObjectsLayerFilter(null, null, null))
                }
                prevLayerConfig = liveObjectsLayerConfig
            }
        }

        val onObjectClickState = rememberUpdatedState(onObjectClick)
        val onClusterClickState = rememberUpdatedState(onClusterClick)
        val onZoneClickState = rememberUpdatedState(onZoneClick)

        DisposableEffect(liveObjectsLayer) {
            val layer = liveObjectsLayer ?: return@DisposableEffect onDispose {}

            val listener = object : LiveObjectsLayerListener() {
                override fun onObjectClick(info: LiveTrackedObjectInfo) {
                    onObjectClickState.value?.invoke(info)
                }

                override fun onClusterClick(infos: ArrayList<LiveTrackedObjectInfo>) {
                    onClusterClickState.value?.invoke(infos)
                }

                override fun onZoneClick(info: LiveZoneClickInfo) {
                    onZoneClickState.value?.invoke(info)
                }
            }

            runCatching { layer.addLiveObjectsLayerListener(listener) }
                .onFailure { Log.e(TAG, "Failed to add live objects layer listener", it) }

            onDispose {
                runCatching { layer.removeLiveObjectsLayerListener(listener) }
            }
        }

        DisposableEffect(window, parentComposition) {
            if (window == null || !window.isValid) return@DisposableEffect onDispose {}

            val root = LocationRootNode(window)
            val applier = LocationApplier(root)
            val composition = Composition(applier, parentComposition)

            composition.setContent { currentContent.value() }

            onDispose { composition.dispose() }
        }
    }
}