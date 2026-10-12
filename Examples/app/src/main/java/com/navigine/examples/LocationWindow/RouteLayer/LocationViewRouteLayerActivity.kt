package com.navigine.examples.LocationWindow.RouteLayer

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.navigine.examples.databinding.ActivityLocationViewRouteLayerBinding
import com.navigine.idl.java.InputListener
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.Placement
import com.navigine.idl.java.RouteEndpoint
import com.navigine.idl.java.RouteLayer
import com.navigine.idl.java.RouteLayerListener
import com.navigine.idl.java.RouteLayerStatus
import com.navigine.idl.java.RouteProgress
import com.navigine.idl.java.RouteRemainingStyle
import com.navigine.idl.java.RouteView
import com.navigine.sdk.Navigine
import java.lang.Error
import java.util.Locale

/**
 * Example of drawing and following a route on the map with RouteLayer.
 *
 * RouteLayer builds the route from the user's current position, draws it,
 * rebuilds it when the user deviates or changes floors, and reports progress and arrival.
 */
class LocationViewRouteLayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RouteLayerActivity"
        private const val LOCATION_ID = 2025  // Assign the locationId you want to load (obtained from LocationList sample)

        private val ROUTE_COLOR = Color.rgb(76, 217, 100)
        private const val ROUTE_WIDTH = 8f
        private const val ROUTE_DOT_SIZE = 8f
        private const val ROUTE_DOT_SPACING = 8f
        private const val HINT_TEXT = "Long tap on the map to set a route target"
    }

    private var _binding: ActivityLocationViewRouteLayerBinding? = null
    private val binding get() = _binding!!

    private var locationManager: LocationManager? = null
    private var routeLayer: RouteLayer? = null

    // Keep listeners in fields: the SDK does not keep them alive
    private var locationListener: LocationListener? = null
    private var inputListener: InputListener? = null
    private var routeLayerListener: RouteLayerListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationViewRouteLayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        initListeners()
        initRouteLayer()
        binding.btnClearRoute.setOnClickListener { clearRoute() }
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
        routeLayer?.apply {
            removeRouteLayerListener(routeLayerListener)
            clear()
        }
        routeLayer = null
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
    }

    /**
     * One RouteLayer per LocationWindow. Style it once; the layer draws the route by itself.
     */
    private fun initRouteLayer() {
        routeLayer = NavigineSdk.getInstance()
            .getRouteLayer(binding.navigationLocationView.locationWindow)
            .apply {
                setWidth(ROUTE_WIDTH)
                setRemainingStyle(RouteRemainingStyle.DOTTED)
                setRemainingColor(ROUTE_COLOR)
                setRemainingPointSize(ROUTE_DOT_SIZE, ROUTE_DOT_SIZE)
                setRemainingPlacement(Placement.SPACED)
                setRemainingPlacementSpacing(ROUTE_DOT_SPACING)
                setRemainingPlacementMinRatio(0f)
                setRemainingCollisionEnabled(false)
                isVisible = true
                addRouteLayerListener(routeLayerListener)
            }
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

            /**
             * RouteEndpoint takes WGS84 coordinates directly — no conversion to meters needed.
             * The route starts from the user's current position.
             */
            override fun onViewLongTap(point: PointF?) {
                point ?: return
                val window = binding.navigationLocationView.locationWindow ?: return
                val target = window.screenPositionToGlobal(point) ?: return
                routeLayer?.setTarget(RouteEndpoint(target, window.sublocationId))
                showStatus("Building route...")
            }
        }

        routeLayerListener = object : RouteLayerListener() {
            override fun onRouteChanged(status: RouteLayerStatus?, route: RouteView?) {
                when (status) {
                    RouteLayerStatus.ROUTE_UPDATED ->
                        route?.let { showStatus(String.format(Locale.US, "Route: %.0f m", it.length())) }
                    RouteLayerStatus.IDLE, null -> Unit
                    else -> showStatus("Route status: $status")
                }
            }

            override fun onRouteAdvanced(progress: RouteProgress?) {
                progress ?: return
                showStatus(
                    String.format(
                        Locale.US,
                        "Remaining: %.0f m, %.0f s",
                        progress.remainingDistance,
                        progress.remainingTime
                    )
                )
            }

            override fun onRouteTargetReached() {
                Toast.makeText(this@LocationViewRouteLayerActivity, "Target reached", Toast.LENGTH_SHORT).show()
                clearRoute()
            }
        }
    }

    private fun clearRoute() {
        routeLayer?.clear()
        showStatus(HINT_TEXT)
    }

    private fun showStatus(text: String) {
        binding.routeStatusText.text = text
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
}