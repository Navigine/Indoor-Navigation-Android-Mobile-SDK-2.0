package com.navigine.examples.LocationWindow.GlobalMap

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.navigine.examples.databinding.ActivityLocationViewGlobalMapBinding
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.MapTheme
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.OperatingMode
import com.navigine.sdk.Navigine
import java.lang.Error

/**
 * Example of switching the map between indoor-only and global mode
 * (indoor floors on top of the OSM outdoor basemap).
 *
 * Global mode needs more than an operating mode switch:
 *  - stickToBorder must be off, otherwise the camera stays locked to the floor;
 *  - minZoomFactor must be lowered, otherwise you can't zoom out beyond the floor.
 */
class LocationViewGlobalMapActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GlobalMapActivity"
        private const val LOCATION_ID = 2025  // Assign the locationId you want to load (obtained from LocationList sample)

        private const val FLOOR_ZOOM_RANGE = 16f
        private const val MIN_ZOOM_FACTOR = 1f
        private const val MAX_ZOOM_FACTOR = 600f
        private const val GLOBAL_MIN_ZOOM_FACTOR = 0.01f
    }

    private var _binding: ActivityLocationViewGlobalMapBinding? = null
    private val binding get() = _binding!!

    private var locationManager: LocationManager? = null

    // Keep SDK objects in fields: the SDK holds them by weak references
    private var locationListener: LocationListener? = null

    // Indoor zoom limit of the loaded floor, restored when global mode is turned off
    private var floorMinZoomFactor: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationViewGlobalMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        initListeners()
        setViewListeners()
    }

    override fun onStart() {
        super.onStart()
        locationManager?.addLocationListener(locationListener)
    }

    override fun onStop() {
        super.onStop()
        locationManager?.removeLocationListener(locationListener)
    }

    override fun onDestroy() {
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

    private fun setViewListeners() {
        binding.switchGlobalMap.setOnCheckedChangeListener { _, isChecked ->
            setGlobalMapEnabled(isChecked)
        }
        binding.switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            // Affects only the outdoor basemap; indoor floors and map objects are unchanged
            binding.navigationLocationView.locationWindow?.mapTheme =
                if (isChecked) MapTheme.DARK else MapTheme.LIGHT
        }
    }

    /**
     * Switches the operating mode and the camera limits that come with it.
     */
    private fun setGlobalMapEnabled(enabled: Boolean) {
        val window = binding.navigationLocationView.locationWindow ?: return
        window.operatingMode = if (enabled) OperatingMode.OUTDOOR_INDOOR else OperatingMode.INDOOR_ONLY
        window.stickToBorder = !enabled
        window.minZoomFactor = if (enabled) {
            GLOBAL_MIN_ZOOM_FACTOR
        } else {
            floorMinZoomFactor ?: window.minZoomFactor
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
    }

    /**
     * Load the first sublocation in indoor mode and fit the zoom to it.
     *
     * The map starts indoor on purpose: the camera is positioned over the floor first.
     * A map created directly in global mode has nothing to center on and opens
     * at (0, 0) — in the middle of the ocean.
     */
    private fun loadSublocation(location: Location) {
        val sublocation = location.sublocations.firstOrNull() ?: return

        binding.navigationLocationView.post {
            binding.navigationLocationView.locationWindow?.apply {
                setSublocationId(sublocation.id)
                val pixelWidth =
                    binding.navigationLocationView.width / resources.displayMetrics.density
                val fitZoom = pixelWidth / sublocation.width
                maxZoomFactor = (fitZoom * FLOOR_ZOOM_RANGE).coerceIn(MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR)
                minZoomFactor = (fitZoom / FLOOR_ZOOM_RANGE).coerceIn(MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR)
                zoomFactor = fitZoom.coerceIn(minZoomFactor, maxZoomFactor)
                floorMinZoomFactor = minZoomFactor
            }
            setGlobalMapEnabled(binding.switchGlobalMap.isChecked)
            binding.switchGlobalMap.isEnabled = true
        }
    }
}