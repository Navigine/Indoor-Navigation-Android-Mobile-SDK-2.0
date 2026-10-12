# LocationWindow Adding Objects Example

This example shows how to add and remove **map objects** on a `NavigationLocationView` / `locationWindow` using the Navigine SDK. It ensures that **only one instance** of each object type (Circle, Icon, Polygon, Polyline) exists at any time.

---

## ✅ What does this example show?

- **SDK Initialization** and loading a location/sublocation.
- **Adding map objects**: `addCircleMapObject`, `addIconMapObject`, `addPolygonMapObject`, `addPolylineMapObject`.
- **Positioning objects in WGS84** with `GlobalPoint` and a sublocation id.
- **Removing map objects** via corresponding `remove*` methods (or `removeAllMapObjects`).
- **State handling** to avoid duplicates using a small holder (`MapObjectsHolder`).
- **Two buttons**: `Add` adds only missing objects; `Remove` deletes currently added ones.

---

## 🛠 Prerequisites

1. **Dependency** — the SDK `.aar` is taken from the `libs` folder in the repository root:
   ```kotlin
   // settings.gradle.kts
   dependencyResolutionManagement {
       repositories {
           flatDir { dirs("../libs") }
       }
   }

   // app/build.gradle.kts
   dependencies {
       implementation(":libnavigine@aar")
   }
   ```
2. **Permissions** (request at runtime on Android 6+ where required):
   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
   ```
3. **Layout**: `activity_location_view_adding_objects.xml`
   - Must contain a `NavigationLocationView`.
   - Two buttons with IDs `btn_add` and `btn_remove` (or `btnAdd`/`btnRemove` if using ViewBinding).

---

## 🔧 Step-by-Step Guide

### 1) Initialize the SDK and load a location

```kotlin
private fun initSdk() {
    val sdk = NavigineSdk.getInstance().apply {
        setServer("https://ips.navigine.com")
        setUserHash("YOUR_USER_HASH")
    }
    locationManager = sdk.locationManager.apply {
        locationId = YOUR_LOCATION_ID
    }
}
```

Register a `LocationListener` and switch to a sublocation (fit zoom to view). Keep the listener in a field — see [Notes](#-notes):

```kotlin
locationListener = object : LocationListener() {
    override fun onLocationLoaded(location: Location?) {
        loadSublocation(location ?: return)
    }
    override fun onLocationUploaded(locationId: Int) {}
    override fun onLocationFailed(locationId: Int, error: Error?) { /* handle */ }
}
locationManager?.addLocationListener(locationListener)

private fun loadSublocation(location: Location) {
    val sub = location.sublocations.firstOrNull() ?: return
    binding.navigationLocationView.post {
        binding.navigationLocationView.locationWindow?.apply {
            setSublocationId(sub.id)
            val pixelWidth = binding.navigationLocationView.width / resources.displayMetrics.density
            maxZoomFactor = (pixelWidth * 16f / sub.width).coerceIn(1f, 600f)
            minZoomFactor = (pixelWidth / 16f / sub.width).coerceIn(1f, 600f)
            zoomFactor    = (pixelWidth / sub.width).coerceIn(minZoomFactor, maxZoomFactor)
        }
    }
}
```

### 2) Keep references to prevent duplicates

```kotlin
private data class MapObjectsHolder(
    var circle: CircleMapObject? = null,
    var icon: IconMapObject? = null,
    var polygon: PolygonMapObject? = null,
    var polyline: PolylineMapObject? = null,
)

private val mapObjects = MapObjectsHolder()
```

### 3) Wire up the buttons

```kotlin
binding.btnAdd.setOnClickListener { addObjects() }
binding.btnRemove.setOnClickListener { removeObjects() }
```

### 4) Define coordinates

Map objects are positioned in **WGS84** (`GlobalPoint(latitude, longitude)`) plus the id of the sublocation they belong to (`null` means outdoors). Replace the values below with coordinates inside your location:

```kotlin
private const val SUBLOCATION_ID = 1

private val POINT_1 = GlobalPoint(55.75100, 37.61700)
private val POINT_2 = GlobalPoint(55.75100, 37.61705)
private val POINT_3 = GlobalPoint(55.75103, 37.61705)
private val POINT_4 = GlobalPoint(55.75103, 37.61700)

private val OBJECT_COLOR = Color.rgb(140, 93, 76)
private val POLYGON_COLOR = Color.argb(128, 140, 93, 76)
private const val CIRCLE_RADIUS = 0.5f
private const val POLYLINE_WIDTH_PX = 4f
```

> If you have coordinates in local sublocation meters (`LocationPoint`), convert them with `sublocation.localToGlobal(locationPoint)`.

### 5) Add one of each object (only if missing)

```kotlin
private fun addObjects() {
    val lw = binding.navigationLocationView.locationWindow ?: return

    if (mapObjects.circle == null) {
        mapObjects.circle = lw.addCircleMapObject().also { circle ->
            circle.setPosition(POINT_4, SUBLOCATION_ID)
            circle.setRadius(CIRCLE_RADIUS)
            circle.setColor(OBJECT_COLOR)
        }
    }

    if (mapObjects.icon == null) {
        mapObjects.icon = lw.addIconMapObject().also { icon ->
            icon.setPosition(POINT_2, SUBLOCATION_ID)
            // icon.setBitmap(...)  set your own bitmap here
        }
    }

    if (mapObjects.polygon == null) {
        mapObjects.polygon = lw.addPolygonMapObject().also { polygon ->
            polygon.setPolygon(
                LocationPolygon(arrayListOf(POINT_1, POINT_2, POINT_3, POINT_4), SUBLOCATION_ID, arrayListOf())
            )
            polygon.setColor(POLYGON_COLOR)
        }
    }

    if (mapObjects.polyline == null) {
        mapObjects.polyline = lw.addPolylineMapObject().also { polyline ->
            polyline.setPolyLine(LocationPolyline(arrayListOf(POINT_1, POINT_3), SUBLOCATION_ID))
            polyline.setWidth(POLYLINE_WIDTH_PX)
            polyline.setColor(OBJECT_COLOR)
        }
    }
}
```

Colors are a single ARGB `Int` (e.g. `Color.argb(...)` or `0xFF8C5D4C.toInt()`). The last argument of `LocationPolygon` is a list of holes (inner rings); pass an empty list for a solid polygon.

### 6) Remove all added objects and clear references

```kotlin
private fun removeObjects() {
    val lw = binding.navigationLocationView.locationWindow ?: return

    mapObjects.circle?.let   { if (lw.removeCircleMapObject(it))   mapObjects.circle   = null }
    mapObjects.icon?.let     { if (lw.removeIconMapObject(it))     mapObjects.icon     = null }
    mapObjects.polygon?.let  { if (lw.removePolygonMapObject(it))  mapObjects.polygon  = null }
    mapObjects.polyline?.let { if (lw.removePolylineMapObject(it)) mapObjects.polyline = null }

    // Alternatively remove everything at once:
    // lw.removeAllMapObjects(); mapObjects = MapObjectsHolder()
}
```

---

## 📂 Files Involved

- `LocationViewAddingObjectsActivity.kt` — Activity with add/remove logic and holder for objects.
- `activity_location_view_adding_objects.xml` — layout with `NavigationLocationView` and `Add`/`Remove` buttons.
- `AndroidManifest.xml` — permissions and activity declaration.

---

## ⚠️ Notes

- **Migrating from SDK < 2.27:** map objects use WGS84 `GlobalPoint` instead of `LocationPoint`, `setColor` takes a single ARGB `Int`, and `FlatIconMapObject` was removed — use `IconMapObject` instead.
- `SUBLOCATION_ID` must match the sublocation shown on the map, otherwise the objects are added to another floor and won't be visible.
- After changing **sublocation** or **reloading** a location, previously created objects may become irrelevant; remove them if needed.
- Keep references to objects you add; without them, you can only remove everything via `removeAllMapObjects()`.
- **Keep references to SDK listeners.** The SDK does not keep listener objects alive: a listener passed without being stored in a field can be garbage-collected, and its callbacks silently stop arriving. Store listeners in fields and add/remove them in the appropriate lifecycle methods.
- For production, consider initializing the SDK in your `Application` class and managing managers as a singleton.