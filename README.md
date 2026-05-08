<a href="http://navigine.com"><img src="https://navigine.com/assets/web/images/logo.svg" align="right" height="60" width="180" hspace="10" vspace="5"></a>

# Android SDK 2.0

**📱 Quick Start:** [**🆕 Modern Compose Demo (Recommended)**](#-naviginedemocompose---modern-kotlin--jetpack-compose) | [Legacy Java/XML Demo](#naviginedemo---legacy-javaxml)

The following sections describe the contents of the Navigine Android SDK repository. The files in our public repository for Android are: 

- Sources of the Navigine Demo Applications for Android (Compose + Java)
- Navigine SDK for Android in form of a AAR file

## Useful Links
- Learn more about our solutions on [the Navigine website](https://navigine.com/)
- [SDK Documentation](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki)
- Refer to the [Navigine official documentation](https://docs.navigine.com) for complete list of downloads, useful materials, information about the company, and so on.
- [Get started](http://locations.navigine.com/login) with Navigine to get full access to Navigation services, SDKs, and applications.
- Refer to the Navigine [User Manual](http://docs.navigine.com/) for complete product usage guidelines.
- Find company contact information at the official website under <a href="https://navigine.com/contacts/">Contact</a> tab.
- [Join our community](https://community.navigine.com/t/indoor-navigation-android-mobile-sdk-2-0/177) to discuss this project. Share your use cases, questions, and suggestions

## Values and benefits

<p align="center"><img  width="100%" height="100%" src=https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/blob/master/img/values%20and%20benefits.jpg></p>

**Enhanced User Experience**: An Indoor Navigation SDK for Android provides users with an intuitive and seamless navigation experience within indoor environments. It helps them easily navigate complex spaces such as shopping malls, airports, museums, or hospitals, reducing confusion and improving overall user satisfaction.

**Accurate Positioning**: The SDK utilizes advanced positioning technologies, such as Bluetooth Low Energy (BLE), Wi-Fi, UWB etc., to achieve high accuracy in indoor positioning. Users can rely on precise location information to navigate to specific destinations, find points of interest, and locate desired products or services within a venue.

**Indoor Mapping and Wayfinding**: The SDK offers indoor mapping capabilities, allowing developers to integrate detailed maps of indoor spaces into their Android applications. Users can benefit from interactive maps, highlighted routes, and turn-by-turn directions to efficiently navigate through the venue and reach their desired destinations.

**Location-Based Services**: With an Indoor Navigation SDK, developers can build Android applications that offer location-based services tailored to specific indoor environments. This opens up opportunities to provide personalized recommendations, targeted promotions, and context-aware information based on the user's location within the venue.

**Integration with Existing Apps**: The SDK can be seamlessly integrated into existing Android applications, enhancing their functionality with indoor navigation capabilities. This enables businesses and organizations to leverage their existing user base and infrastructure, saving time and resources while providing a value-added service to their customers.

**Improved Operational Efficiency**: For businesses and organizations, an Indoor Navigation SDK can optimize operational efficiency. It can assist in managing crowds, monitoring visitor flows, and optimizing resource allocation within a venue. This data-driven approach helps businesses streamline their operations, improve staff productivity, and enhance the overall visitor experience.

**Analytics and Insights**: An Indoor Navigation SDK often includes analytics and reporting functionalities, providing valuable insights into user behavior, traffic patterns, and popular areas within a venue. Businesses can leverage this data to make informed decisions, optimize space utilization, and identify opportunities for improvements or revenue generation.

**Multi-Venue Support**: Many Indoor Navigation SDKs offer support for multiple venues, allowing businesses to provide a consistent navigation experience across different locations. This flexibility is particularly beneficial for large retail chains, airports, or exhibition centers with multiple venues under their operation.

**Customization and Branding**: The SDK provides customization options, allowing developers to tailor the indoor navigation experience to match their branding and user interface guidelines. This ensures a consistent and cohesive user experience within the application, reinforcing brand identity and familiarity.

**Developer-Friendly Tools and Support**: Indoor Navigation SDKs come with comprehensive documentation, developer tools, and dedicated support channels. This empowers developers to efficiently integrate and utilize the SDK, accelerating the development process and ensuring a smooth implementation.

## Android Demo Applications

We provide **two demo applications** to help you get started with Navigine SDK. Choose based on your tech stack:

### 🆕 NavigineDemoCompose - Modern Kotlin + Jetpack Compose

**⭐ Recommended for new projects** - Production-ready demo showcasing Navigine SDK with modern Android development.

**Why choose the Compose version:**
- ✨ **50% less boilerplate** - Clean Architecture with Kotlin-first approach
- 🚀 **Modern UI/UX** - Material 3 design, intuitive navigation
- 🎯 **Production patterns** - Hilt DI, Coroutines/Flow, proper state management
- 🔧 **Developer-friendly** - Type-safe navigation, better testability, clear code structure
- 📱 **Latest standards** - Follows current Android best practices (2024+)

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt DI, Coroutines/Flow, Retrofit, DataStore

**Get Started:**
- 📦 **[Download APK](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/releases/tag/2.24.4/download/NavigineCompose-debug.apk)** - Try it now on your device
- 📖 **[Full Documentation](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/tree/master/NavigineDemoCompose/)** - Setup guide, architecture details, troubleshooting
- 💻 **[Source Code](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/tree/master/NavigineDemoCompose/)** - Clone and adapt to your needs

**Screenshots:**

<img src="NavigineDemoCompose/screenshots/navigation.jpg" alt="Navigation" width="200"/>&nbsp;&nbsp;<img src="NavigineDemoCompose/screenshots/locations.jpg" alt="Locations" width="200"/>&nbsp;&nbsp;<img src="NavigineDemoCompose/screenshots/debug.jpg" alt="Debug" width="200"/>&nbsp;&nbsp;/>

**Quick Start:**
```bash
git clone https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0.git
cd Indoor-Navigation-Android-Mobile-SDK-2.0/NavigineDemoCompose
# Open in Android Studio and run
```

---

### NavigineDemo - Legacy Java/XML

Our stable Java/XML demo application for developers working with legacy codebases or preferring traditional Android patterns.

Navigine Demo application enables you to test indoor navigation as well as measure your target location's radiomap.

**Get the Java Demo:**
- [Download APK](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/releases/tag/2.24.4/download/NavigineDemo-debug.apk)
- [Browse Source Code](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/tree/master/NavigineDemo)

For complete guidelines, refer to the [Navigine User Manual](https://docs.navigine.com/en/Using_Navigine_Application_for_Android).

**Screenshots:**

<img src="img/locations.png" alt="img/locations.png" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="img/navigation.png" alt="img/navigation.png" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="img/debug.png" alt="img/debug.png" width="250"/>

---

### 📊 Which Demo Should I Use?

| Aspect | NavigineDemoCompose | NavigineDemo (Java) |
|:--|:--|:--|
| **UI Framework** | Jetpack Compose | XML Views |
| **Language** | Kotlin | Java |
| **Architecture** | Clean Architecture + MVVM | Traditional Android |
| **State Management** | Flow + Coroutines | Callbacks |
| **Dependency Injection** | Hilt | Manual |
| **Code Complexity** | ~2,500 lines | ~5,000+ lines |
| **Material Design** | Material 3 (2024) | Material 2 |
| **Recommended For** | ⭐ New projects, modern apps | Legacy codebases |
| **Maintenance** | **Actively maintained** | Stable, limited updates |

## Navigation SDK and Implementation

Navigine SDK for Android applications enables you to develop your own indoor navigation apps using the well-developed methods, classes, and functions created by the Navigine team.
The SDK file resides in the libs folder.

Find formal description of Navigine-SDK API including the list of classes and their public fields and methods at [Navigine SDK wiki](https://github.com/Navigine/Android-SDK-2.0/wiki).

### Using .aar file in Android Studio

- Download `libnavigine.aar` file from current repositories `libs` folder;
- In your project choose `File` -> `New` -> `New module` -> `Import .JAR/.AAR Package`;
- Select downloaded `libnavigine.aar` file and add it to your project as new module;
- Add following lines inside `dependencies` module in your apps `build.gradle` file:
```
  implementation project(":libnavigine")
```
- Start using Navigine SDK.

### Using with Jitpack

[![](https://jitpack.io/v/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0.svg)](https://jitpack.io/#Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0)

JitPack lets you consume this repository (and its Gradle modules) directly from GitHub.

**1) Add the JitPack repository**

<details>
<summary><code>settings.gradle.kts</code> (recommended)</summary>

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```
</details>

<details>
<summary><code>build.gradle.kts</code> (legacy projects)</summary>

```kotlin
repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}
```
</details>

**2) Add a dependency**

- To depend on the **root artifact** (single-module publish):
```kotlin
dependencies {
    implementation("com.github.Navigine:Indoor-Navigation-Android-Mobile-SDK-2.0:<TAG>")
}
```

- To depend on a **specific module** from this monorepo (multi-module publish), use:
```kotlin
dependencies {
    // Example: take a module named "libnavigine" (replace with actual module name)
    implementation("com.github.Navigine.Indoor-Navigation-Android-Mobile-SDK-2.0:libnavigine:<TAG>")
}
```

> Use a Git tag/release (recommended) or a commit hash as `<TAG>`.

## Jetpack Compose wrapper: LocationView Compose

A modern, production-ready Compose wrapper around Navigine’s `LocationView` — inspired by the quality bar of
`android-maps-compose`. It provides idiomatic Compose APIs, lifecycle-aware state, and property updaters.

**Highlights**
- Two ready-to-use map composables: `DefaultNavigineLocation` (built-in UI widgets) and `NavigineLocation` (full control)
- Camera state hoisting with animated and immediate movement
- Tap, double-tap, long-tap and pick event handlers
- Declarative map objects: `Circle`, `Icon`, `Polyline`, and more
- Building and sublocation event handlers for campus mode
- 3D rendering toggle and gesture controls
- Widget customization: zoom controls, floor selector, follow-me button
- Lifecycle-aware — cleanup handled automatically across recompositions

### 🎯 See it in a real app!

Want to see `LocationViewCompose` in action? Check out **[NavigineDemoCompose](NavigineDemoCompose/)** - a production-ready demo that showcases all features:
- Camera state management & animations
- Map objects (polylines, circles, polygons)
- Input handlers (tap, long-press, feature picking)
- Lifecycle-aware setup
- Integration with Hilt & Clean Architecture

The demo is a perfect reference for building your own indoor navigation app with Compose!

### Getting started

**1) Add dependencies**

```kotlin
dependencies {
    // Note: core sdk is pulled transitively with the library
    implementation("com.navigine:navigine-locationview-compose:2.24.5")
}
```

> Make sure `mavenCentral()` is added to your project repositories.

**2) Initialize Navigine in your `Application` class**

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Navigine.initialize(this)
    }
}
```

**3) Configure the SDK in your `Activity`**

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

**4) Required permissions**

Grant Location + Bluetooth (BLE) permissions according to your target SDK (foreground + background if needed).
Ensure the device supports BLE (Android 8.0+).

### Quick start

`DefaultNavigineLocation` is the quickest way to get a map with zoom controls, floor selector, and follow-me button out of the box:

```kotlin
DefaultNavigineLocation(modifier = Modifier.fillMaxSize())
```

Use `NavigineLocation` when you need full control over the UI:

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize())
```

### Camera control

```kotlin
val cameraState = rememberNavCameraPositionState()
 
NavigineLocation(
    modifier = Modifier.fillMaxSize(),
    cameraPositionState = cameraState
)
 
LaunchedEffect(Unit) {
    cameraState.flyTo(
        camera = Camera(point = Point(100.0, 200.0), zoom = 18f, rotation = 0f),
        durationMs = 1000
    )
}
```

### Map objects and input handlers

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize()) {
    Circle(
        position = LocationPoint(100.0, 200.0),
        radius = 50f,
        color = Color.Blue
    )
 
    InputHandlers(
        onTap = { viewPoint, meters -> /* handle tap */ },
        autoPickObjectOnTap = true
    )
 
    PickHandlers(
        onObjectPicked = { result, viewPoint -> /* handle pick */ },
        onFeaturePicked = { attrs, viewPoint -> /* handle feature */ }
    )
}
```

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

### Customizing built-in widgets

```kotlin
DefaultNavigineLocation(
    modifier = Modifier.fillMaxSize(),
    widgetConfig = DefaultNavigineWidgetConfig(
        visibility = DefaultWidgetVisibility(showFloorSelector = false),
        followMe = FollowMeButtonAppearance(accentColor = Color(0xFF0057FF))
    )
)
```

### Campus mode — building events

```kotlin
NavigineLocation(modifier = Modifier.fillMaxSize()) {
    BuildingHandlers(
        onBuildingFocused = { sublocations, activeId, switchSublocation ->
            // Show your custom floor selector
            // Call switchSublocation(id) to change the active floor
        },
        onBuildingLeft = {
            // Hide floor selector
        }
    )
}
```

### Samples

A sample app with bottom navigation shows 4 demos (Camera, Shapes, Input, Icons) that you can use as a reference for
routing, drawing shapes, and handling taps. It’s a good starting point for your own project structure.

### Notes

- The wrapper handles resource cleanup and respects recomposition and lifecycle events.
- For release builds, add R8/Proguard keep rules for SDK classes if needed.
- **Source code:** [LocationViewCompose/location-view](https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/tree/master/LocationViewCompose/location-view)
- Full API reference: [navigine-locationview-compose on Maven Central](https://central.sonatype.com/artifact/com.navigine/navigine-locationview-compose)

### Android&HW compatibility
Indoor positioning SDK and applications require Android 8.0 or higher as well your smartphone should have BLE 4.0 or higher.

We are testing our SDK and Apps on the following smartphones:
Nexus Pixel 3	(Android 12),  Honor 30 pro (Android	10), Xiaomi Redmi	9C (Android	10), Samsung A20 (Android 10, 11), Huawei P20 lite (Android	9), Xiaomi Note	 (Android	8).
