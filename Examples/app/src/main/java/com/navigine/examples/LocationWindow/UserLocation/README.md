# UserLocationLayer Example

This example shows how to display the **user's position on the map** with `UserLocationLayer` and how to make the camera **follow** the user.

`UserLocationLayer` renders the position arrow and the accuracy circle by itself. You don't need to subscribe to positions and move a marker manually.

---

## ✅ What does this example show?

- **Creating** a `UserLocationLayer` for the map window and making it visible.
- **Styling** the accuracy circle (`setAccuracyColor`) and the heading arrow (`setArrowBitmap`).
- **Follow modes**: `NONE`, `POSITION`, `HEADING`, `COURSE`.
- **Keeping the UI in sync** when a map gesture switches following off.

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
3. **Beacons in range.** The layer shows nothing until a position is available.
4. **Layout**: `activity_location_view_user_location.xml` with a `LocationView` and a toggle group for the follow mode.

---

## 🔧 Step-by-Step Guide

### 1) Initialize the SDK, load a location and start positioning

```kotlin
private fun initSdk() {
    Navigine.initialize(applicationContext)
    val sdk = NavigineSdk.getInstance()
    sdk.setServer("https://ips.navigine.com")
    sdk.setUserHash("YOUR_USER_HASH")

    locationManager = sdk.locationManager
    locationManager?.locationId = YOUR_LOCATION_ID

    // Positioning runs through NavigationManager — keep a reference to it
    navigationManager = sdk.getNavigationManager(locationManager)
}
```

Load the first sublocation in `LocationListener.onLocationLoaded` and fit the zoom to it — same as in the other `LocationWindow` examples.

### 2) Create the layer

One `UserLocationLayer` per `LocationWindow`:

```kotlin
userLocationLayer = NavigineSdk.getInstance()
    .getUserLocationLayer(binding.navigationLocationView.locationWindow)
    .apply {
        setAccuracyColor(Color.argb(66, 48, 170, 217))
        // setArrowBitmap(ImageProvider.fromResource(context, R.drawable.my_arrow))
        setVisible(true)
    }
```

- `setAccuracyColor` — ARGB fill of the accuracy circle (default: translucent blue).
- `setArrowBitmap` — replaces the heading arrow; `null` restores the built-in one.

### 3) Choose how the camera follows the user

```kotlin
userLocationLayer?.setFollowMode(UserLocationFollowMode.POSITION)
```

| Mode       | Camera behavior                                                         |
|------------|-------------------------------------------------------------------------|
| `NONE`     | Doesn't follow the user.                                                |
| `POSITION` | Keeps the user in the anchor point (screen center by default).          |
| `HEADING`  | Follows the position and rotates the map so the user's heading points up. |
| `COURSE`   | Follows the position and rotates the map along the direction of movement. |

Use `setAnchor(PointF)` to keep the user at a custom screen point (for example, lower on the screen when a bottom sheet is open). `setAnchor()` from `NONE` switches to `POSITION`, `resetAnchor()` switches to `NONE`.

### 4) Keep the UI in sync with gestures

A map gesture (pan, pinch) switches the follow mode to `NONE`. There is no dedicated callback for that, so listen to gesture-driven camera changes:

```kotlin
cameraListener = object : CameraListener() {
    override fun onCameraPositionChanged(reason: CameraUpdateReason?, finished: Boolean) {
        if (reason != CameraUpdateReason.GESTURES) return
        if (userLocationLayer?.followMode() == UserLocationFollowMode.NONE) {
            binding.followModeGroup.check(R.id.btn_follow_none)
        }
    }
}
```

Add the camera listener in `onStart()` and remove it in `onStop()`.

---

## 📂 Files Involved

- `LocationViewUserLocationActivity.kt` — Activity with the user location layer and follow modes.
- `activity_location_view_user_location.xml` — layout with `LocationView` and the follow mode toggle group.
- `AndroidManifest.xml` — permissions and activity declaration.

---

## ⚠️ Notes

- **Using `DefaultNavigationView`?** It already creates a `UserLocationLayer` and a follow-me button — don't add a second layer.
- The layer draws the position on its floor. If the user is on another floor, switch the map with `setSublocationId` (e.g. from a `PositionListener`) to see them.
- **Keep references to SDK objects** (managers, the layer, listeners) in fields — the SDK holds them by weak references and they can be garbage-collected otherwise.
- For production, consider initializing the SDK in your `Application` class and managing managers as a singleton.