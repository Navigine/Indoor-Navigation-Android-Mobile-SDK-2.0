# AsyncRouteManager Example

This example demonstrates how to **calculate** and **track** indoor routes *asynchronously* using the Navigine SDK.

`AsyncRouteManager` gives you route **data**: the path from the user's current position to a target, rebuilt automatically when the user deviates, and progress along it. It does not draw anything. To draw and follow a route on the map, see the **RouteLayer** example.

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

### 🔹 Step 1: Initialize the SDK and load a location

A location must be loaded: without it there is no position, and without a position there is no route.

```kotlin
Navigine.initialize(applicationContext)

val sdk = NavigineSdk.getInstance()
sdk.setServer("https://ips.navigine.com")
sdk.setUserHash("your_user_hash")

val locationManager = sdk.locationManager
locationManager.locationId = LOCATION_ID // obtained from the LocationListManager example
```
⚠️ In production, initialize the SDK in your Application class and reuse managers from a singleton like NavigineSdkManager.

---

### 🔹 Step 2: Obtain `NavigationManager` and `AsyncRouteManager`

```kotlin
navigationManager = sdk.getNavigationManager(locationManager)
asyncRouteManager = sdk.getAsyncRouteManager(locationManager, navigationManager)
```

---

### 🔹 Step 3: Create a Route Session

The route always starts from the user's current position, so subscribe to position updates first (`navigationManager.addPositionListener(...)`), then create a session to the target:

```kotlin
val targetPoint = LocationPoint(Point(1f, 1f), LOCATION_ID, SUBLOCATION_ID)
val options = RouteOptions(0.0, 3.0, 2.0)

routeSession = asyncRouteManager?.createRouteSession(targetPoint, options)
    ?.apply { addRouteListener(asyncRouteListener) }
```

- `targetPoint`: a `LocationPoint` in **local sublocation meters** within the loaded location. In a real app it usually comes from a map tap or a venue (`LocationPoint(venue.point, venue.locationId, venue.sublocationId)`).
- `RouteOptions(smoothRadius, maxProjectionDistance, maxAdvance)`: customize as needed.

> The map works in WGS84 (`GlobalPoint`), while routing works in local meters. A tap on the map gives you a `GlobalPoint`; convert it with `sublocation.globalToLocal(globalPoint)` before using it as a route target.

---

### 🔹 Step 4: Handle Callbacks

Implement `PositionListener` and `AsyncRouteListener`:

- `onPositionUpdated(position: Position?)`  
  Called when a new position has been calculated. `position.locationPoint` is in local meters, `position.point` is in WGS84.

- `onPositionError(error: Error)`  
  Called if the user's position cannot be calculated.

- `onRouteChanged(status: RouteStatus?, path: RoutePath?)`  
  Called when a route is built or rebuilt (for example, after the user deviates).  
  `status` is `NEW_ROUTE` when a route is available; otherwise it tells why it is missing (`MISSING_POSITION`, `MISSING_GRAPH`, `MISSING_ROUTE`, `MISSING_PROJECTION`).  
  `path.length` is the route length in meters, `path.nodes()` are the route nodes with their `LocationPoint`s.

- `onRouteAdvanced(distance: Float, currentPoint: LocationPoint?)`  
  Called when the user moves along the route.  
  `distance` is how far they've traveled along the route so far, in meters.

---

### 🔹 Step 5: Clean Up

Use a symmetric lifecycle: subscribe and create the session in `onStart()`, clean up in `onStop()`.  
Remove the route listener **before** cancelling the session, so no callbacks arrive from the session being cancelled:

```kotlin
override fun onStart() {
    super.onStart()
    addPositionListener()
    startRoute()
}

override fun onStop() {
    super.onStop()
    removePositionListener()
    cancelRoute()
}

private fun cancelRoute() {
    val session = routeSession ?: return
    asyncRouteListener?.let(session::removeRouteListener)
    asyncRouteManager?.cancelRouteSession(session)
    routeSession = null
}
```

---

## 📝 Notes

- **Which API to use:**
  - `AsyncRouteManager` — route data from the current position, rebuilt automatically (this example).
  - `RouteManager` — a one-off path between two arbitrary points.
  - `RouteLayer` — draws the route on the map and reports progress and arrival (see the RouteLayer example).
- You can't build a route without an available position.
- **Keep references to SDK listeners.** The SDK does not keep listener objects alive: a listener passed without being stored in a field can be garbage-collected, and its callbacks silently stop arriving. Store listeners in fields, as in this example.
- Always cancel your route session when the Activity or Fragment stops.
- Ensure `targetPoint` belongs to the loaded location (`LOCATION_ID`) and to an existing sublocation; if a position is available but no route arrives, the target is probably outside the navigation graph.