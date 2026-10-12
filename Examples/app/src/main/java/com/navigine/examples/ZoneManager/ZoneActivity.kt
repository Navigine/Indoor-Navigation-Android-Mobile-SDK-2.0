package com.navigine.examples.ZoneManager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.navigine.examples.databinding.ActivityZoneBinding
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.NavigationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.ZoneEvent
import com.navigine.idl.java.ZoneEventType
import com.navigine.idl.java.ZoneListener
import com.navigine.idl.java.ZoneManager
import com.navigine.sdk.Navigine
import java.lang.Error
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Example of detecting when the user enters or leaves zones with ZoneManager.
 *
 * Zones are areas drawn in the Navigine CMS. ZoneManager uses the user's position
 * from NavigationManager and reports ENTER / EXIT events.
 */
class ZoneActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ZoneActivity"
        private const val LOCATION_ID = 2025  // Assign the locationId you want to load (obtained from LocationList sample)
        private const val MAX_LOG_LINES = 50
    }

    private var _binding: ActivityZoneBinding? = null
    private val binding get() = _binding!!

    // Keep SDK objects in fields: the SDK holds them by weak references
    private var locationManager: LocationManager? = null
    private var navigationManager: NavigationManager? = null
    private var zoneManager: ZoneManager? = null

    private var locationListener: LocationListener? = null
    private var zoneListener: ZoneListener? = null

    // Zones the user is currently inside: id -> display name
    private val currentZones = linkedMapOf<Int, String>()
    private val eventLog = ArrayDeque<String>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityZoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        initListeners()
        renderCurrentZones()
    }

    override fun onStart() {
        super.onStart()
        locationManager?.addLocationListener(locationListener)
        zoneManager?.addZoneListener(zoneListener)
    }

    override fun onStop() {
        super.onStop()
        locationManager?.removeLocationListener(locationListener)
        zoneManager?.removeZoneListener(zoneListener)
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

        // Zone events are based on the user's position from NavigationManager
        navigationManager = sdk.getNavigationManager(locationManager)
        zoneManager = sdk.getZoneManager(navigationManager)
    }

    private fun initListeners() {
        locationListener = object : LocationListener() {
            override fun onLocationLoaded(location: Location?) {
                appendLog("Location loaded: ${location?.name}")
            }

            override fun onLocationUploaded(locationId: Int) {}

            override fun onLocationFailed(locationId: Int, error: Error?) {
                Log.e(TAG, "onLocationFailed: $error")
                appendLog("Location failed: $error")
            }
        }

        // Called on the UI thread
        zoneListener = object : ZoneListener() {
            override fun onZoneEvent(zoneEvent: ZoneEvent?) {
                zoneEvent ?: return
                handleZoneEvent(zoneEvent)
            }
        }
    }

    /**
     * EXIT also comes when the user changes floor or the zone is removed,
     * so the set of current zones always stays consistent.
     */
    private fun handleZoneEvent(event: ZoneEvent) {
        val name = event.displayName()
        when (event.type) {
            ZoneEventType.ENTER -> currentZones[event.id] = name
            ZoneEventType.EXIT -> currentZones.remove(event.id)
            else -> return
        }
        appendLog("${event.type}: $name (sublocation ${event.sublocationId})")
        renderCurrentZones()
    }

    private fun ZoneEvent.displayName(): String =
        name?.takeIf { it.isNotBlank() }
            ?: alias?.takeIf { it.isNotBlank() }
            ?: "#$id"

    private fun renderCurrentZones() {
        binding.currentZonesText.text = if (currentZones.isEmpty()) {
            "Outside of all zones"
        } else {
            "Inside: ${currentZones.values.joinToString()}"
        }
    }

    private fun appendLog(message: String) {
        eventLog.addFirst("${timeFormat.format(Date())}  $message")
        while (eventLog.size > MAX_LOG_LINES) eventLog.removeLast()
        binding.zoneEventsText.text = eventLog.joinToString("\n")
    }
}