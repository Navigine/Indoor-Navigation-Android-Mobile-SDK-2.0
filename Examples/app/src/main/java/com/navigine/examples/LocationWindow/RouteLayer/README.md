# RouteLayer Example

This example shows how to **draw and follow a route on the map** with `RouteLayer`.

`RouteLayer` builds the route from the user's current position to a target, draws it on the `LocationView`, rebuilds it when the user deviates or changes floors, and reports progress and arrival. You don't need to build polylines from a route path yourself.

> Need only route data without drawing (e.g. for your own UI)? See the **AsyncRouteManager** example.

---

## ✅ What does this example show?

- **SDK Initialization** and loading a location/sublocation.
- **Creating and styling** a `RouteLayer` for the map window.
- **Setting a route target** from a long tap: `screenPositionToGlobal` → `RouteEndpoint` → `setTarget`.
- **Tracking the route** with `RouteLayerListener`: route length, remaining distance and time, arrival.
- **Clearing the route** with `clear()`.

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
2. **Permissions** (request at runtime on Android 6+ / 12+):
   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   ```
3. **Beacons in range.** The route starts from the user's current position, so positioning must work — otherwise you will get `MISSING_POSITION`.
4. **Layout**: `activity_location_view_route_layer.xml` with a `LocationView`, a status `TextView` and a `Clear` button.

---

## 🔧 Step-by-Step Guide

### 1) Initialize the SDK and load a location

```kotlin
private fun initSdk() {
    Navigine.initialize(applicationContext)
    val sdk = NavigineSdk.getInstance()
    sdk.setServer("https://ips.navigine.com")
    sdk.setUserHash("YOUR_USER_HASH")

    locationManager = sdk.locationManager
    locationManager?.locationId = YOUR_LOCATION_ID
}
```

Load the first sublocation in `LocationListener.onLocationLoaded` and fit the zoom to it — same as in the other `LocationWindow` examples.

### 2) Create and style the RouteLayer

One `RouteLayer` per `LocationWindow`. Style it once — the layer draws the route by itself:

```kotlin
routeLayer = NavigineSdk.getInstance()
    .getRouteLayer(binding.navigationLocationView.locationWindow)
    .apply {
        setWidth(8f)
        setRemainingStyle(RouteRemainingStyle.DOTTED) // SOLID / DASHED / DOTTED
        setRemainingColor(Color.rgb(76, 217, 100))
        setRemainingPointSize(8f, 8f)
        setRemainingPlacement(Placement.SPACED)
        setRemainingPlacementSpacing(8f)
        setRemainingPlacementMinRatio(0f)
        setRemainingCollisionEnabled(false)
        isVisible = true
        addRouteLayerListener(routeLayerListener)
    }
```

### 3) Set a target from a long tap

`RouteEndpoint` takes WGS84 coordinates and a sublocation id directly — no conversion to meters is needed:

```kotlin
override fun onViewLongTap(point: PointF?) {
    point ?: return
    val window = binding.navigationLocationView.locationWindow ?: return
    val target = window.screenPositionToGlobal(point) ?: return
    routeLayer?.setTarget(RouteEndpoint(target, window.sublocationId))
}
```

`window.sublocationId` is the floor shown at the moment of the tap (`null` means outdoors). To route to a venue instead, convert its local point: `sublocation.localToGlobal(LocationPoint(venue.point, venue.locationId, venue.sublocationId))`.

### 4) Track the route

```kotlin
routeLayerListener = object : RouteLayerListener() {
    override fun onRouteChanged(status: RouteLayerStatus?, route: RouteView?) {
        when (status) {
            RouteLayerStatus.ROUTE_UPDATED -> route?.let { showStatus("Route: ${it.length()} m") }
            RouteLayerStatus.IDLE, null -> Unit
            else -> showStatus("Route status: $status") // MISSING_POSITION, MISSING_GRAPH, ...
        }
    }

    override fun onRouteAdvanced(progress: RouteProgress?) {
        progress ?: return
        showStatus("Remaining: ${progress.remainingDistance} m, ${progress.remainingTime} s")
    }

    override fun onRouteTargetReached() {
        clearRoute()
    }
}
```

- `ROUTE_UPDATED` — a route is built or rebuilt; `route.length()` is its length in meters.
- `MISSING_*` — why there is no route: no position, no navigation graph, target unreachable, etc.
- `RouteProgress.remainingTime` — pedestrian ETA in seconds, calculated by the SDK.

### 5) Clear the route and clean up

```kotlin
private fun clearRoute() {
    routeLayer?.clear()
}

override fun onDestroy() {
    routeLayer?.apply {
        removeRouteLayerListener(routeLayerListener)
        clear()
    }
    routeLayer = null
    super.onDestroy()
}
```

The input listener is added in `onStart()` and removed in `onStop()`. The route listener lives as long as the layer, so progress and arrival are not lost while the screen is briefly in the background.

---

## 📂 Files Involved

- `LocationViewRouteLayerActivity.kt` — Activity with the route layer, long-tap target and listeners.
- `activity_location_view_route_layer.xml` — layout with `LocationView`, status text and `Clear` button.
- `AndroidManifest.xml` — permissions and activity declaration.

---

## ⚠️ Notes

- **Static routes:** `setRoute(from, to)` draws a route between two arbitrary points without following the user's position.
- **Keep references to SDK listeners.** The SDK does not keep listener objects alive: a listener passed without being stored in a field can be garbage-collected, and its callbacks silently stop arriving. Store listeners in fields, as in this example.
- Create the layer once per `LocationWindow`; calling `setTarget` again replaces the current route.
- `MISSING_POSITION` can be temporary (e.g. a short positioning gap) — the layer continues guidance when the position returns.
- For production, consider initializing the SDK in your `Application` class and managing managers as a singleton.