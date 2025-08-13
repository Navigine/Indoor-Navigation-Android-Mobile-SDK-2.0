# LocationWindow Pickers Example

This example demonstrates how to use Navigine SDK’s pick and input listeners to interact with an indoor map. You will learn how to detect taps, double-taps, long-taps, and pick map objects or map features at specific screen coordinates.

---

## ✅ What does this example show?

- **SDK Initialization**: Configure the Navigine SDK server URL and user hash.
- **Location Loading**: Load a location and its sublocation via `LocationManager` and `LocationListener`.
- **Input Handling**: Detect user interactions on the map view:
    - Single tap (`onViewTap`)
    - Double tap (`onViewDoubleTap`)
    - Long tap (`onViewLongTap`)
- **Pick Handling**:
    - Pick map objects (`onMapObjectPickComplete`) with `PickListener`.
    - Pick map features (`onMapFeaturePickComplete`) with `PickListener`.

---

## 🛠 Prerequisites

1. Make sure the SDK `.aar` file is located in the `libs/` directory.  
   Then add the following in your `build.gradle.kts`:

    ```kotlin
    dependencies {
        implementation(files("../libs/navigine.aar"))
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
        loadSublocation(location ?: return)
    }
    override fun onLocationFailed(locationId: Int, error: Error?) {
        Log.e("Pickers", "Failed to load location: $error")
    }
}
locationManager?.addLocationListener(locationListener)
```

Helper to switch to the first sublocation and set zoom bounds:

```kotlin
private fun loadSublocation(location: Location) {
    val sub = location.sublocations.firstOrNull() ?: return
    navigationLocationView.post {
        navigationLocationView.locationWindow?.apply {
            setSublocationId(sub.id)
            // Calculate zoom constraints then fit
            configureZoom(sub.width)
            setupZoomCameraDefault()
        }
    }
}
```

### 3. Register Input and Pick Listeners

Set up `InputListener` to handle tap gestures and `PickListener` to handle object/feature picks:

```kotlin
inputListener = object : InputListener() {
    override fun onViewTap(point: PointF?)        { showToast("Tap at $point") }
    override fun onViewDoubleTap(point: PointF?)  { showToast("Double tap at $point") }
    override fun onViewLongTap(point: PointF?)    { showToast("Long tap at $point") }
}

pickListener = object : PickListener() {
    override fun onMapObjectPickComplete(
        result: MapObjectPickResult?, screenPos: PointF?
    ) {
        if (result != null) {
            showToast("Picked object type: ${result.mapObject.type.name}")
        }
    }
    override fun onMapFeaturePickComplete(
        props: HashMap<String, String>?, screenPos: PointF?
    ) {
        val label = props?.values?.firstOrNull()
        showToast("Picked feature: $label")
    }
}

// Add listeners when the view is ready
locationManager?.addLocationListener(locationListener)
navigationLocationView.locationWindow?.apply {
    addInputListener(inputListener)
    addPickListener(pickListener)
}
```

Remember to remove listeners in `onPause()` or `onDestroy()`:

```kotlin
locationManager?.removeLocationListener(locationListener)
navigationLocationView.locationWindow?.apply {
    removeInputListener(inputListener)
    removePickListener(pickListener)
}
```

### 4. Handle User Interactions

For example, you can use a helper method to display a `Toast`:

```kotlin
private fun showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
```

---

## 📂 Files Involved

- `LocationWindowPickersActivity.kt` – main Activity implementing listeners
- `activity_location_window_pickers.xml` – layout with `NavigationLocationView`

---

## ⚠️ Notes

- Ensure you request runtime permissions before loading the location.
- Always remove listeners to prevent memory leaks.
- Customize the `Toast` or replace it with a UI dialog for production.
- You can combine pick results with custom UI overlays to highlight selected objects.
- For production, consider initializing the SDK in your `Application` class and managing `LocationManager` as a singleton.

