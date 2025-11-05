package com.navigine.naviginedemocompose

import android.app.Application
import android.util.Log
import com.navigine.naviginedemocompose.core.util.Constants.TAG
import com.navigine.sdk.Navigine
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            Navigine.initialize(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Navigine SDK", e)
        }
    }
}