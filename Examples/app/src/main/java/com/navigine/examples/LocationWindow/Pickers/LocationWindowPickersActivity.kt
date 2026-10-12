package com.navigine.examples.LocationWindow.Pickers

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.navigine.examples.databinding.ActivityLocationWindowPickersBinding
import com.navigine.idl.java.InputListener
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.MapObjectPickResult
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.PickListener
import com.navigine.sdk.Navigine
import java.lang.Error
import java.util.HashMap
import java.util.Locale

/**
 * Activity demonstrating how to handle user interactions on a Navigine indoor map.
 *
 * Features:
 *  1. Initialize and load a location with its sublocation.
 *  2. Detect tap gestures (single, double, long) via InputListener.
 *  3. Pick map objects and features at the tapped screen position via PickListener.
 *  4. Convert a screen position to WGS84 coordinates on long tap.
 *  5. Display feedback using Toast messages.
 */
class LocationWindowPickersActivity : AppCompatActivity() {

    companion object {
        private const val KEY_FEATURE_NAME = "name"
    }

    private var _binding: ActivityLocationWindowPickersBinding? = null
    private val binding get() = _binding!!

    private var locationManager: LocationManager? = null

    // Keep listeners in fields: the SDK does not keep them alive,
    // an unreferenced listener can be garbage-collected and stop receiving callbacks.
    private var locationListener: LocationListener? = null
    private var inputListener: InputListener? = null
    private var pickListener: PickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationWindowPickersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        initListeners()
    }

    /**
     * Register listeners when the activity enters the foreground.
     */
    override fun onResume() {
        super.onResume()
        addListeners()
    }

    /**
     * Unregister listeners when the activity is paused to avoid memory leaks.
     */
    override fun onPause() {
        super.onPause()
        removeListeners()
    }

    /**
     * Clear view binding to free resources.
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * Load the first sublocation of the given Location, apply zoom constraints,
     * and initialize the camera to fit.
     *
     * @param location Loaded Location containing sublocations.
     */
    private fun loadSublocation(location: Location) {
        // you can choose any sublocation, it is ordered ascending by index
        val sublocation = location.sublocations.firstOrNull() ?: return

        binding.navigationLocationView.post {
            binding.navigationLocationView.locationWindow?.apply {
                // Switch to chosen sublocation by its unique ID
                setSublocationId(sublocation.id)
                // Calculate pixel width in dp for zoom fitting
                val pixelWidth =
                    binding.navigationLocationView.width / resources.displayMetrics.density
                // Define maximum and minimum zoom based on sublocation dimensions
                maxZoomFactor = (pixelWidth * 16f / sublocation.width).coerceIn(1f, 600f)
                minZoomFactor = (pixelWidth / 16f / sublocation.width).coerceIn(1f, 600f)

                // Initialize zoom factor to fit sublocation view
                zoomFactor = (pixelWidth / sublocation.width).coerceIn(minZoomFactor, maxZoomFactor)
            }
        }
    }

    private fun initSdk() {
        Navigine.initialize(this.applicationContext)
        val sdk = NavigineSdk.getInstance()
        // your server URL
        sdk.setServer("https://ips.navigine.com")
        // your user hash
        sdk.setUserHash("0000-0000-0000-0000")

        locationManager = sdk.locationManager
        // Assign the locationId you want to load (obtained from LocationList sample)
        locationManager?.locationId = 1111
    }

    /**
     * Subscribe to location, input and pick updates.
     */
    private fun addListeners() {
        locationManager?.addLocationListener(locationListener)
        binding.navigationLocationView.locationWindow.apply {
            addPickListener(pickListener)
            addInputListener(inputListener)
        }
    }

    /**
     * Remove all listeners to stop callbacks and prevent leaks.
     */
    private fun removeListeners() {
        locationManager?.removeLocationListener(locationListener)
        binding.navigationLocationView.locationWindow.apply {
            removePickListener(pickListener)
            removeInputListener(inputListener)
        }
    }

    /**
     * Instantiate listeners for location loading, input gestures, and map picking.
     */
    private fun initListeners() {
        locationListener = object : LocationListener() {
            /**
             * Called when the location data is fully loaded.
             */
            override fun onLocationLoaded(location: Location?) {
                location?.let { loadSublocation(it) }
            }

            /**
             * For Internal usage
             */
            override fun onLocationUploaded(locationId: Int) {}

            /**
             * Called if loading the location fails due to an error.
             */
            override fun onLocationFailed(locationId: Int, error: Error?) {
                Log.e("LocationListener", "onLocationFailed: $error")
            }
        }

        // Listener for user tap gestures on the locationView
        inputListener = object : InputListener() {
            /**
             * Picking is not automatic: request it explicitly at the tapped position.
             * Results are delivered to the PickListener below.
             */
            override fun onViewTap(point: PointF?) {
                point ?: return
                binding.navigationLocationView.locationWindow?.apply {
                    pickMapObjectAt(point)
                    pickMapFeatureAt(point)
                }
            }

            override fun onViewDoubleTap(point: PointF?) {
                showToast("Double tap")
            }

            /**
             * Converts the screen position to WGS84 coordinates.
             */
            override fun onViewLongTap(point: PointF?) {
                point ?: return
                val global = binding.navigationLocationView.locationWindow
                    ?.screenPositionToGlobal(point)
                    ?: return
                showToast(
                    String.format(Locale.US, "Long tap at %.6f, %.6f", global.latitude, global.longitude)
                )
            }
        }

        // Listener for picking map objects and features
        pickListener = object : PickListener() {
            /**
             * Called with null when there is no interactive map object at the position.
             */
            override fun onMapObjectPickComplete(
                mapObjectPickResult: MapObjectPickResult?,
                screenPosition: PointF?
            ) {
                val type = mapObjectPickResult?.mapObject?.type ?: return
                showToast("Map object picked: ${type.name}")
            }

            override fun onMapFeaturePickComplete(
                mapFeaturePickResult: HashMap<String, String>?,
                point: PointF?
            ) {
                val name = mapFeaturePickResult?.get(KEY_FEATURE_NAME) ?: return
                showToast("Map feature picked: $name")
            }
        }
    }

    /**
     * Utility to display a short Toast message.
     *
     * @param message Text to show in the toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}