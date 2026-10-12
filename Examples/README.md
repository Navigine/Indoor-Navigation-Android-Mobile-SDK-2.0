# Navigine Android SDK — Examples

[![API](https://img.shields.io/badge/API-26%2B-brightgreen)](https://android-arsenal.com/api?level=26)
[![Platform](https://img.shields.io/badge/platform-Android-lightgrey)](https://developer.android.com)
[![Navigine SDK](https://img.shields.io/badge/Navigine%20SDK-2.29%2B-blue)](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0)

A collection of minimal, focused examples for the Navigine Android SDK. Each example is a small Activity with a local README showing **how to integrate the SDK and perform common tasks**: loading locations, positioning, routing and turn-by-turn guidance, showing the user on the map, the global (indoor + outdoor) map, zones, measurements, camera control, user input, picking map objects/features, and drawing objects on the map.

> **Looking for a complete app?** Check out [NavigineDemoCompose](../NavigineDemoCompose/) — a production-ready reference app with Clean Architecture, Hilt, and Compose UI.

---

## Quick Start

1. **Clone & open** the project in Android Studio.

2. **SDK dependency** — the SDK `.aar` is taken from the **libs** folder in the repository root:

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

3. **Configure the SDK** — initialize the library first, then replace placeholders in the examples:

```kotlin
Navigine.initialize(applicationContext)          // before the first NavigineSdk.getInstance()

NavigineSdk.getInstance().apply {
    setServer("https://ips.navigine.com") // your Navigine server URL
    setUserHash("YOUR_USER_HASH")         // your user hash
}
```

4. **Permissions** (plus runtime requests on Android 6+ / 12+):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- Android 12+: for BLE scanning -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
```

> Add `ACCESS_BACKGROUND_LOCATION` if your scenario requires background operation.

5. **locationId**: examples set it manually. You can obtain a valid `locationId` from the **LocationListManager** example (or from https://locations.navigine.com/).

6. **Build & run**: by default, the LAUNCHER activity is `LocationListManager.LocationListActivity`. To run a specific example either:

- Temporarily mark the desired Activity as LAUNCHER in `AndroidManifest.xml`, or
- Create a **Run Configuration** → Launch: *Specified Activity* → pick the Activity you want.

---

## Folder Structure

```
app/src/main/java/com/navigine/examples/
├─ LocationListManager/       # list available locations, pick/get locationId
├─ NavigationManager/         # subscribe to position updates
├─ AsyncRouteManager/         # route data from the current position (no drawing)
├─ ZoneManager/               # enter/leave zone events
├─ MeasurementManager/        # collect sensor & radio measurements
└─ LocationWindow/
   ├─ Camera/                 # camera controls (zoom, flyTo/moveTo)
   ├─ Pickers/                # InputListener taps & PickListener for objects/features
   ├─ AddingObjects/          # add/remove Circle/Icon/Polygon/Polyline
   ├─ UserLocation/           # user position on the map, follow modes
   ├─ RouteLayer/             # draw and follow a route on the map
   ├─ Guidance/               # turn-by-turn guidance camera and instructions
   └─ GlobalMap/              # indoor floors on top of the OSM outdoor map
```

Each subfolder contains its own README with details and code snippets.

---

## Example Catalog

### LocationListManager
**Shows:** how to fetch and display available locations, obtain a `locationId`.

### NavigationManager
**Shows:** subscribing to user position updates, basic navigation setup.

### AsyncRouteManager
**Shows:** route **data** from the user's current position — building, automatic rebuilding and progress via `RouteSession` and `AsyncRouteListener`. Nothing is drawn; to draw a route on the map, see **LocationWindow / RouteLayer**.

### ZoneManager
**Shows:** `ZoneListener` with `ENTER` / `EXIT` events and tracking the zones the user is currently inside.

### MeasurementManager
**Shows:** `MeasurementListener` for sensors (ACCELEROMETER/GYRO/…) and signals (Wi‑Fi/BLE/Beacon/RTT).

### LocationWindow / Camera
**Shows:** programmatic camera controls (`flyTo`/`moveTo` to the floor center), on-screen zoom buttons, `CameraListener`, `CameraCallback`.

### LocationWindow / Pickers
**Shows:** tap/double‑tap/long‑tap via `InputListener`, requesting picks with `pickMapObjectAt`/`pickMapFeatureAt` and receiving them via `PickListener`, converting a screen position to WGS84 with `screenPositionToGlobal`.

### LocationWindow / AddingObjects
**Shows:** adding/removing `Circle/Icon/Polygon/Polyline` in WGS84 coordinates while keeping a single instance of each type.

### LocationWindow / UserLocation
**Shows:** `UserLocationLayer` — the user's position arrow and accuracy circle, follow modes (`NONE`/`POSITION`/`HEADING`/`COURSE`), styling.

### LocationWindow / RouteLayer
**Shows:** `RouteLayer` — setting a target from a long tap, drawing the route, remaining distance and time, arrival.

### LocationWindow / Guidance
**Shows:** `GuidanceCamera` — the camera following the route (`FREE`/`FOLLOWING`/`OVERVIEW`, 2D mode) and the next instruction via `GuidanceListener`.

### LocationWindow / GlobalMap
**Shows:** switching between indoor-only and global mode (`OperatingMode.OUTDOOR_INDOOR`) together with the camera limits it needs, and the outdoor basemap theme.

---

## Common Patterns

- **Keep references to SDK objects.** The SDK holds listeners, managers, layers and controllers by **weak references**. An object that nothing in your code references can be garbage-collected, after which its callbacks silently stop arriving. Store them in fields (or a ViewModel / singleton).
- **Lifecycle:** add listeners in `onStart`/`onResume` and remove them in the paired `onStop`/`onPause`. Objects that live as long as the map (route layer, guidance camera) are cleaned up in `onDestroy`.
- **Coordinates:** the map works in **WGS84** (`GlobalPoint` + sublocation id, `null` = outdoors) — map objects, camera, screen conversions, `RouteEndpoint`. Positions, venues and `AsyncRouteManager`/`RouteManager` use **local sublocation meters** (`LocationPoint`). Convert at the boundary with `Sublocation.localToGlobal` / `globalToLocal`.
- **Picking is explicit:** a tap only reaches `InputListener`; call `pickMapObjectAt` / `pickMapFeatureAt` to get results in `PickListener`.
- **UI thread:** when you need view dimensions, call `view.post { ... }` before working with `locationWindow`.
- **Zoom bounds:** compute `minZoomFactor`/`maxZoomFactor` from sublocation dimensions and the view width.
- **Map object references:** keep references (e.g., a `MapObjectsHolder`) for objects you add; otherwise you can only bulk‑remove via `removeAllMapObjects()`.
- **Animation durations** in `flyTo` / `moveTo` are in milliseconds.
- **Error handling:** handle `onLocationFailed(...)` and log errors to diagnose configuration issues.

---

## Upgrading from SDK < 2.27

| Before                                              | Now                                                              |
|-----------------------------------------------------|------------------------------------------------------------------|
| Map coordinates in local meters (`Point`, `LocationPoint`) | WGS84 `GlobalPoint` + sublocation id                      |
| `screenPositionToMeters` / `metersToScreenPosition` | `screenPositionToGlobal` / `globalToScreenPosition`              |
| `setColor(r, g, b, a)` (floats)                     | `setColor(int argb)`                                             |
| `FlatIconMapObject`                                 | `IconMapObject`                                                  |
| Route drawn manually from `RoutePath`               | `RouteLayer` (+ `GuidanceCamera` for turn-by-turn)               |

See the **AddingObjects**, **Pickers** and **RouteLayer** examples for the new API in action.

---

## Troubleshooting

**Blank map / no data** — verify `userHash`, server URL, a valid `locationId`, and required permissions.

**Empty blue map in global mode** — the map was switched to global mode before the camera was positioned over the building, so it shows (0, 0) in the ocean. Load the floor in indoor mode first (see **GlobalMap**).

**Callbacks stop arriving after a while** — a listener or another SDK object was garbage-collected. Keep it in a field.

**No BLE/Wi‑Fi readings** — on Android 12+ request `BLUETOOTH_SCAN` at runtime; ensure Bluetooth/Wi‑Fi are enabled on the device.

**No route / `MISSING_POSITION`** — routing starts from the user's current position: make sure beacons are in range and the user location is visible. If a position is available but there is still no route, the target is probably outside the navigation graph.

**Tap doesn't pick anything** — picking must be requested with `pickMapObjectAt` / `pickMapFeatureAt` (see **Pickers**).

**Camera doesn't move** — ensure a valid `sublocationId` is set and call `flyTo`/`moveTo` only after `locationWindow` is initialized/measured.