# LocationListManager Example

This example demonstrates how to use the **Navigine LocationListManager**  
to fetch and display a list of available locations from the platform.

---

## ✅ What does this example show?

- How to initialize the Navigine SDK (if not done in `Earlier`)
- How to access `LocationListManager` from the SDK
- How to register a `LocationListListener` to receive updates
- How to display the list of locations in a `TextView`

---

## 🧭 Step-by-step Guide

### 🔹 Step 1: Initialize the SDK

You can initialize the SDK inside your activity (for demonstration purposes):

```kotlin
val sdk = NavigineSdk.getInstance()
sdk.setServer("https://ips.navigine.com")
sdk.setUserHash("your_user_hash")
val locationListManager = sdk.locationListManager
```

---

### 🔹 Step 2: Register a LocationListListener
```kotlin
val listener = object : LocationListListener() {
    override fun onLocationListLoaded(hashMap: HashMap<Int, LocationInfo>) {
    // Handle received locations
    }

    override fun onLocationListFailed(error: Error) {
        // Handle failure
    }
}
locationListManager?.addLocationListListener(listener)
```

---

### 🔹 Step 3: Clean up
Always remove listeners when the activity is stopped:

```kotlin
locationListManager?.removeLocationListListener(listener)
```
And release the ViewBinding in onDestroy():

```kotlin
binding = null
```

---

### 📦 Files Involved
LocationListActivity.kt	Main activity handling SDK access and location updates
activity_location_list.xml	Layout file with a TextView for displaying results

---

### ⚠️ Notes
- Make sure Navigine.initialize(context) is already called in application class.
- Always set the user hash and server before using any SDK managers.