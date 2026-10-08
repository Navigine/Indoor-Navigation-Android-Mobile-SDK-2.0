# Navigine Demo — Kotlin / Jetpack Compose

A production-ready reference app showcasing the [Navigine Indoor Navigation SDK](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0) with a modern Android stack.

[![API](https://img.shields.io/badge/API-26%2B-brightgreen)](https://android-arsenal.com/api?level=26)
[![Platform](https://img.shields.io/badge/platform-Android-lightgrey)](https://developer.android.com)
[![Navigine SDK](https://img.shields.io/badge/Navigine%20SDK-2.29%2B-blue)](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0)

---

## Screenshots

<img src="screenshots/navigation.jpg" alt="Navigation" width="200"/>&nbsp;&nbsp;<img src="screenshots/locations.jpg" alt="Locations" width="200"/>&nbsp;&nbsp;<img src="screenshots/debug.jpg" alt="Debug" width="200"/>

---

## What's inside

| Screen         | Description                                                   |
|----------------|---------------------------------------------------------------|
| **Navigation** | Indoor and global map, route building and guidance, map settings |
| **Locations**  | Search and select a location                                  |
| **Debug**      | Live position, sensors, signals, BT/Location state            |
| **Profile**    | View/edit, logout                                             |

**Key patterns demonstrated:**
- SDK initialization and readiness state machine (`Idle → Configuring → Ready → Error`)
- QR onboarding (server URL + user hash + optional location)
- Location and position subscriptions via Monitors (lifecycle-safe SDK listener wrappers)
- Route drawing and guidance with the SDK `RouteLayer`, wrapped in a declarative `NavigationRoute` composable
- Global map (indoor floors over the OSM basemap) via the `operatingMode` parameter of the map composable
- Map input via `InputHandlers` / `PickHandlers` — the ViewModel never holds a `LocationWindow`; the screen passes it plain facts (tapped coordinate and floor)
- Dynamic base URL with Retrofit interceptor
- Hilt DI across all layers

---

## Tech Stack

- **Language:** Kotlin, Coroutines, Flow
- **UI:** Jetpack Compose, Material 3, Navigation Compose
- **DI:** Dagger Hilt
- **Storage:** DataStore (Preferences)
- **Network:** Retrofit + OkHttp + Moshi
- **Map:** [navigine-locationview-compose](../LocationViewCompose/)

---

## Project Structure

```
app/
├── ui/
│   ├── login/        # Login flow and QR onboarding
│   ├── navigation/   # Map screen, route UI, map effects (NavigationRoute)
│   ├── locations/    # Location list
│   ├── debug/        # Debug snapshot UI
│   └── profile/      # Profile view/edit
├── core/
│   ├── sdk/          # NavigineSdkManager
│   ├── di/           # Hilt modules
│   └── log/          # AppLogger interface
├── domain/           # Models and monitor interfaces (LocationMonitor, PositionMonitor)
└── data/             # Repositories, DTOs, monitor implementations
```

---

## Getting Started

**1. Clone and open in Android Studio:**

```bash
git clone https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0.git
cd Indoor-Navigation-Android-Mobile-SDK-2.0/NavigineDemoCompose
```

**2. Build and run on a real device** — recommended for BLE and sensor features.

**3. Log in:** enter your Navigine server URL and user hash on the Login screen, or scan a QR code with those parameters.

> Don't have credentials? [Get started at locations.navigine.com](https://locations.navigine.com/login)

**4. (Optional) Enable Firebase:** place your `google-services.json` into `app/` and keep the Firebase plugins in `app/build.gradle.kts`. To opt out — remove the plugins and Firebase dependencies.

---

## Working with the SDK

A few things worth knowing if you use this demo as a starting point for your own app.

**Keep SDK listeners alive.** The SDK does not keep your listener objects alive: a listener that nothing references can be garbage-collected, after which its callbacks silently stop arriving. In Compose, create the listener inside a `DisposableEffect` and remove it in `onDispose` — the `onDispose` lambda keeps it referenced for as long as the effect is active:

```kotlin
DisposableEffect(layer) {
    val listener = object : RouteLayerListener() { /* ... */ }
    layer.addRouteLayerListener(listener)
    onDispose { layer.removeRouteLayerListener(listener) }
}
```

`InputHandlers`, `PickHandlers` and `NavigationRoute` already follow this pattern.

**Map coordinates are WGS84.** Map objects, the camera and map input work with `GlobalPoint` plus an optional sublocation id (`null` means outdoors). Venues and positions still carry local metric coordinates (`LocationPoint`). Convert at the boundary with `Sublocation.localToGlobal` / `globalToLocal` — in this demo that happens in the ViewModel.

**Let `RouteLayer` draw the route.** It builds the route from the current position, redraws it when the user deviates or changes floors, and reports progress (remaining distance and time) and arrival. See `NavigationRoute` for a declarative wrapper: pass a target to start guidance, pass `null` to clear it.

**Global map needs relaxed camera limits.** Switching `operatingMode` to `OUTDOOR_INDOOR` alone is not enough — also turn off `stickToBorder` and lower `minZoomFactor` via `LocationProperties`, otherwise the camera stays locked to the floor.

**`flyTo` duration is in milliseconds.**

---

## Permissions

| Permission                             | Reason                             |
|----------------------------------------|------------------------------------|
| `CAMERA`                               | QR code scanning                   |
| `ACCESS_FINE_LOCATION`                 | Indoor positioning                 |
| `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` | BLE beacon scanning (Android 12+)  |
| `ACCESS_BACKGROUND_LOCATION`           | Background positioning (if needed) |

Battery optimization exception is recommended to keep scanning active in the background.

---

## Troubleshooting

**"SDK is not configured"** — complete Login or QR onboarding first. The SDK becomes `Ready` only after a successful `configure(server, hash)`.

**No beacons in background** — ensure Bluetooth and Location are on and battery optimizations are disabled for the app.

**Route not visible** — routing starts from the current position, so make sure a position is available (the user location marker is visible) and that the location has a navigation graph. Also confirm the selected location/sublocation matches your map data; the Debug screen shows the current position and sensor state.

**Callbacks stop arriving after a while** — a listener was garbage-collected. Keep it referenced (see [Working with the SDK](#working-with-the-sdk)).

**Server unreachable** — the base URL is rewritten dynamically on every request; make sure your server URL is reachable from the device's network.