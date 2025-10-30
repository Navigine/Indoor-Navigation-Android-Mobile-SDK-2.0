# Navigine Demo Compose (Production-Grade Sample)

A modern, production‑ready demo app that showcases **Navigine Indoor Navigation SDK** with a **Jetpack Compose** UI.  
The goal of this project is to provide a clear, copy‑and‑use template that you can clone, build, and adapt to your own.

> This app is a fresh Compose rewrite of the legacy demo (same business logic, cleaner architecture, Kotlin‑first).


## ✨ Highlights

- **Jetpack Compose** UI with Material 3
- **Multiple tabs** with independent back stacks (Navigation / Locations / Debug / Profile)
- **Indoor map** via `NavigineLocation` composable
- **QR onboarding** (server + user hash + optional location/sublocation)
- **Route building** (e.g., POI → route)
- **Locations list** with search & selection
- **Debug screen**: position, sensors, signals, system state (BT/Location)
- **Profile screen**: view/edit, logout
- **Clean Architecture** (data‑domain‑presentation), **Coroutines/Flow**
- **Hilt DI**, **DataStore** (preferences)
- **Retrofit/Moshi** networking with **dynamic base URL**
- **AppLogger** with **Firebase Analytics** & **Crashlytics** integration (optional, opt‑in)


## 🧱 Tech Stack

- Kotlin, Coroutines, Flow
- Jetpack Compose, Material3, Navigation Compose
- Dagger **Hilt**
- DataStore (Preferences)
- Retrofit + OkHttp + Moshi
- Firebase Analytics & Crashlytics (via unified `AppLogger`)
- Clean Architecture, modular responsibilities


## 📦 Project Structure (key pieces)

```
app/
  ui/
    login/                 // Login flow and QR onboarding
    navigation/            // Map screen + route UI
    locations/             // Location list
    debug/                 // Debug snapshot UI
    profile/               // Profile view/edit
  core/
    sdk/                   // NavigineSdkManager (state: Idle/Configuring/Ready/Error)
    di/                    // Hilt modules (Network, DataStore, SDK, Firebase logger)
    log/                   // AppLogger interface + Firebase implementation
  data/                    // Repositories, DTOs
  domain/                  // Interfaces for repositories/monitors
```

Key runtime pieces:
- **`NavigineSdkManager`** – single entry point to the SDK (+ readiness state).  
- **Monitors** – wrappers around SDK listeners with safe lifetimes (`Position/Location/Measurement/...`).  


## ✅ Requirements

- Android Studio (latest stable)
- Android SDK (see exact `compileSdk`, `minSdk` in `app/build.gradle.kts`)
- A valid **Navigine server URL** and **user hash** (from your Navigine account)

> This repo contains **no secrets**. You must supply your own server/hash at runtime.


## 🚀 Getting Started

1) **Clone** the repo and open in Android Studio.

2) **(Optional) Enable Firebase**  
   - Place your `google-services.json` into the `app/` module.
   - Keep these plugins in `app/build.gradle.kts`:
     ```kotlin
     plugins {
       id("com.google.gms.google-services")
       id("com.google.firebase.crashlytics")
     }
     ```
   - Dependencies use Firebase BOM; see `dependencies {}` in `app/build.gradle.kts`.

   If you don’t want Firebase, remove those two plugins and the Firebase dependencies.

3) **Build & Run** on a real device (recommended for BLE & sensors).

4) **Log in / Configure SDK**  
   - Enter **Server URL** and **User Hash** on the Login screen, or  
   - **Scan a QR** with parameters (see below).  
   Once configured, `NavigineSdkManager` switches to **Ready** state and the rest of the app starts receiving data from the SDK.

5) **Use the tabs**:  
   - **Navigation** – map, route building, FAB, bottom sheet  
   - **Locations** – search/select location  
   - **Debug** – live position/sensors/signals, system switches  
   - **Profile** – view/edit data, logout


## 🔐 Permissions

The app requests the following at runtime (Android version dependent):

- **Camera** – QR code scanning
- **Bluetooth** – scanning beacons (on Android 12+: `BLUETOOTH_SCAN/CONNECT`)
- **Location** – for indoor positioning (`ACCESS_FINE_LOCATION`, and background as appropriate)
- (Recommended) **Battery optimization exception** – to keep scanning active in the background

If Bluetooth or Location is off, the Debug screen points it out and some features won’t work.


## 🧪 Tips & Troubleshooting

- **“Navigine SDK is not configured”**: make sure you completed Login or QR onboarding. The SDK becomes *Ready* only after a successful `configure(server, hash)`.
- **No beacons in background**: ensure Bluetooth is on, Location is on, and battery optimizations are disabled for the app.
- **Route not visible**: confirm the selected location/sublocation matches your map data; check Debug for current position/sensors.
- **Server URL** must be reachable from your device/network (interceptor rewrites the base URL on every request).


## 🤝 Contributing

Issues and PRs are welcome. Please keep the style consistent:
- Kotlin, clear naming, no magic numbers
- KDoc for public APIs
- Follow the existing layer boundaries and DI patterns
- Prefer small, focused PRs

