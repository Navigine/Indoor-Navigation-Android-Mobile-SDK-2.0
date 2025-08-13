package com.navigine.examples.LocationWindow.AddingObjects

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.navigine.examples.R
import com.navigine.examples.databinding.ActivityLocationViewAddingObjectsBinding
import com.navigine.idl.java.CameraListener
import com.navigine.idl.java.CameraUpdateReason
import com.navigine.idl.java.CircleMapObject
import com.navigine.idl.java.FlatIconMapObject
import com.navigine.idl.java.IconMapObject
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolygon
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.Point
import com.navigine.idl.java.PolygonMapObject
import com.navigine.idl.java.PolylineMapObject
import java.lang.Error

/**
 * Example of adding/removing map objects (Circle, Icon, FlatIcon, Polygon, Polyline)
 * to the Navigine LocationWindow. The sample ensures there is **only one** instance
 * of each object type on the map at any time.
 *
 * Buttons behavior:
 * - **Add**: adds all objects that are not yet present.
 * - **Remove**: removes all currently added objects.
 */
class LocationViewAddingObjectsActivity : AppCompatActivity() {

    private var _binding: ActivityLocationViewAddingObjectsBinding? = null
    private val binding get() = _binding!!

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    companion object{
        private const val LOCATION_ID = 2025  // Assign the locationId you want to load (obtained from LocationList sample)
        private const val SUBLOCATION_ID = 1  // Assign the sublocationId you want to load (obtained from LocationsListener)
    }

    /**
     * Holder for all map-object references so we can avoid duplicates and remove them later.
     * Consider to store your map objects somewhere, for example in a ViewModel.
     */
    private data class MapObjectsHolder(
        var circle: CircleMapObject? = null,
        var icon: IconMapObject? = null,
        var flatIcon: FlatIconMapObject? = null,
        var polygon: PolygonMapObject? = null,
        var polyline: PolylineMapObject? = null,
    )

    private val mapObjects = MapObjectsHolder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityLocationViewAddingObjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSdk()
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

    private fun initViewsListeners() {
        binding.btnAdd.setOnClickListener { addObjects() }
        binding.btnRemove.setOnClickListener { removeObjects() }
    }

    /**
     * Adds one instance of each map object if it's not already present.
     * Geometry/appearance values below are placeholders — adjust to your needs.
     * Most of them have default styles, but you can change them to your liking by using object.set* methods.
     */
    private fun addObjects(){
        val lw = binding.navigationLocationView.locationWindow ?: return

        // Usually you get coordinates from AsyncRouteListener or PositionListener
        val p1 = Point(1f, 1f)
        val p2 = Point(3f, 1f)
        val p3 = Point(3f, 3f)
        val p4 = Point(1f, 3f)

        if (mapObjects.circle == null) {
            mapObjects.circle = lw.addCircleMapObject().also { circle ->
                circle.setPosition(LocationPoint(p1, LOCATION_ID, SUBLOCATION_ID))
                circle.setRadius(3f)
                circle.setColor(
                    140f/255f,
                    93f/255f,
                    76f/255f,
                    1f
                )
            }
        }

        if (mapObjects.icon == null) {
            mapObjects.icon = lw.addIconMapObject().also { icon ->
                icon.setPosition(LocationPoint(p2, LOCATION_ID, SUBLOCATION_ID))
                //icon.setBitmap()  set your own bitmap here
            }
        }

        if (mapObjects.flatIcon == null) {
            mapObjects.flatIcon = lw.addFlatIconMapObject().also { flatIcon ->
                flatIcon.setPosition(LocationPoint(p3, LOCATION_ID, SUBLOCATION_ID))
                //flatIcon.setBitmap()  set your own bitmap here
            }
        }

        if (mapObjects.polygon == null) {
            mapObjects.polygon = lw.addPolygonMapObject().also { polygon ->
                //polygon.setPolygon(LocationPolygon()) set your own polygon here
                polygon.setColor(
                    140f/255f,
                    93f/255f,
                    76f/255f,
                    1f
                )
            }
        }

        if (mapObjects.polyline == null) {
            mapObjects.polyline = lw.addPolylineMapObject().also { polyline ->
                // polyline.setWidth(px)
                //polyline.setPolyLine(LocationPolyline()) set your own polyline here
            }
        }
    }

    private fun removeObjects() {
        val lw = binding.navigationLocationView.locationWindow ?: return

        mapObjects.circle?.let { if (lw.removeCircleMapObject(it)) mapObjects.circle = null }
        mapObjects.icon?.let { if (lw.removeIconMapObject(it)) mapObjects.icon = null }
        mapObjects.flatIcon?.let { if (lw.removeFlatIconMapObject(it)) mapObjects.flatIcon = null }
        mapObjects.polygon?.let { if (lw.removePolygonMapObject(it)) mapObjects.polygon = null }
        mapObjects.polyline?.let { if (lw.removePolylineMapObject(it)) mapObjects.polyline = null }
    }


    /**
     * Load the first sublocation of the given Location, apply zoom constraints
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
        }
    }

    /**
     * Subscribe to location updates.
     */
    private fun addListeners() {
        locationManager?.addLocationListener(locationListener)
    }

    private fun removeListeners() {
        locationManager?.removeLocationListener(locationListener)
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