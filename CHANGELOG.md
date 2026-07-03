# Change Log
All notable changes to this project will be documented in this file
adheres to [Semantic Versioning](http://semver.org/).

## 2.26.2
* Fixed DefaultNavigationLayer direction blinking
* Improved OpenGL/Vulkan functions loading

## 2.26.1
* Replaced **djinni_support_lib** with a new **`common`** library that owns platform binding infrastructure (Android JNI, iOS ObjC++, Flutter FFI).
* Rewrote Djinni generators (JNI, ObjC++, FFI, C++) to emit code against **navigine::android**, **navigine::ios**, and **navigine::bindings::flutter** instead of legacy **navigine::platform::*** / **djinni::*** APIs.
* Removed vendored Djinni support-lib (JNI/ObjC/FFI helpers, **NativeLibLoader**, **djinni_main**, etc.).
* Added Android binding runtime: **NativeObject**, **Subscription** (`com.navigine.common`), JNI helpers in **navigine/android/**.
* Added iOS binding runtime: **NCSubscription**, object/cast helpers in **navigine/ios/**.
* Added Flutter binding runtime under **navigine/bindings/flutter/** (opaque handles, interface wrappers, finalizable storage, main isolate helpers).
* Extracted shared geometry/error types into a separate **common.djinni** IDL module (Point, Polygon, Error, etc.).

## 2.26.0
* Added **ClusterMapObject** and **ClusterMapObjectController** for icon map object clustering.
* Added Swift Package Manager support for iOS and Flutter iOS SDK (**Package.swift**, install guide).
* Added Android SDK publication to Maven Central and Javadoc artifacts to JitPack.
* Improved listener lifetime: auto-unsubscribe expired listeners in generated bindings (Djinni).
* Improved platform-specific API documentation generation (Java, Objective-C, Dart doc snippets).

## 2.25.2
* Added **Camera.tilt** (degrees; 0 = top-down) to the public API; **flyTo**, **setCameraEased**, and **getEnclosingCamera** now animate and preserve tilt.
* Fixed **Camera.rotation** units: values are now consistently degrees in the API layer (including **UserLocationLayer** heading mode); previously some paths mixed radians and degrees.
* Reworked **TileManager** tile loading and proxy resolution (including grandparent tile fallback); extracted **BuildingsPayload** / **buildBuildingsFromLocation**; added **building_manager** unit tests.
* Improved gesture and inertial scrolling: kinetic zoom uses screen center as pivot; simplified **InertialScalar** interval model; scroll bounds with rubber-band effect applied inline in **GestureHandler** (removed **scroll_bounds**, **rubber_band**, and **setScrollBoundsScale**).
* Improved **ImageProvider** decoded RGBA handling on Android.

## 2.25.1
# Fix crash in SDK reset method

## 2.25.0
* Added **ModelMapObject** and **ModelProvider** (Wavefront OBJ + texture): C++ rendering (**model_asset** / **model_style**, shaders), sample POI mesh assets, bindings for Android, iOS (**NCModelProviderFactory**, file/data providers), and Flutter (**model_provider.dart**).
* Reworked **ImageProvider** and platform bitmap handling for decoded RGBA (**icon_downloader**, markers, sprites); **IconMapObject** and related pipelines now use **ImageProvider**; Flutter **image_provider.dart** extended (**image_provider_result**, **raw_image**, slimmer **image_wrapper**); removed the legacy standalone bitmap shim from bundled IDL.
* Improved **scroll bounds**, kinetic/gesture panning, and **DefaultNavigationView** behavior on Android and iOS (floor selector, zoom controls); minor projection/header cleanup.

## 2.24.5
* Added Android light and iOS lite mobile binary builds.
* Added `sdk reset` API and `setPublishIntervalMs` for `MeasurementManager`.
* Added public logger support and user agent forwarding in requests.
* Added outdoor scenario support and in `DefaultNavigationView`.
* Improved gesture handling and map filter state updates while rebuilding tiles.
* Cleanup and dependency optimization in mobile builds and contribs.

## 2.24.4
* Fix flutter android initialization

## 2.24.3
* Added MqttSession
* Fixed ble scanning

## 2.24.2
* Added DefaultNavigationView
* Added UserLocationLayer

## 2.24.1
* Fixed gl resource deletion.
* Added 3d barriers support

## 2.24.0
* Added StorageManager
* Improved LocationWindow behavior on first start

## 1.2.14
* Adaptive image rendering
* Improved onRouteChanged method

## 1.2.13
* Added PolygonMapObject
* Added selectMapFeature/deselectMapFeature/deselectAllMapFeatures/getSelectedMapFeatures

## 1.2.12
* Fix crash in interface finalization

## 1.2.11
* Fix CircleMapObject rendering

## 1.2.10
* Removed unused dart files
* Changed creation of LocationWindow

## 1.2.9
* Fixed navigation algorighms
* Fixed crash on new flutter versions

## 1.2.8
* Fixed Metal DEPTH_TEST
* Fixed navigation algorighms
* Improved ssl certificate verification
