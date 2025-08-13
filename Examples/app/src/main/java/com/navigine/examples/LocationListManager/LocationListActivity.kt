package com.navigine.examples.LocationListManager

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.navigine.examples.R
import com.navigine.examples.databinding.ActivityLocationListBinding
import com.navigine.idl.java.LocationInfo
import com.navigine.idl.java.LocationListListener
import com.navigine.idl.java.LocationListManager
import com.navigine.idl.java.NavigineSdk

/**
 * Example activity that demonstrates how to use the LocationListManager
 * to fetch a list of available locations from the Navigine platform.
 *
 * This activity:
 * - Initializes the Navigine SDK
 * - Sets server and user hash
 * - Registers a LocationListListener to receive location updates
 * - Displays the fetched location list in a TextView
 */
class LocationListActivity : AppCompatActivity() {

    private var _binding: ActivityLocationListBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")

    private var locationListManager: LocationListManager? = null
    private var locationListListener: LocationListListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSdk(this)
    }

    override fun onResume() {
        super.onResume()
        addListener()
    }

    override fun onStop() {
        super.onStop()
        removeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * Initializes the SDK and sets the server and user hash.
     * Also retrieves the LocationListManager instance.
     * Note: it is better to initialize the SDK in the Application class and keep managers as singletons.
     */
    private fun initSdk(context: Context) {

        val sdk = NavigineSdk.getInstance()
        sdk.setServer("https://ips.navigine.com")
        sdk.setUserHash("0000-0000-0000-0000")
        locationListManager = sdk?.locationListManager
    }

    /**
     * Subscribes to location list updates using LocationListListener.
     * Displays the list of location names in a TextView.
     */
    private fun addListener() {
        locationListListener = object : LocationListListener() {
            override fun onLocationListLoaded(hashMap: HashMap<Int, LocationInfo>) {
                val locations = buildString {
                    hashMap.values.forEach { loc ->
                        appendLine("Name: ${loc.name}, id: ${loc.id}")
                    }
                }
                binding.locationListTv.text = locations
            }

            override fun onLocationListFailed(error: Error) {
                binding.locationListTv.text = "Failed to load location list: ${error.message}"
                Log.e("LocationListActivity", "onLocationListFailed", error)
            }
        }
        locationListManager?.addLocationListListener(locationListListener)
    }

    /**
     * Unsubscribes the location list listener to avoid memory leaks.
     */
    private fun removeListener() {
        locationListManager?.removeLocationListListener(locationListListener)
    }

}