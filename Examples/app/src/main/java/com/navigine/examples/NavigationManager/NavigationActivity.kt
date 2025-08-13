package com.navigine.examples.NavigationManager

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.navigine.examples.R
import com.navigine.examples.databinding.ActivityNavigationBinding
import com.navigine.idl.java.NavigationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.Position
import com.navigine.idl.java.PositionListener
import com.navigine.sdk.Navigine
/**
 * Example activity demonstrating how to receive position updates using NavigationManager.
 *
 */
class NavigationActivity : AppCompatActivity() {
    private var _binding: ActivityNavigationBinding? = null
    private val binding get() = _binding!!

    private var navigationManager: NavigationManager? = null
    private var positionListener: PositionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSdk()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        addPositionListener()
    }

    override fun onPause() {
        super.onPause()
        removePositionListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * Initializes Navigine SDK and NavigationManager.
     */
    private fun initSdk() {
        Navigine.initialize(applicationContext)
        val sdk = NavigineSdk.getInstance()
        sdk.setServer("https://ips.navigine.com")
        sdk.setUserHash("0000-0000-0000-0000")
        val locationManager = sdk.locationManager
        navigationManager = sdk.getNavigationManager(locationManager)
    }

    /**
     * Sets up the PositionListener for receiving location updates.
     * ⚠️ **Important**
     * - Make sure you've already requested **ACCESS_FINE_LOCATION** (and on Android 12+ **BLUETOOTH_SCAN**) at runtime;
     *   without them, this listener will never fire.
     * - The Navigine SDK uses **Bluetooth LE beacons** for indoor positioning.
     *   If no beacons are in range, you will not receive any `onPositionUpdated()` callbacks.
     */
    private fun initListeners() {
        positionListener = object : PositionListener() {
            override fun onPositionUpdated(position: Position?) {
                position?.let {
                    val msg = buildString {
                        appendLine("Position updated:")
                        appendLine("• point: ${it.point}")
                        appendLine("• accuracy: ${it.accuracy}")
                        appendLine("• heading: ${it.heading}")
                        appendLine("• headingAccuracy: ${it.headingAccuracy}")
                        appendLine("• locationPoint: ${it.locationPoint}")
                        appendLine("• locationHeading: ${it.locationHeading}")
                    }
                    binding.positionTextView.text = msg
                }
            }

            override fun onPositionError(error: Error) {
                val msg = "Position error: ${error.message}"
                binding.positionTextView.text = msg
                Log.e("NavigationActivity", msg)
            }
        }
    }

    /**
     * Registers the PositionListener.
     */
    private fun addPositionListener() {
        positionListener?.let {
            navigationManager?.addPositionListener(it)
        }
    }

    /**
     * Unregisters the PositionListener to prevent memory leaks.
     */
    private fun removePositionListener() {
        positionListener?.let {
            navigationManager?.removePositionListener(it)
        }
    }
}