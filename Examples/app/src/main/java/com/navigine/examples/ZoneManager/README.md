# ZoneManager Example

This example shows how to detect when the user **enters or leaves zones** with `ZoneManager`.

Zones are areas drawn in the Navigine CMS (a shop, a meeting room, an entrance). `ZoneManager` uses the user's position from `NavigationManager` and reports `ENTER` / `EXIT` events — a typical base for geofencing scenarios: greetings, offers, analytics, access control.

---

## ✅ What does this example show?

- **Obtaining** a `ZoneManager` from `NavigationManager`.
- **Subscribing** to zone events with `ZoneListener`.
- **Tracking the set of zones** the user is currently inside.
- **Logging** `ENTER` / `EXIT` events.

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
3. **Zones in your location.** Draw zones for the location in the Navigine CMS.
4. **Beacons in range.** Zone events are based on the user's position.
5. **Layout**: `activity_zone.xml` with a text for the current zones and an event log.

---

## 🔧 Step-by-Step Guide

### 1) Initialize the SDK and obtain the managers

```kotlin
Navigine.initialize(applicationContext)
val sdk = NavigineSdk.getInstance()
sdk.setServer("https://ips.navigine.com")
sdk.setUserHash("YOUR_USER_HASH")

locationManager = sdk.locationManager
locationManager?.locationId = YOUR_LOCATION_ID

navigationManager = sdk.getNavigationManager(locationManager)
zoneManager = sdk.getZoneManager(navigationManager)
```

### 2) Listen to zone events

```kotlin
zoneListener = object : ZoneListener() {
    override fun onZoneEvent(zoneEvent: ZoneEvent?) {
        zoneEvent ?: return
        when (zoneEvent.type) {
            ZoneEventType.ENTER -> currentZones[zoneEvent.id] = zoneEvent.name
            ZoneEventType.EXIT -> currentZones.remove(zoneEvent.id)
            else -> return
        }
        renderCurrentZones()
    }
}
```

The callback is invoked on the UI thread, so you can update views directly.

`ZoneEvent` fields:

| Field           | Description                                   |
|-----------------|-----------------------------------------------|
| `type`          | `ENTER` or `EXIT`                             |
| `id`            | Zone id                                       |
| `name`          | Zone name from the CMS                        |
| `alias`         | Zone alias from the CMS                       |
| `locationId`    | Location of the zone                          |
| `sublocationId` | Floor of the zone                             |

`EXIT` also comes when the user changes floor or the zone is removed, so tracking `ENTER` / `EXIT` by `id` keeps the set of current zones consistent.

### 3) Register and remove the listener

```kotlin
override fun onStart() {
    super.onStart()
    zoneManager?.addZoneListener(zoneListener)
}

override fun onStop() {
    super.onStop()
    zoneManager?.removeZoneListener(zoneListener)
}
```

---

## 📂 Files Involved

- `ZoneActivity.kt` — Activity with zone tracking and event log.
- `activity_zone.xml` — layout with the current zones and the event log.
- `AndroidManifest.xml` — permissions and activity declaration.

---

## ⚠️ Notes

- **Keep references to SDK objects** (managers, listeners) in fields — the SDK holds them by weak references and they can be garbage-collected otherwise. Losing the `ZoneManager` or `ZoneListener` silently stops zone events.
- Zone events stop when the listener is removed in `onStop()`. To react to zones in the background, move the managers and the listener to a long-lived component (e.g. a foreground service) and handle background location permissions.
- Use `alias` as a stable key for business logic; `name` is meant for display and may change in the CMS.
- For production, consider initializing the SDK in your `Application` class and managing managers as a singleton.