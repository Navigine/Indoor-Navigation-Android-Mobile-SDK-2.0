# MeasurementManager Example

This example demonstrates how to use the **Navigine SDK MeasurementManager** to collect raw sensor and signal measurements (Wi-Fi, Bluetooth, Beacons, etc.) in an Android application.

---

## ✅ What does this example show?

- Initializing the Navigine SDK
- Obtaining the `MeasurementManager` instance
- Setting up and registering a `MeasurementListener`
- Handling sensor measurements (accelerometer, gyroscope, magnetometer, etc.)
- Handling signal measurements (Wi-Fi, iBeacon, Eddystone, Bluetooth)
- Configuring measurement generators to control which data types are collected

---

## 🛠 Prerequisites

1. **Android Permissions**  
   Ensure your app requests the following permissions at runtime:
   ```kotlin
   val requestPermissions = registerForActivityResult(
     ActivityResultContracts.RequestMultiplePermissions()
   ) { permissions ->
     // Check permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true, etc.
   }
   requestPermissions.launch(arrayOf(
     Manifest.permission.ACCESS_FINE_LOCATION,
     Manifest.permission.BLUETOOTH_SCAN,     // Android 12+
     Manifest.permission.BLUETOOTH_CONNECT   // Android 12+
   ))
   ```

---

## 🔧 Step-by-Step Guide

### 1. Initialize the SDK
In your `Activity` or `Application` class:
```kotlin
Navigine.initialize(applicationContext)
val sdk = NavigineSdk.getInstance().apply {
    setServer("https://ips.navigine.com")
    setUserHash("YOUR_USER_HASH")
}
val measurementManager = sdk.getMeasurementManager(sdk.locationManager)
```

### 2. Setup the MeasurementListener
Implement `MeasurementListener` to handle callbacks:
```kotlin
val measurementListener = object : MeasurementListener() {
    override fun onSensorMeasurementDetected(
        measurements: HashMap<SensorType, SensorMeasurement>?
    ) {
        // Raw sensor data: 
        // data.vector3D -> (x, y, z) of type float
        // data.timestamp -> time in ms
    }

    override fun onSignalMeasurementDetected(
        signals: HashMap<String, SignalMeasurement>?
    ) {
        // Signal data: 
        // signal.rssi      -> Received Signal Strength (dBm)
        // signal.distance  -> Estimated distance (m)
        // signal.timestamp -> time in ms
    }
}
```

### 3. Register and Unregister the Listener
Add in `onCreate()` or `onStart()`:
```kotlin
measurementManager.addMeasurementListener(measurementListener)
```
Remove in `onStop()` or `onDestroy()`:
```kotlin
measurementManager.removeMeasurementListener(measurementListener)
```

### 4. Configure Measurement Generators
You can add generators to collect specific data types:
```kotlin
// iBeacon generator
measurementManager.addBeaconGenerator(
  uuid = "00000000-0000-0000-0000-000000000000",
  major = 1, minor = 2, txPower = -59,
  timeout = 0, rssiMin = -100, rssiMax = 0
)

// Eddystone (UID) generator
measurementManager.addEddystoneGenerator(
  namespace = "namespace",
  instanceId = "instanceId",
  txPower = -59, timeout = 0, rssiMin = -100
)

// Wi-Fi generator
measurementManager.addWifiGenerator(
  mac = "00:11:22:33:44:55",
  timeout = 0, rssiMin = -100, rssiMax = 0
)

// Location generator
measurementManager.addLocationGenerator(
  latMin = 0.0, latMax = 0.0,
  lonMin = 0.0, lonMax = 0.0,
  accuracyMin = 0.0, accuracyMax = 10.0,
  timeout = 0
)
```

---

## 📂 Files Involved

- `MeasurementActivity.kt` – main Activity demonstrating `MeasurementManager` setup and callbacks
- `activity_measurement.xml` – layout containing `TextView`s for displaying sensor and signal outputs

---

## ⚠️ Notes

- Always remove the listener to prevent memory leaks.
- Adjust generator parameters (UUIDs, MACs, RSSI thresholds) to match your environment.
- For production, initialize the SDK in your `Application` class and reuse managers from a central singleton.

