# LocationWindow Pickers Example

This example demonstrates how to use Navigine SDK’s pick and input listeners to interact with an indoor map. You will learn how to detect taps, double-taps, long-taps, pick map objects or map features at the tapped position, and convert a screen position to WGS84 coordinates.

---

## ✅ What does this example show?

- **SDK Initialization**: Configure the Navigine SDK server URL and user hash.
- **Location Loading**: Load a location and its sublocation via `LocationManager` and `LocationListener`.
- **Input Handling**: Detect user interactions on the map view:
    - Single tap (`onViewTap`) — triggers picking at the tapped position
    - Double tap (`onViewDoubleTap`)
    - Long tap (`onViewLongTap`) — converts the position to WGS84 with `screenPositionToGlobal`
- **Pick Handling**:
    - Request picking with `pickMapObjectAt` / `pickMapFeatureAt`.
    - Receive map objects (`onMapObjectPickComplete`) and map features (`onMapFeaturePickComplete`) with `PickListener`.

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

2. **Android Permissions** in `AndroidManifest.xml` (and request at runtime on Android 6+):
   ```xml
   <!-- Location for Wi‑Fi, BLE scanning, and map interaction -->
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

   <!-- Internet & network state for SDK communication -->
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   ```
3. **Layout Resources**:
    - `activity_location_window_pickers.xml` containing a `NavigationLocationView`.
    - Make sure the view fills the screen and is ready to receive touch events.

---

## 🔧 Step-by-Step Guide

### 1. Initialize the SDK and LocationManager

In `onCreate()`:

```kotlin
private fun initSdk() {
    val sdk = NavigineSdk.getInstance().apply {
        setServer("https://ips.navigine.com")   // Navigine backend URL
        setUserHash("YOUR_USER_HASH")           // User authorization hash
    }
    locationManager = sdk.locationManager.apply {
        locationId = YOUR_LOCATION_ID            // ID of the location to load (get from LocationListManager)
    }
}
```
⚠️ In production, initialize the SDK in your Application class and reuse managers from a singleton like NavigineSdkManager.

### 2. Load Location and Sublocation

Register a `LocationListener` to receive the loaded location:

```kotlin
locationListener = object : LocationListener() {
    override fun onLocationLoaded(location: Location?) {
        location?.let { loadSublocation(it) }
    }
    override fun onLocationUploaded(locationId: Int) {}
    override fun onLocationFailed(locationId: Int, error: Error?) {
        Log.e("Pickers", "Failed to load location: $error")
    }
}
```

Helper to switch to the first sublocation and set zoom bounds:

```kotlin
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

### 3. Handle Input and Picking

Picking is **not automatic**: a tap only reaches the `InputListener`. To find out what is under the finger, request picking explicitly at the tapped position — results are delivered to the `PickListener`.

```kotlin
inputListener = object : InputListener() {
    override fun onViewTap(point: PointF?) {
        point ?: return
        binding.navigationLocationView.locationWindow?.apply {
            pickMapObjectAt(point)
            pickMapFeatureAt(point)
        }
    }

    override fun onViewDoubleTap(point: PointF?) {
        showToast("Double tap")
    }

    override fun onViewLongTap(point: PointF?) {
        point ?: return
        val global = binding.navigationLocationView.locationWindow
            ?.screenPositionToGlobal(point)
            ?: return
        showToast(String.format(Locale.US, "Long tap at %.6f, %.6f", global.latitude, global.longitude))
    }
}

pickListener = object : PickListener() {
    override fun onMapObjectPickComplete(result: MapObjectPickResult?, screenPosition: PointF?) {
        val type = result?.mapObject?.type ?: return
        showToast("Map object picked: ${type.name}")
    }

    override fun onMapFeaturePickComplete(attributes: HashMap<String, String>?, point: PointF?) {
        val name = attributes?.get("name") ?: return
        showToast("Map feature picked: $name")
    }
}
```

`screenPositionToGlobal` returns a WGS84 `GlobalPoint`. If you need local sublocation meters instead, convert it with `sublocation.globalToLocal(globalPoint)`.

### 4. Register and Remove Listeners

Add listeners in `onResume()` and remove them in `onPause()`:

```kotlin
private fun addListeners() {
    locationManager?.addLocationListener(locationListener)
    binding.navigationLocationView.locationWindow.apply {
        addPickListener(pickListener)
        addInputListener(inputListener)
    }
}

private fun removeListeners() {
    locationManager?.removeLocationListener(locationListener)
    binding.navigationLocationView.locationWindow.apply {
        removePickListener(pickListener)
        removeInputListener(inputListener)
    }
}
```

---

## 📂 Files Involved

- `LocationWindowPickersActivity.kt` – main Activity implementing listeners
- `activity_location_window_pickers.xml` – layout with `NavigationLocationView`

---

## ⚠️ Notes

- **Migrating from SDK < 2.27:** `screenPositionToMeters` (local meters) was replaced by `screenPositionToGlobal` (WGS84 `GlobalPoint`).
- **Keep references to SDK listeners.** The SDK does not keep listener objects alive: a listener passed without being stored in a field can be garbage-collected, and its callbacks silently stop arriving. Store listeners in fields, as in this example.
- `onMapObjectPickComplete` is called with `null` when there is no interactive map object at the tapped position — always check the result.
- Feature attributes depend on your map data; venues usually carry a `name` attribute.
- Ensure you request runtime permissions before loading the location.
- Customize the `Toast` or replace it with a UI dialog for production.
- You can combine pick results with custom UI overlays to highlight selected objects (`selectMapFeature` / `deselectMapFeature`).
- For production, consider initializing the SDK in your `Application` class and managing `LocationManager` as a singleton.