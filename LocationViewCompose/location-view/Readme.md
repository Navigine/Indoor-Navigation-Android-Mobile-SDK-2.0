# navigine-locationview-compose

A thin, idiomatic Jetpack Compose wrapper around the [Navigine Indoor Navigation SDK](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0) for Android.

[![Maven Central](https://img.shields.io/maven-central/v/com.navigine/navigine-locationview-compose)](https://central.sonatype.com/artifact/com.navigine/navigine-locationview-compose)

---

## Features

- Compose-first API — no direct interaction with the underlying OpenGL/View layer required
- Two ready-to-use map composables: `NavigineLocation` (full control) and `DefaultNavigineLocation` (built-in UI widgets)
- Declarative map objects: `Circle`, `Icon`, `Polyline`, `Model` (3D), and more
- Camera state hoisting via `NavCameraPositionState`
- Tap, double-tap, long-tap and pick event handlers
- Building and sublocation event handlers for campus mode
- 3D rendering toggle and gesture controls via `LocationUiSettings`
- Lifecycle-aware — `onStart` / `onStop` / `onLowMemory` handled automatically

---

## Requirements

- Android `minSdk = 24`
- Jetpack Compose BOM
- Navigine SDK initialized before using any composable (see [Setup](#setup))

---

## Installation

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.navigine:navigine-locationview-compose:<version>")
}
```

The latest version is available on [Maven Central](https://central.sonatype.com/artifact/com.navigine/navigine-locationview-compose).

---

## Setup

**1. Initialize Navigine in your `Application` class:**

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Navigine.initialize(this)
    }
}
```

**2. Configure the SDK in your `Activity`:**

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            val sdk = NavigineSdk.getInstance().apply {
                setServer("https://ips.yourserver.com")
                setUserHash("0000-0000-0000-0000")
            }
            sdk.locationManager.locationId = YOUR_LOCATION_ID
        }.onFailure { it.printStackTrace() }

        setContent { /* your Compose UI */ }
    }
}
```

---

## Usage

### Basic map

`DefaultNavigineLocation` is the quickest way to get a map with zoom controls, floor selector, and a follow-me button out of the box:

```kotlin
DefaultNavigineLocation(modifier = Modifier.fillMaxSize())
```

Use `NavigineLocation` when you need full control over the UI:

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize())
```

---

### Camera control

```kotlin
val cameraState = rememberNavCameraPositionState()

NavigineLocation(
    modifier = Modifier.fillMaxSize(),
    cameraPositionState = cameraState
)

LaunchedEffect(Unit) {
    cameraState.flyTo(
        camera = Camera(point = Point(100.0, 200.0), zoom = 18f, rotation = 0f, tilt = 0f),
        durationMs = 1000
    )
}
```

---

### Map objects

Declare map objects inside the `content` block:

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize()) {
    Circle(
        position = LocationPoint(100.0, 200.0),
        radius = 50f,
        color = Color.Blue
    )
}
```

#### Icon

Use `ImageProvider` to load the icon image from a bitmap, resource, asset, or file:

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize()) {
    // From a drawable resource
    Icon(
        position = LocationPoint(100.0, 200.0),
        image = ImageProvider.fromResource(context, R.drawable.pin)
    )

    // From an asset file
    Icon(
        position = LocationPoint(100.0, 200.0),
        image = ImageProvider.fromAsset(context, "icons/pin.png")
    )

    // From a bitmap
    Icon(
        position = LocationPoint(100.0, 200.0),
        image = ImageProvider.fromBitmap(myBitmap)
    )
}
```

#### Icon clustering

Use `ClusterController` to group nearby `Icon` objects into clusters. Pass only the `IconState` handles that should participate in clustering:

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize()) {
    val iconStates = pins.map { pin ->
        key(pin.id) {
            val state = rememberIconState()

            Icon(
                state = state,
                position = pin.position,
                image = ImageProvider.fromResource(context, R.drawable.pin)
            )

            state
        }
    }

    ClusterController(
        icons = iconStates,
        config = ClusterConfig(
            radius = 60f,
            clusterSize = Size(48f, 48f)
        ),
        onClusterCreated = { cluster ->
            cluster.setBitmap(createClusterBadge(cluster.count))
        },
        onClusterChanged = { cluster ->
            cluster.setBitmap(createClusterBadge(cluster.count))
        },
        onClusterDestroyed = { clusterId ->
            Log.d("Clusters", "Cluster destroyed: $clusterId")
        }
    )
}
```

Cluster picks are delivered through `PickHandlers`, the same way as other map object picks.

---

#### 3D Model

Use `ModelProvider` to load a `.obj` model with a texture:

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize()) {
    val texture = ImageProvider.fromResource(context, R.drawable.model_texture)

    // From an asset file
    Model(
        position = LocationPoint(100.0, 200.0),
        model = ModelProvider.fromAsset(context, "models/chair.obj", texture)
    )

    // From a raw resource
    Model(
        position = LocationPoint(100.0, 200.0),
        model = ModelProvider.fromResource(context, R.raw.chair, texture)
    )
}
```

> **Note:** The texture bitmap must use `Bitmap.Config.ARGB_8888`.

---

### Tap and pick handlers

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize()) {
    InputHandlers(
        onTap = { viewPoint, meters ->
            // handle tap
        },
        onLongTap = { viewPoint, meters ->
            // handle long tap
        },
        autoPickObjectOnTap = true
    )

    PickHandlers(
        onObjectPicked = { result, viewPoint ->
            // handle picked map object
        }
    )
}
```

---

### Gesture and rendering settings

```kotlin
NavigineLocation(
    modifier = Modifier.fillMaxSize(),
    uiSettings = LocationUiSettings(
        rotateGesturesEnabled = false,
        is3dEnabled = true
    )
)
```

---

### Customizing built-in widgets

```kotlin
DefaultNavigineLocation(
    modifier = Modifier.fillMaxSize(),
    widgetConfig = DefaultNavigineWidgetConfig(
        visibility = DefaultWidgetVisibility(showFloorSelector = false),
        followMe = FollowMeButtonAppearance(
            accentColor = Color(0xFF0057FF),
            marginBottom = 64.dp
        )
    )
)
```

---

### Campus mode — building events

Use `BuildingHandlers` inside `NavigineLocation` to react to building focus changes and switch floors programmatically:

> **Note:** `BuildingHandlers` is designed for use with `NavigineLocation`. `DefaultNavigineLocation` manages building events internally for its floor selector widget.

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize()) {
    BuildingHandlers(
        onBuildingFocused = { sublocations, activeId, switchSublocation ->
            // Show your custom floor selector UI
            // Call switchSublocation(id) to change the active floor for this building
        },
        onBuildingLeft = {
            // Hide floor selector
        },
        onActiveSublocationChanged = { sublocationId ->
            // React to floor change
        }
    )
}
```

---

### Low-level SDK access

If you need direct access to `LocationWindow` for SDK features not yet exposed by the library, use the `onWindowReady` escape hatch:

```kotlin
NavigineLocation(
    modifier = Modifier.fillMaxSize(),
    onWindowReady = { window ->
        // Direct SDK access — use with care
    }
)
```

---

## License

Proprietary — see [Navigine Terms of Use](https://navigine.com/privacy/#end_user).