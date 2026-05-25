# Navigine Android SDK — Examples

[![API](https://img.shields.io/badge/API-26%2B-brightgreen)](https://android-arsenal.com/api?level=26)
[![Platform](https://img.shields.io/badge/platform-Android-lightgrey)](https://developer.android.com)

A collection of minimal, focused examples for the Navigine Android SDK. Each example is a small Activity with a local README showing **how to integrate the SDK and perform common tasks**: loading locations, navigation, measurements, camera control, user input, picking map objects/features, and drawing objects on the map.

> **Looking for a complete app?** Check out [NavigineDemoCompose](../NavigineDemoCompose/) — a production-ready reference app with Clean Architecture, Hilt, and Compose UI.

---

## Quick Start

1. **Clone & open** the project in Android Studio.

2. **Add the SDK dependency** (you can find it in **libs** package in the project root):

```kotlin
dependencies {
    implementation(files("../libs/navigine.aar"))
}
```

3. **Configure the SDK** — replace placeholders in the examples:

```kotlin
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
examples/
├─ LocationListManager/       # list available locations, pick/get locationId
├─ NavigationManager/         # subscribe to position updates / navigation
├─ AsyncRouteManager/         # build routes asynchronously
├─ MeasurementManager/        # collect sensor & radio measurements
└─ LocationWindow/
   ├─ camera/                 # camera controls (zoom, flyTo/moveTo)
   ├─ pickers/                # InputListener taps & PickListener for objects/features
   └─ adding objects/         # add/remove Circle/Icon/FlatIcon/Polygon/Polyline
```

Each subfolder contains its own README with details and code snippets.

---

## Example Catalog

### LocationListManager
**Shows:** how to fetch and display available locations, obtain a `locationId`.

### NavigationManager
**Shows:** subscribing to user position updates, basic navigation setup.

### AsyncRouteManager
**Shows:** building a route and listening to async status/updates.

### MeasurementManager
**Shows:** `MeasurementListener` for sensors (ACCELEROMETER/GYRO/…) and signals (Wi‑Fi/BLE/Beacon/RTT).

### LocationWindow / camera
**Shows:** programmatic camera controls (`flyTo`/`moveTo`), on-screen zoom buttons, `CameraListener`.

### LocationWindow / pickers
**Shows:** tap/double‑tap/long‑tap via `InputListener`, picking map objects/features via `PickListener`.

### LocationWindow / adding objects
**Shows:** adding/removing `Circle/Icon/FlatIcon/Polygon/Polyline` while keeping a single instance of each type.

---

## Common Patterns

- **Lifecycle:** register listeners in `onResume`, remove in `onPause`/`onStop`.
- **UI thread:** when you need view dimensions, call `view.post { ... }` before working with `locationWindow`.
- **Zoom bounds:** compute `minZoomFactor`/`maxZoomFactor` from sublocation dimensions and the view width.
- **Map object references:** keep references (e.g., a `MapObjectsHolder`) for objects you add; otherwise you can only bulk‑remove via `removeAllMapObjects()`.
- **Error handling:** handle `onLocationFailed(...)` and log errors to diagnose configuration issues.

---

## Troubleshooting

**Blank map / no data** — verify `userHash`, server URL, a valid `locationId`, and required permissions.

**No BLE/Wi‑Fi readings** — on Android 12+ request `BLUETOOTH_SCAN` at runtime; ensure Bluetooth/Wi‑Fi are enabled on the device.

**Camera doesn't move** — ensure a valid `sublocationId` is set and call `flyTo`/`moveTo` only after `locationWindow` is initialized/measured.