package com.navigine.locationviewcompose

import android.app.Application
import android.util.Log.e
import com.navigine.idl.java.NavigineSdk
import com.navigine.sdk.Navigine
import kotlin.apply

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Navigine.initialize(this)
    }
}