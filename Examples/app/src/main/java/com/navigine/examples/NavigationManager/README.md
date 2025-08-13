# NavigationManager Example

This example demonstrates how to use the **Navigine NavigationManager**  
to receive user positioning updates from the Navigine SDK.

---

## ✅ What does this example show?

- How to initialize the Navigine SDK (if not done in `Application`)
- How to get the `NavigationManager`
- How to register a `PositionListener` to receive real-time position updates
- How to handle and use position data for your app (e.g., visualization in `LocationView`)

---

## 🧭 Step-by-step Guide

## ⚙️ Prerequisites

1. **Android Permissions**  
   Before initializing or using any Navigine location/navigation features, **you must** request and obtain the following permissions at runtime:
   ```kotlin
   // Example with AndroidX Activity KTX
   private val requestPermissions =
     registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
       // check result[Manifest.permission.ACCESS_FINE_LOCATION] == true etc.
     }

   requestPermissions.launch(arrayOf(
     Manifest.permission.ACCESS_FINE_LOCATION,
     Manifest.permission.BLUETOOTH_SCAN  // on Android 12+
   ))
   ```

2. **Beacons in Range**  
   Position updates are calculated from nearby Navigine BLE beacons.
   If you don’t have any beacons deployed (or they’re out of range), onPositionUpdated() will not be called.

### 🔹 Step 1: Initialize the SDK

Before using the `NavigationManager`, make sure the SDK is properly initialized:

```kotlin
val sdk = NavigineSdk.getInstance()
sdk.setServer("https://ips.navigine.com")
sdk.setUserHash("your_user_hash")
val locationManager = sdk.locationManager
val navigationManager = sdk.getNavigationManager(locationManager)
```
⚠️ In production, initialize the SDK in your Application class and reuse managers from a singleton like NavigineSdkManager.

---

### 🔹 Step 2: Create and register a PositionListener
```kotlin
private fun initListeners() {
    positionListener = object : PositionListener() {
        override fun onPositionUpdated(position: Position?) {
            // Handle position updates
        }

        override fun onPositionError(error: Error) {
            // Handle position errors
        }
    }
}
```
Then attach the listener:

```kotlin
private fun addPositionListener() {
    positionListener?.let {
        navigationManager?.addPositionListener(it)
    }
}
```
Don't forget to call removePositionListener() in onStop() or onDestroy().

---

### 🔹 Step 3: Use the Position Data
Each Position object includes the following information:
* point: GlobalPoint	Global coordinates (latitude, longitude)
* accuracy: Double	Accuracy of the position, in meters
* heading: Double?	Heading, angle of rotation about the -Z axis (in radians)
* headingAccuracy: Double?	Accuracy of heading value (in radians)
* locationPoint: LocationPoint	Position inside the location
* locationHeading: Double?	Similiar to heading but with respect to sublocation north

---

You can use this data to:

- Display the user on a LocationView
- Log location changes
- Build navigation UX

### 📦 Files Involved
NavigationActivity.kt	Main activity handling NavigationManager setup and listener
activity_navigation.xml	Layout for UI (optional TextView to display position, etc.)

### ⚠️ Notes
- NavigationManager works only after a location is loaded into the SDK.
- Always ensure PositionListener is removed when not needed to prevent memory leaks.
- Consider combining position updates with LocationView to show the user on the map.