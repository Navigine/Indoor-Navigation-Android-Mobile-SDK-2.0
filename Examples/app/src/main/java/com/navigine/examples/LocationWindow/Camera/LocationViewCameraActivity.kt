package com.navigine.examples.LocationWindow.Camera

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.navigine.examples.databinding.ActivityLocationViewCameraBinding
import com.navigine.idl.java.AnimationType
import com.navigine.idl.java.Camera
import com.navigine.idl.java.CameraCallback
import com.navigine.idl.java.CameraListener
import com.navigine.idl.java.CameraUpdateReason
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.Point
import kotlinx.coroutines.launch
import java.lang.Error


/**
 * Activity showcasing how to display an indoor location with interactive camera controls.
 *
 * Users can:
 *  - Zoom in and out using UI buttons.
 *  - Fly or move the camera programmatically to a target point.
 *  - Load and switch to a specific sublocation with zoom constraints.
 *  - Listen for map and camera updates via callbacks.
 */
class LocationViewCameraActivity : AppCompatActivity() {

    private var _binding: ActivityLocationViewCameraBinding? = null
    private val binding get() = _binding!!

    private var locationManager: LocationManager? = null

    private var locationListener: LocationListener? = null
    private var cameraListener: CameraListener? = null

    private var defaultCameraZoom = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationViewCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        setViewListeners()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        addListeners()
    }

    override fun onPause() {
        super.onPause()
        removeListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setViewListeners() {
        binding.btnZoomIn.setOnClickListener { zoomIn() }
        binding.btnZoomOut.setOnClickListener { zoomOut() }
    }

    /**
     * Increase zoomFactor by 2x on current camera window.
     */
    private fun zoomIn() {
        binding.navigationLocationView.locationWindow.apply {
            zoomFactor *= 2f
        }
    }

    /**
     * Decrease zoomFactor by 2x on current camera window.
     */
    private fun zoomOut() {
        binding.navigationLocationView.locationWindow.apply {
            zoomFactor /= 2f
        }
    }

    /**
     * Animate camera to a target configuration smoothly.
     *
     * @param target Camera object with center point, zoom, and rotation.
     * @param duration Animation duration in milliseconds (default 300).
     */
    private fun adjustCamera(target: Camera, duration: Int = 300) {
        lifecycleScope.launch {
            binding.navigationLocationView.locationWindow?.flyTo(target, duration, /* listener= */ null)
        }
    }

    /**
     * Move camera with a specific animation type.
     *
     * @param target Camera object with center point, zoom, and rotation.
     * @param duration Animation duration in milliseconds (default 300).
     * @param animationType Type of interpolation (e.g., CUBIC).
     */
    private fun moveCamera(
        target: Camera,
        duration: Int = 300,
        animationType: AnimationType = AnimationType.CUBIC
    ) {
        lifecycleScope.launch {
            binding.navigationLocationView.locationWindow?.moveTo(
                target, duration, animationType, object : CameraCallback(){
                    override fun onMoveFinished(finished: Boolean) {
                        Log.d("CameraCallback", "onMoveFinished: $finished")
                    }
                }
            )
        }
    }

    /**
     * Store the current zoom level to allow resetting later.
     */
    private fun setupZoomCameraDefault() {
        defaultCameraZoom = binding.navigationLocationView.locationWindow?.camera?.zoom ?: 0f
    }

    /**
     * Load the first sublocation of the given Location, apply zoom constraints,
     * and initialize the camera to fit.
     *
     * @param location Loaded Location containing sublocations.
     */
    private fun loadSublocation(location: Location) {
        // you can choose any sublocation, it is ordered ascending by index
        val sublocation = location.sublocations.first() ?: return

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
            setupZoomCameraDefault()
        }
    }

    /**
     * Subscribe to location and camera updates.
     */
    private fun addListeners() {
        locationManager?.addLocationListener(locationListener)
        binding.navigationLocationView.locationWindow.addCameraListener(cameraListener)
    }

    private fun removeListeners() {
        locationManager?.removeLocationListener(locationListener)
        binding.navigationLocationView.locationWindow.removeCameraListener(cameraListener)
    }

    private fun initListeners() {
        locationListener = object : LocationListener() {
            /**
             * Called when the location data is fully loaded.
             */
            override fun onLocationLoaded(location: Location?) {
                loadSublocation(location ?: return)
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

        cameraListener = object : CameraListener() {
            /**
             * Called whenever the camera position is updated.
             *
             * @param reason Source of camera change (GESTURES or APPLICATION).
             * @param isFinished True if animation has completed, false if in progress.
             */
            override fun onCameraPositionChanged(reason: CameraUpdateReason?, isFinished: Boolean) {
                val message = when(reason){
                    CameraUpdateReason.GESTURES -> "Camera position changed by gestures"
                    CameraUpdateReason.APPLICATION -> "Camera position changed by application"
                    else -> "Camera position changed"
                }
                Log.d("CameraListener", message)
            }
        }
    }

    private fun initSdk() {
        val sdk = NavigineSdk.getInstance()
        // your server URL
        sdk.setServer("https://ips.navigine.com")
        // your user hash
        sdk.setUserHash("0000-0000-0000-0000")

        locationManager = sdk.locationManager
        // Assign the locationId you want to load (obtained from LocationList sample)
        locationManager?.locationId = 2076
    }
}