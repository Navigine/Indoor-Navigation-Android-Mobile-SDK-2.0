# GuidanceCamera Example

This example shows **turn-by-turn guidance**: the camera follows the user along the route and the screen shows the next instruction.

Three SDK objects work together:
- `RouteLayer` builds and draws the route (see the **RouteLayer** example);
- `GuidanceCamera` drives the camera along the route and publishes the next `RouteInstruction`. It does **not** draw the route;
- `UserLocationLayer` shows the user on the map (see the **UserLocationLayer** example).

---

## ✅ What does this example show?

- **Creating** a `GuidanceCamera` bound to a `RouteLayer` of the same window.
- **Camera modes**: `FREE`, `FOLLOWING`, `OVERVIEW`, and the top-down `2D` option.
- **Next instruction** via `GuidanceListener.onInstructionChanged`.
- **Keeping the UI in sync** when a user gesture switches the camera to `FREE`.

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
3. **Beacons in range.** Guidance starts from the user's current position.
4. **Layout**: `activity_location_view_guidance.xml` with a `LocationView`, an instruction banner, a mode toggle group, a `2D` switch and a `Clear` button.

---

## 🔧 Step-by-Step Guide

### 1) Initialize the SDK, load a location and start positioning

```kotlin
Navigine.initialize(applicationContext)
val sdk = NavigineSdk.getInstance()
sdk.setServer("https://ips.navigine.com")
sdk.setUserHash("YOUR_USER_HASH")

locationManager = sdk.locationManager
locationManager?.locationId = YOUR_LOCATION_ID
navigationManager = sdk.getNavigationManager(locationManager)
```

Load the first sublocation in `LocationListener.onLocationLoaded` and fit the zoom to it — same as in the other `LocationWindow` examples.

### 2) Create the layers and the guidance camera

`GuidanceCamera` needs a `RouteLayer` of the same `LocationWindow`:

```kotlin
val window = binding.navigationLocationView.locationWindow

userLocationLayer = sdk.getUserLocationLayer(window).apply { setVisible(true) }

routeLayer = sdk.getRouteLayer(window).apply {
    isVisible = true
    addRouteLayerListener(routeLayerListener)
}

guidanceCamera = sdk.getGuidanceCamera(window, routeLayer).apply {
    set2DMode(false)
    addListener(guidanceListener)
}
```

### 3) Start guidance

Set a route target and switch the camera to `FOLLOWING`:

```kotlin
override fun onViewLongTap(point: PointF?) {
    point ?: return
    val window = binding.navigationLocationView.locationWindow ?: return
    val target = window.screenPositionToGlobal(point) ?: return
    routeLayer?.setTarget(RouteEndpoint(target, window.sublocationId))
    guidanceCamera?.setMode(GuidanceCameraMode.FOLLOWING)
}
```

| Mode        | Camera behavior                                                           |
|-------------|---------------------------------------------------------------------------|
| `FREE`      | Leaves the camera alone. Also entered when the user pans or pinches.      |
| `FOLLOWING` | Looks along the route at the current progress.                            |
| `OVERVIEW`  | Fits the whole route into `LocationWindow.focusRect` (or the full view).  |

- `set2DMode(true)` — top-down camera (tilt 0) in `FOLLOWING` and `OVERVIEW`; with `false`, `FOLLOWING` uses a forward tilt.
- `setActive(false)` — stops moving the camera; instructions keep updating.

### 4) Show the next instruction and track the mode

```kotlin
guidanceListener = object : GuidanceListener() {
    override fun onCameraModeChanged(mode: GuidanceCameraMode?) {
        // Also called when a user gesture switches the camera to FREE
        binding.guidanceModeGroup.check(mode.toButtonId())
    }

    override fun onInstructionChanged(instruction: RouteInstruction?) {
        instruction ?: return
        val action = instruction.title?.takeIf { it.isNotBlank() } ?: instruction.type.name
        showInstruction("In ${instruction.distance} m: $action")
    }
}
```

`RouteInstruction` carries the annotation `type` (turn, floor transition, finish, …), its WGS84 `point` and `sublocationId`, the `distance` to it in meters and a `title`.

Arrival is reported by the route layer — `RouteLayerListener.onRouteTargetReached()`.

### 5) Clear the route and clean up

```kotlin
private fun clearRoute() {
    routeLayer?.clear()
    guidanceCamera?.setMode(GuidanceCameraMode.FREE)
}

override fun onDestroy() {
    guidanceCamera?.removeListener(guidanceListener)
    guidanceCamera = null
    routeLayer?.apply {
        removeRouteLayerListener(routeLayerListener)
        clear()
    }
    routeLayer = null
    userLocationLayer?.setVisible(false)
    userLocationLayer = null
    super.onDestroy()
}
```

---

## 📂 Files Involved

- `LocationViewGuidanceActivity.kt` — Activity with the route layer, guidance camera and user location layer.
- `activity_location_view_guidance.xml` — layout with `LocationView`, instruction banner and controls.
- `AndroidManifest.xml` — permissions and activity declaration.

---

## ⚠️ Notes

- **Overview and on-screen UI:** `OVERVIEW` fits the route into `LocationWindow.focusRect`. If your UI covers part of the map (banner, bottom sheet, floor selector), set `focusRect` to the free area before switching to `OVERVIEW`, otherwise part of the route ends up under the UI.
- **Don't fight over the camera.** While guidance is in `FOLLOWING`/`OVERVIEW`, don't also use `UserLocationLayer.setFollowMode` or your own `flyTo` — keep one owner of the camera.
- **Keep references to SDK objects** (managers, layers, the guidance camera, listeners) in fields — the SDK holds them by weak references and they can be garbage-collected otherwise.
- For production, consider initializing the SDK in your `Application` class and managing managers as a singleton.