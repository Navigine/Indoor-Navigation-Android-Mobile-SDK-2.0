package com.navigine.examples.LocationWindow.UserLocation

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.navigine.examples.R
import com.navigine.examples.databinding.ActivityLocationViewUserLocationBinding
import com.navigine.idl.java.CameraListener
import com.navigine.idl.java.CameraUpdateReason
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.NavigationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.UserLocationFollowMode
import com.navigine.idl.java.UserLocationLayer
import com.navigine.sdk.Navigine
import java.lang.Error

/**
 * Example of showing the user's position on the map with UserLocationLayer
 * and making the camera follow it.
 *
 * UserLocationLayer draws the position arrow and accuracy circle by itself —
 * no need to subscribe to positions and move a marker manually.
 */
class LocationViewUserLocationActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserLocationActivity"
        private const val LOCATION_ID = 2025  // Assign the locationId you want to load (obtained from LocationList sample)

        private val ACCURACY_COLOR = Color.argb(66, 48, 170, 217)
    }

    private var _binding: ActivityLocationViewUserLocationBinding? = null
    private val binding get() = _binding!!

    // Keep SDK objects in fields: the SDK holds them by weak references
    private var locationManager: LocationManager? = null
    private var navigationManager: NavigationManager? = null
    private var userLocationLayer: UserLocationLayer? = null

    private var locationListener: LocationListener? = null
    private var cameraListener: CameraListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationViewUserLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        initListeners()
        initUserLocationLayer()
        setViewListeners()
    }

    override fun onStart() {
        super.onStart()
        locationManager?.addLocationListener(locationListener)
        binding.navigationLocationView.locationWindow.addCameraListener(cameraListener)
    }

    override fun onStop() {
        super.onStop()
        locationManager?.removeLocationListener(locationListener)
        binding.navigationLocationView.locationWindow.removeCameraListener(cameraListener)
    }

    override fun onDestroy() {
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
     * One UserLocationLayer per LocationWindow. It renders the current position automatically.
     */
    private fun initUserLocationLayer() {
        userLocationLayer = NavigineSdk.getInstance()
            .getUserLocationLayer(binding.navigationLocationView.locationWindow)
            .apply {
                setAccuracyColor(ACCURACY_COLOR)
                // setArrowBitmap(ImageProvider.fromResource(context, R.drawable.my_arrow)) to replace the heading arrow
                setVisible(true)
            }
    }

    private fun setViewListeners() {
        binding.followModeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            userLocationLayer?.setFollowMode(checkedId.toFollowMode())
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

        // A map gesture switches the follow mode to NONE. There is no callback for that,
        // so keep the UI in sync by listening to gesture-driven camera changes.
        cameraListener = object : CameraListener() {
            override fun onCameraPositionChanged(reason: CameraUpdateReason?, finished: Boolean) {
                if (reason != CameraUpdateReason.GESTURES) return
                if (userLocationLayer?.followMode() == UserLocationFollowMode.NONE) {
                    binding.followModeGroup.check(R.id.btn_follow_none)
                }
            }
        }
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

    private fun Int.toFollowMode(): UserLocationFollowMode = when (this) {
        R.id.btn_follow_position -> UserLocationFollowMode.POSITION
        R.id.btn_follow_heading -> UserLocationFollowMode.HEADING
        R.id.btn_follow_course -> UserLocationFollowMode.COURSE
        else -> UserLocationFollowMode.NONE
    }
}