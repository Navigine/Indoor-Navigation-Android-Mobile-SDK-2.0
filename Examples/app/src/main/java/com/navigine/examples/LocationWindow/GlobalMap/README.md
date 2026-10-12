# Global Map Example

This example shows how to switch the map between **indoor-only** mode and **global** mode — indoor floors drawn on top of the OSM outdoor basemap — and how to change the outdoor basemap theme.

---

## ✅ What does this example show?

- **Operating mode**: `LocationWindow.operatingMode` — `INDOOR_ONLY` (default) and `OUTDOOR_INDOOR`.
- **Camera limits for global mode**: turning off `stickToBorder` and lowering `minZoomFactor`.
- **Restoring indoor limits** when global mode is turned off.
- **Outdoor basemap theme**: `LocationWindow.mapTheme` — `LIGHT` / `DARK`.

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
2. **Permissions**:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   ```
   Outdoor tiles are downloaded from the network.
3. **Layout**: `activity_location_view_global_map.xml` with a `LocationView` and two switches (`Global map`, `Dark basemap`).
4. **Location with geo-reference.** Global mode places floors on the world map using the location's WGS84 binding. Make sure your location is geo-referenced in the Navigine CMS.

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

### 2) Load the floor in indoor mode first

Start in indoor mode so the camera is positioned over the floor. Remember the floor's minimum zoom — you will need it to leave global mode:

```kotlin
private fun loadSublocation(location: Location) {
    val sublocation = location.sublocations.firstOrNull() ?: return

    binding.navigationLocationView.post {
        binding.navigationLocationView.locationWindow?.apply {
            setSublocationId(sublocation.id)
            val pixelWidth = binding.navigationLocationView.width / resources.displayMetrics.density
            val fitZoom = pixelWidth / sublocation.width
            maxZoomFactor = (fitZoom * 16f).coerceIn(1f, 600f)
            minZoomFactor = (fitZoom / 16f).coerceIn(1f, 600f)
            zoomFactor = fitZoom.coerceIn(minZoomFactor, maxZoomFactor)
            floorMinZoomFactor = minZoomFactor
        }
        binding.switchGlobalMap.isEnabled = true
    }
}
```

### 3) Switch the operating mode together with the camera limits

```kotlin
private fun setGlobalMapEnabled(enabled: Boolean) {
    val window = binding.navigationLocationView.locationWindow ?: return
    window.operatingMode = if (enabled) OperatingMode.OUTDOOR_INDOOR else OperatingMode.INDOOR_ONLY
    window.stickToBorder = !enabled
    window.minZoomFactor = if (enabled) 0.01f else floorMinZoomFactor ?: window.minZoomFactor
}
```

Changing only `operatingMode` is not enough:
- with `stickToBorder = true` the camera stays locked to the floor bounds;
- with the indoor `minZoomFactor` you can't zoom out far enough to see the streets.

### 4) Change the outdoor basemap theme

```kotlin
binding.navigationLocationView.locationWindow?.mapTheme =
    if (isDark) MapTheme.DARK else MapTheme.LIGHT
```

The theme affects only the outdoor basemap; indoor floors, venues and your map objects are unchanged.

---

## 📂 Files Involved

- `LocationViewGlobalMapActivity.kt` — Activity with mode switching and camera limits.
- `activity_location_view_global_map.xml` — layout with `LocationView` and switches.
- `AndroidManifest.xml` — permissions and activity declaration.

---

## ⚠️ Notes

- **Don't create the map directly in global mode** before the camera knows where your building is: it opens at (0, 0), in the middle of the ocean, and you see an empty blue map. Load the floor in indoor mode first, then switch.
- `operatingMode` is applied only when it changes; switching it rebuilds the renderer pipeline, so avoid setting it on every frame or recomposition.
- **OSM attribution** is shown in global mode (bottom-right by default). Move it with `attributionAlignment` if it overlaps your UI, but keep it visible.
- **Custom outdoor tiles:** set `LocationWindow.tileProvider` (`TileProvider` with an `HttpTileSource` or a local MBTiles pack; vector or raster). `null` uses the default OSM Shortbread tiles.
- In global mode the user can move between buildings of a campus; use `addBuildingListener` to react when the camera focuses on a building or leaves it.
- Keep references to SDK objects (listeners, managers) in fields — the SDK holds them by weak references.