package com.navigine.examples.AsyncRouteManager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.navigine.examples.databinding.ActivityAsyncRouteBinding
import com.navigine.idl.java.AsyncRouteListener
import com.navigine.idl.java.AsyncRouteManager
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.NavigationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.Point
import com.navigine.idl.java.Position
import com.navigine.idl.java.PositionListener
import com.navigine.idl.java.RouteOptions
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.RouteSession
import com.navigine.sdk.Navigine

/**
 * Example Activity demonstrating how to use AsyncRouteManager
 * to calculate and track routes asynchronously.
 *
 * Steps:
 * 1. Initialize Navigine SDK (in Application or here).
 * 2. Obtain NavigationManager & AsyncRouteManager instances.
 * 3. Register a PositionListener to capture start point.
 * 4. Create a RouteSession & add AsyncRouteListener.
 * 5. Handle onRouteChanged and onRouteAdvanced callbacks.
 * 6. Unregister listeners and cancel session in onStop to avoid leaks.
 */
class AsyncRouteActivity : AppCompatActivity() {

    private var _binding: ActivityAsyncRouteBinding? = null
    private val binding get() = _binding!!

    private var navigationManager: NavigationManager? = null
    private var asyncRouteManager: AsyncRouteManager? = null
    private var routeSession: RouteSession? = null

    private var positionListener: PositionListener? = null
    private var asyncRouteListener: AsyncRouteListener? = null

    // TODO: Replace with your valid target point LocationPoint
    private var targetPoint: LocationPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAsyncRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        addPositionListener()
    }

    override fun onStart() {
        super.onStart()
        startRoute()
    }

    override fun onStop() {
        super.onStop()
        removePositionListener()
        removeAsyncRouteListener()
    }

    private fun initSdk() {
        Navigine.initialize(applicationContext)
        val sdk = NavigineSdk.getInstance()
        // your server URL
        sdk.setServer("https://ips.navigine.com")
        // your user hash
        sdk.setUserHash("0000-0000-0000-0000")
        // your valid target point. Usually get from pickListener in LocationView.
        targetPoint = LocationPoint(Point(1f,1f), 0,0)

        val locationManager = sdk.locationManager
        navigationManager = sdk.getNavigationManager(locationManager)
        asyncRouteManager = sdk.getAsyncRouteManager(locationManager, navigationManager)
    }

    private fun initListeners() {
        positionListener = object : PositionListener() {
            override fun onPositionUpdated(position: Position?) {
                /** Handle position updates here. For example: display in on LocationView. */
                binding.asyncRoutePositionTextView.text = position?.locationPoint.toString()
            }
            override fun onPositionError(error: Error) {
                Log.e("AsyncRouteActivity", "Position error", error)
            }
        }

        asyncRouteListener = object : AsyncRouteListener() {
            override fun onRouteChanged(path: RoutePath?) {
                // Called when new route is built or rebuilt after a failure.
                path?.let {
                    binding.asyncRoutePathTextView.text = it.points.toString()
                }
            }
            override fun onRouteAdvanced(distance: Float, currentPoint: LocationPoint?) {
                // Called when user has progressed along the route.
                Log.d("AsyncRouteActivity", "Advanced $distance meters; next point: $currentPoint")
            }
        }
    }

    /** Subscribes to position updates. */
    private fun addPositionListener() {
        positionListener?.let {
            navigationManager?.addPositionListener(it)
        }
    }

    /** Unsubscribes from position updates. */
    private fun removePositionListener() {
        positionListener?.let {
            navigationManager?.removePositionListener(it)
        }
    }

    /** Creates and starts the async route session. */
    private fun startRoute() {
        if (routeSession != null) return
        val options = RouteOptions(0.0, 3.0, 2.0)
        targetPoint?.let { point ->
            routeSession = asyncRouteManager?.createRouteSession(point, options)
                ?.apply {
                    addRouteListener(asyncRouteListener)
                }
        } ?: Log.e("AsyncRouteActivity", "Start point is null; cannot create route session")
    }

    /**
     * Cancels the current route session and removes the listener to avoid memory leaks.
     */
    private fun removeAsyncRouteListener() {
        routeSession?.let { session ->
            asyncRouteManager?.cancelRouteSession(session)
            asyncRouteListener?.let { listener ->
                session.removeRouteListener(listener)
            }
            routeSession = null
        }
    }
}