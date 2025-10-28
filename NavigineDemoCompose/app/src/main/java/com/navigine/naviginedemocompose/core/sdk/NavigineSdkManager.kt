package com.navigine.naviginedemocompose.core.sdk

import android.content.Context
import android.util.Log
import com.navigine.idl.java.AsyncRouteManager
import com.navigine.idl.java.BeaconProximityEstimator
import com.navigine.idl.java.LocationEditManager
import com.navigine.idl.java.LocationListManager
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.MeasurementManager
import com.navigine.idl.java.NavigationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.NotificationManager
import com.navigine.idl.java.ResourceManager
import com.navigine.idl.java.RouteManager
import com.navigine.idl.java.ZoneManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class NavigineSdkManager(
    private val appContext : Context
) {

    sealed interface SdkState {
        data object Idle : SdkState
        data class Configuring(val config: Config) : SdkState
        data class Ready(val config: Config) : SdkState
        data class Error(val error: Throwable?) : SdkState
    }

    private val _state = MutableStateFlow<SdkState>(SdkState.Idle)
    val state: StateFlow<SdkState> = _state.asStateFlow()

    val locationListManager: LocationListManager get() = requireManagers().locationList
    val locationManager: LocationManager get() = requireManagers().location
    val locationEditManager: LocationEditManager get() = requireManagers().locationEdit
    val resourceManager: ResourceManager get() = requireManagers().resource
    val navigationManager: NavigationManager get() = requireManagers().navigation
    val measurementManager: MeasurementManager get() = requireManagers().measurement
    val routeManager: RouteManager get() = requireManagers().route
    val asyncRouteManager: AsyncRouteManager get() = requireManagers().asyncRoute
    val notificationManager: NotificationManager get() = requireManagers().notification
    val zoneManager: ZoneManager get() = requireManagers().zone
    val beaconProximityEstimator: BeaconProximityEstimator get() = requireManagers().proximity

    /** Current configuration snapshot. */
    data class Config(
        val serverUrl: String,
        val userHash: String
    )

    /** Internal bag of managers bound to a particular configuration. */
    private data class Managers(
        val locationList: LocationListManager,
        val location: LocationManager,
        val locationEdit: LocationEditManager,
        val resource: ResourceManager,
        val navigation: NavigationManager,
        val measurement: MeasurementManager,
        val route: RouteManager,
        val asyncRoute: AsyncRouteManager,
        val notification: NotificationManager,
        val zone: ZoneManager,
        val proximity: BeaconProximityEstimator
    )

    @Volatile private var managers: Managers? = null
    @Volatile private var config: Config? = null

    private val tag = "NavigineSdkManager"

    /**
     * Configure or reconfigure the SDK. Safe to call multiple times.
     *
     * If called with the same (server, hash) as current, it is a no-op.
     *
     * @return true if configuration is valid and managers are ready.
     */
    @Synchronized
    fun configure(serverUrl: String, userHash: String): Boolean {
        if (userHash.isBlank()) {
            Log.w(tag, "configure(): userHash is blank – refusing to init")
            clearInternal()
            _state.value = SdkState.Error(IllegalArgumentException("Blank userHash"))
            return false
        }
        val newCfg = Config(serverUrl.trim(), userHash.trim())

        if (newCfg == config && managers != null) {
            _state.value = SdkState.Ready(newCfg)
            return true
        }

        _state.value = SdkState.Configuring(newCfg)
        return try {

            val sdk = NavigineSdk.getInstance()
            sdk.setServer(newCfg.serverUrl)
            sdk.setUserHash(newCfg.userHash)

            val locationList = sdk.locationListManager
            val location = sdk.locationManager
            val locationEdit = sdk.getLocationEditManager(location)
            val resource = sdk.getResourceManager(location)
            val navigation = sdk.getNavigationManager(location)
            val measurement = sdk.getMeasurementManager(location)
            val route = sdk.getRouteManager(location, navigation)
            val asyncRoute = sdk.getAsyncRouteManager(location, navigation)
            val notification = sdk.getNotificationManager(location)
            val zone = sdk.getZoneManager(navigation)
            val proximity = sdk.getBeaconProximityEstimator(location)

            managers = Managers(
                locationList = locationList,
                location = location,
                locationEdit = locationEdit,
                resource = resource,
                navigation = navigation,
                measurement = measurement,
                route = route,
                asyncRoute = asyncRoute,
                notification = notification,
                zone = zone,
                proximity = proximity
            )
            config = newCfg
            _state.value = SdkState.Ready(newCfg)
            true
        } catch (t: Throwable) {
            Log.e(tag, "Failed to configure SDK", t)
            clearInternal()
            _state.value = SdkState.Error(t)
            false
        }
    }

    /**
     * Convenience that only attempts to configure when both values are non-blank.
     * @return true if configured, false otherwise.
     */
    @Synchronized
    fun tryConfigure(serverUrl: String?, userHash: String?): Boolean {
        val s = serverUrl?.trim().orEmpty()
        val h = userHash?.trim().orEmpty()
        if (s.isBlank() || h.isBlank()) return false
        return configure(s, h)
    }

    /** Whether managers are currently available. */
    fun isReady(): Boolean =  state.value is SdkState.Ready

    /** Clears current managers/config (e.g., on logout). */
    @Synchronized
    fun clear() {
        clearInternal()
    }

    /** Returns the current device MAC address. */
    fun getDeviceId() = NavigineSdk.getDeviceId()

    /** Throws IllegalStateException if not configured. */
    private fun requireManagers(): Managers =
        managers ?: error("Navigine SDK is not configured. Call configure(server, userHash) first.")

    @Synchronized
    private fun clearInternal() {
        managers = null
        config = null
        _state.value = SdkState.Idle
    }

}