# LocationViewCamera Example

This example demonstrates how to integrate the Navigine SDK into an Android app to display an indoor map with interactive camera controls, 
including programmatic camera movements and manual zoom via UI buttons.

---

## ✅ What does this example show?

- **SDK Initialization**: Configuring the Navigine SDK server URL and user hash.
- **Location Loading**: Using `LocationManager` and `LocationListener` to load a location and its sublocations.
- **Map View Setup**: Displaying the location in a `NavigationLocationView`.
- **Camera Controls**:
    - Zoom in/out via on-screen `+` and `–` buttons.
    - Programmatic camera movements (`flyTo` and `moveTo`) with customizable duration and animation type.
- **Callbacks**:
    - `LocationListener.onLocationLoaded` to receive location data.
    - `CameraListener.onCameraPositionChanged` to track camera updates (via gestures or application code).

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
   <!-- Location for BLE/Wi-Fi scanning -->
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <!-- Background location if needed -->
   <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

   <!-- Bluetooth & Wi-Fi scanning -->
   <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
   <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

   <!-- Internet & network state -->
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   ```
3. **Layout Resources**:
    - `activity_location_view_camera.xml` containing:
        - `NavigationLocationView`
        - Two `ImageButton`s: `btn_zoom_in` and `btn_zoom_out` (using drawables `ic_zoom_in`/`ic_zoom_out`).

---

## 🔧 Step-by-Step Guide

### 1. Initialize the SDK

In your `Activity`:

```kotlin
private fun initSdk() {
    val sdk = NavigineSdk.getInstance().apply {
        setServer("https://ips.navigine.com")        // Navigine backend URL
        setUserHash("YOUR_USER_HASH")                // Your user authentication hash
    }
    locationManager = sdk.locationManager.apply {
        locationId = YOUR_LOCATION_ID                // ID of the location to load
    }
}
```

### 2. Load Location & Sublocation

Register a `LocationListener` to receive callbacks:

```kotlin
locationListener = object : LocationListener() {
    override fun onLocationLoaded(location: Location?) {
        // Select your sublocation and fit camera bounds
        loadSublocation(location ?: return)
    }
    override fun onLocationFailed(locationId: Int, error: Error?) {
        Log.e("LocationViewCamera", "Failed to load: $error")
    }
}
locationManager?.addLocationListener(locationListener)
```

The helper method `loadSublocation()`:

```kotlin
private fun loadSublocation(location: Location) {
    val sub = location.sublocations.firstOrNull() ?: return
    navigationLocationView.post {
        with(navigationLocationView.locationWindow!!) {
            setSublocationId(sub.id)
            // Calculate min/max zoom and initial zoom to fit
            setupZoomConstraints(sub.width)
            setupZoomCameraDefault()
        }
    }
}
```

### 3. Set Up Zoom Buttons

In `onCreate()`, attach listeners:

```kotlin
binding.btnZoomIn.setOnClickListener { zoomIn() }
binding.btnZoomOut.setOnClickListener { zoomOut() }
```

Methods adjust the `zoomFactor` on the `locationWindow`:

```kotlin
private fun zoomIn() {
    locationWindow.zoomFactor *= 2f
}
private fun zoomOut() {
    locationWindow.zoomFactor /= 2f
}
```

### 4. Programmatic Camera Movements

Use `flyTo` for smooth animation:

```kotlin
val target = Camera(Point(x, y), zoomLevel, rotation)
adjustCamera(target)
```

Or `moveTo` with a chosen `AnimationType`:

```kotlin
moveCamera(target, duration = 500, animationType = AnimationType.CUBIC)
```

Internally these run on a coroutine to avoid blocking the UI thread.

### 5. Listen for Camera Updates

Implement `CameraListener`:

```kotlin
cameraListener = object : CameraListener() {
    override fun onCameraPositionChanged(
        reason: CameraUpdateReason?, isFinished: Boolean
    ) {
        Log.d("CameraListener", "Moved by $reason, finished=$isFinished")
    }
}
locationManager?.addCameraListener(cameraListener)
```

---

## 📂 Files Involved

- `LocationViewCameraActivity.kt` – main Activity with camera logic
- `activity_location_view_camera.xml` – UI layout for map and zoom controls
- `AndroidManifest.xml` – permissions and activity declarations
- `ic_zoom_in.xml` / `ic_zoom_out.xml` – drawable icons

---

## ⚠️ Notes

- Always remove listeners in `onPause()`/`onStop()` to prevent leaks:
  ```kotlin
  locationManager?.removeLocationListener(locationListener)
  locationManager?.removeCameraListener(cameraListener)
  ```
- Adjust zoom multiplier and camera duration/distance values based on your UX requirements.
- For production, consider initializing the SDK in your `Application` class and managing `LocationManager` as a singleton.


