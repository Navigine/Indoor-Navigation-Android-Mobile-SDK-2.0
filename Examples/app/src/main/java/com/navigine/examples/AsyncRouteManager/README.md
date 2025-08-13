# AsyncRouteManager Example

This example demonstrates how to **calculate** and **track** indoor routes *asynchronously* using the Navigine SDK.

## ✅ Step-by-step Guide

### 🔹 Prerequisites

- **Runtime Permissions**  
  Before using navigation features, you must request:
  ```kotlin
  // Example with Activity Result API
  private val requestPermissions = 
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
      // Check result[Manifest.permission.ACCESS_FINE_LOCATION] == true etc.
    }
  requestPermissions.launch(arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH_SCAN  // on Android 12+
  ))
  ```
- **Beacons in Range**  
  The SDK relies on Navigine BLE beacons for indoor positioning.  
  If no beacons are detected, you will not receive route updates.

---

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

### 🔹 Step 2: Obtain `NavigationManager` and `AsyncRouteManager`

```kotlin
val locationManager = sdk.locationManager
navigationManager = sdk.getNavigationManager(locationManager)
asyncRouteManager = sdk.getAsyncRouteManager(locationManager, navigationManager)
```

### 🔹 Step 3: After adding a PositionListener, create a Route Session

```kotlin
val options = RouteOptions(0.0, 3.0, 2.0)
routeSession = asyncRouteManager?.createRouteSession(targetPoint, options)
    ?.apply { addRouteListener(asyncRouteListener) }
```

- `targetPoint`: a valid `LocationPoint` within your loaded map.
- `RouteOptions(smoothRadius, maxProjectionDistance, maxAdvance)`: customize as needed.

### 🔹 Step 4: Handle Callbacks

Implement `AsyncRouteListener` and `PositionListener`:

- `onPositionUpdated(position: Position?)`
  Called when new position has been calculated.

- `onPositionError(error: Error)`
  Called if unable to calculate user's position.

- `onRouteChanged(path: RoutePath?)`  
  Called when a new route is successfully calculated or recalculated.  
  You can use `path.points` to draw a polyline on your `LocationView`.

- `onRouteAdvanced(distance: Float, currentPoint: LocationPoint?)`  
  Called when the user moves along the route.  
  `distance` is how far they've traveled along the route so far.

### 🔹 Step 5: Clean Up

Cancel the session and remove listeners in `onPause()` or `onStop()` to avoid memory leaks:

```kotlin
asyncRouteManager?.cancelRouteSession(routeSession)
routeSession?.removeRouteListener(asyncRouteListener)
routeSession = null
```

---

## 📝 Notes
- You can't build a route without available position. 
- It uses user's position as starting point. If you want to calculate path between two points, use RouteManager.
- Always cancel your route session when the Activity or Fragment stops.
- Ensure `targetPoint` and any passed `LocationPoint` correspond to the current location.
