package com.navigine.examples.SetupSdk

import android.app.Application
import android.content.Context
import android.util.Log
import com.navigine.idl.java.NavigineSdk
import com.navigine.sdk.Navigine

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initSdk(this)
    }

    /**
     * Initializes the Navigine SDK.
     *
     * @param context Application context required for initializing the SDK.
     *
     * Steps:
     * 1. Call `Navigine.initialize(context)` — this is mandatory before accessing SDK.
     * 2. Retrieve SDK instance via `NavigineSdk.getInstance()`.
     * 3. Set the server URL and user hash.
     * 4. Access available managers.
     *
     * NOTE: you should initialize the SDK only once and keep managers as singletons.
     * We do need sdk instance in every example, so these lines are commented out.
     */
    private fun initSdk(context: Context){
        val server = "https://ips.navigine.com"
        val hash = "0000-0000-0000-0000"
        try {
            Navigine.initialize(context)
//            NavigineSdk.getInstance().apply {
//                setServer(server)
//                setUserHash(hash)
//
//                val locManager = locationManager
//                val navManager = getNavigationManager(locManager)
//                val measureManager = getMeasurementManager(locManager)
//
//            }
        } catch (e: Exception) {
            Log.e("NavigineSdkManager", "Failed to initialize SDK", e)
        }
    }
}