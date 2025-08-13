# LocationWindow Adding Objects Example

This example shows how to add and remove **map objects** on a `NavigationLocationView` / `locationWindow` using the Navigine SDK. It ensures that **only one instance** of each object type (Circle, Icon, FlatIcon, Polygon, Polyline) exists at any time.

---

## ✅ What does this example show?

- **SDK Initialization** and loading a location/sublocation.
- **Adding map objects**: `addCircleMapObject`, `addIconMapObject`, `addFlatIconMapObject`, `addPolygonMapObject`, `addPolylineMapObject`.
- **Removing map objects** via corresponding `remove*` methods (or `removeAllMapObjects`).
- **State handling** to avoid duplicates using a small holder (`MapObjectsHolder`).
- **Two buttons**: `Add` adds only missing objects; `Remove` deletes currently added ones.

---

## 🛠 Prerequisites

1. **Dependency** (example):
   ```kotlin
    dependencies {
        implementation(files("../libs/navigine.aar"))
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

Register a `LocationListener` and switch to a sublocation (fit zoom to view):

```kotlin
locationListener = object : LocationListener() {
    override fun onLocationLoaded(location: Location?) {
        loadSublocation(location ?: return)
    }
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
data class MapObjectsHolder(
    var circle: CircleMapObject? = null,
    var icon: IconMapObject? = null,
    var flatIcon: FlatIconMapObject? = null,
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

### 4) Add one of each object (only if missing)

```kotlin
private fun addObjects() {
    val lw = binding.navigationLocationView.locationWindow ?: return

    val p1 = Point(1f, 1f)
    val p2 = Point(3f, 1f)
    val p3 = Point(3f, 3f)
    val p4 = Point(1f, 3f)

    if (mapObjects.circle == null) {
        mapObjects.circle = lw.addCircleMapObject().also { circle ->
            // TODO: circle.setCenter(p1); circle.setRadius(0.5f)
            // TODO: circle.setFillColor(argb); circle.setStrokeColor(argb)
        }
    }
    if (mapObjects.icon == null) {
        mapObjects.icon = lw.addIconMapObject().also { icon ->
            // TODO: icon.setPosition(p2); icon.setTitle("Icon")
            // TODO: icon.setIcon(bitmap or drawable)
        }
    }
    if (mapObjects.flatIcon == null) {
        mapObjects.flatIcon = lw.addFlatIconMapObject().also { flatIcon ->
            // TODO: flatIcon.setPosition(p3); flatIcon.setAngle(0f); flatIcon.setScale(1f)
        }
    }
    if (mapObjects.polygon == null) {
        mapObjects.polygon = lw.addPolygonMapObject().also { polygon ->
            // TODO: polygon.setVertices(listOf(p1, p2, p3, p4))
            // TODO: polygon.setFillColor(argb); polygon.setStrokeColor(argb)
        }
    }
    if (mapObjects.polyline == null) {
        mapObjects.polyline = lw.addPolylineMapObject().also { polyline ->
            // TODO: polyline.setPoints(listOf(p1, p2, p3, p4, p1))
            // TODO: polyline.setWidth(px)
        }
    }
}
```

### 5) Remove all added objects and clear references

```kotlin
private fun removeObjects() {
    val lw = binding.navigationLocationView.locationWindow ?: return

    mapObjects.circle?.let   { if (lw.removeCircleMapObject(it))    mapObjects.circle   = null }
    mapObjects.icon?.let     { if (lw.removeIconMapObject(it))      mapObjects.icon     = null }
    mapObjects.flatIcon?.let { if (lw.removeFlatIconMapObject(it))  mapObjects.flatIcon = null }
    mapObjects.polygon?.let  { if (lw.removePolygonMapObject(it))   mapObjects.polygon  = null }
    mapObjects.polyline?.let { if (lw.removePolylineMapObject(it))  mapObjects.polyline = null }

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

- After changing **sublocation** or **reloading** a location, previously created objects may become irrelevant; remove them if needed.
- Keep references to objects you add; without them, you can only remove everything via `removeAllMapObjects()`.
- Always add/remove listeners in the appropriate lifecycle methods to avoid leaks.
- For production, consider initializing the SDK in your `Application` class and managing managers as a singleton.


