package com.navigine.examples.LocationWindow.Guidance

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.navigine.examples.R
import com.navigine.examples.databinding.ActivityLocationViewGuidanceBinding
import com.navigine.idl.java.GuidanceCamera
import com.navigine.idl.java.GuidanceCameraMode
import com.navigine.idl.java.GuidanceListener
import com.navigine.idl.java.InputListener
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.NavigationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.RouteEndpoint
import com.navigine.idl.java.RouteInstruction
import com.navigine.idl.java.RouteLayer
import com.navigine.idl.java.RouteLayerListener
import com.navigine.idl.java.RouteLayerStatus
import com.navigine.idl.java.RouteProgress
import com.navigine.idl.java.RouteView
import com.navigine.idl.java.UserLocationLayer
import com.navigine.sdk.Navigine
import java.lang.Error
import java.util.Locale

/**
 * Example of turn-by-turn guidance with GuidanceCamera.
 *
 * - RouteLayer builds and draws the route.
 * - GuidanceCamera drives the camera along the route and publishes the next instruction.
 * - UserLocationLayer shows the user on the map.
 */
class LocationViewGuidanceActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GuidanceActivity"
        private const val LOCATION_ID = 2025  // Assign the locationId you want to load (obtained from LocationList sample)
        private const val HINT_TEXT = "Long tap on the map to start guidance"
    }

    private var _binding: ActivityLocationViewGuidanceBinding? = null
    private val binding get() = _binding!!

    // Keep SDK objects in fields: the SDK holds them by weak references
    private var locationManager: LocationManager? = null
    private var navigationManager: NavigationManager? = null
    private var userLocationLayer: UserLocationLayer? = null
    private var routeLayer: RouteLayer? = null
    private var guidanceCamera: GuidanceCamera? = null

    private var locationListener: LocationListener? = null
    private var inputListener: InputListener? = null
    private var routeLayerListener: RouteLayerListener? = null
    private var guidanceListener: GuidanceListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationViewGuidanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        initListeners()
        initLayers()
        setViewListeners()
    }

    override fun onStart() {
        super.onStart()
        locationManager?.addLocationListener(locationListener)
        binding.navigationLocationView.locationWindow.addInputListener(inputListener)
    }

    override fun onStop() {
        super.onStop()
        locationManager?.removeLocationListener(locationListener)
        binding.navigationLocationView.locationWindow.removeInputListener(inputListener)
    }

    override fun onDestroy() {
        guidanceCamera?.removeListener(guidanceListener)
        guidanceCamera = null
        routeLayer?.apply {
            removeRouteLayerListener(routeLayerListener)
            clear()
        }
        routeLayer = null
        userLocationLayer?.setVisible(false)
        userLocationLayer = null
        super.onDestroy()
        _binding = null
    }

    private fun initSdk() {
        Navigine.initialize(applicationContext)
        val sdk = NavigineSdk.getInstance()
        // your server URL
        sdk.setServer("https://ips.navigine.com")
        // your user hash
        sdk.setUserHash("0000-0000-0000-0000")

        locationManager = sdk.locationManager
        locationManager?.locationId = LOCATION_ID

        // Positioning runs through NavigationManager — keep a reference to it
        navigationManager = sdk.getNavigationManager(locationManager)
    }

    /**
     * GuidanceCamera is bound to a RouteLayer of the same window.
     */
    private fun initLayers() {
        val sdk = NavigineSdk.getInstance()
        val window = binding.navigationLocationView.locationWindow

        userLocationLayer = sdk.getUserLocationLayer(window).apply { setVisible(true) }

        routeLayer = sdk.getRouteLayer(window).apply {
            isVisible = true
            addRouteLayerListener(routeLayerListener)
        }

        guidanceCamera = sdk.getGuidanceCamera(window, routeLayer).apply {
            set2DMode(binding.switch2d.isChecked)
            addListener(guidanceListener)
        }
    }

    private fun setViewListeners() {
        binding.guidanceModeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = checkedId.toGuidanceMode()
            if (guidanceCamera?.mode() != mode) guidanceCamera?.setMode(mode)
        }
        binding.switch2d.setOnCheckedChangeListener { _, isChecked ->
            guidanceCamera?.set2DMode(isChecked)
        }
        binding.btnClearRoute.setOnClickListener { clearRoute() }
    }

    private fun initListeners() {
        locationListener = object : LocationListener() {
            override fun onLocationLoaded(location: Location?) {
                location?.let { loadSublocation(it) }
            }

            override fun onLocationUploaded(locationId: Int) {}

            override fun onLocationFailed(locationId: Int, error: Error?) {
                Log.e(TAG, "onLocationFailed: $error")
            }
        }

        inputListener = object : InputListener() {
            override fun onViewTap(point: PointF?) {}
            override fun onViewDoubleTap(point: PointF?) {}

            override fun onViewLongTap(point: PointF?) {
                point ?: return
                val window = binding.navigationLocationView.locationWindow ?: return
                val target = window.screenPositionToGlobal(point) ?: return
                routeLayer?.setTarget(RouteEndpoint(target, window.sublocationId))
                guidanceCamera?.setMode(GuidanceCameraMode.FOLLOWING)
                showInstruction("Building route...")
            }
        }

        // RouteLayer reports route status and arrival; instructions come from GuidanceCamera
        routeLayerListener = object : RouteLayerListener() {
            override fun onRouteChanged(status: RouteLayerStatus?, route: RouteView?) {
                when (status) {
                    RouteLayerStatus.ROUTE_UPDATED, RouteLayerStatus.IDLE, null -> Unit
                    else -> showInstruction("Route status: $status")
                }
            }

            override fun onRouteAdvanced(progress: RouteProgress?) {}

            override fun onRouteTargetReached() {
                Toast.makeText(this@LocationViewGuidanceActivity, "Target reached", Toast.LENGTH_SHORT).show()
                clearRoute()
            }
        }

        guidanceListener = object : GuidanceListener() {
            /**
             * Called when the mode changes, including a user gesture switching it to FREE.
             */
            override fun onCameraModeChanged(mode: GuidanceCameraMode?) {
                binding.guidanceModeGroup.check(mode.toButtonId())
            }

            override fun onInstructionChanged(instruction: RouteInstruction?) {
                instruction?.let { showInstruction(it.format()) }
            }
        }
    }

    private fun clearRoute() {
        routeLayer?.clear()
        guidanceCamera?.setMode(GuidanceCameraMode.FREE)
        showInstruction(HINT_TEXT)
    }

    private fun showInstruction(text: String) {
        binding.instructionText.text = text
    }

    private fun RouteInstruction.format(): String {
        val action = title?.takeIf { it.isNotBlank() } ?: type.name.lowercase(Locale.US)
        return String.format(Locale.US, "In %.0f m: %s", distance, action)
    }

    /**
     * Load the first sublocation of the given Location and fit the zoom to it.
     */
    private fun loadSublocation(location: Location) {
        val sublocation = location.sublocations.firstOrNull() ?: return

        binding.navigationLocationView.post {
            binding.navigationLocationView.locationWindow?.apply {
                setSublocationId(sublocation.id)
                val pixelWidth =
                    binding.navigationLocationView.width / resources.displayMetrics.density
                maxZoomFactor = (pixelWidth * 16f / sublocation.width).coerceIn(1f, 600f)
                minZoomFactor = (pixelWidth / 16f / sublocation.width).coerceIn(1f, 600f)
                zoomFactor = (pixelWidth / sublocation.width).coerceIn(minZoomFactor, maxZoomFactor)
            }
        }
    }

    private fun Int.toGuidanceMode(): GuidanceCameraMode = when (this) {
        R.id.btn_mode_following -> GuidanceCameraMode.FOLLOWING
        R.id.btn_mode_overview -> GuidanceCameraMode.OVERVIEW
        else -> GuidanceCameraMode.FREE
    }

    private fun GuidanceCameraMode?.toButtonId(): Int = when (this) {
        GuidanceCameraMode.FOLLOWING -> R.id.btn_mode_following
        GuidanceCameraMode.OVERVIEW -> R.id.btn_mode_overview
        else -> R.id.btn_mode_free
    }
}