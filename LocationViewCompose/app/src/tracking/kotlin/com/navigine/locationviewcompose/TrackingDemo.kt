package com.navigine.locationviewcompose

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.navigine.idl.java.*
import com.navigine.locationview.tracking.DefaultTrackingNavigineLocation
import kotlinx.coroutines.delay

private const val TEST_EMAIL = "123@navigine.com"
private const val TEST_PASSWORD = "123"
private const val AUTH_SERVER = "https://ips.navigine.com"
private const val CLIENT_SERVER = "https://ips.navigine.com"
private const val TRACKING_URL = "https://rtls.navigine.com"

private const val APPLICATION_ID = 15
private const val LOCATION_ID = 12

private const val VISIBILITY_TEST_TAG = "TrackingVisibilityTest"

@Composable
internal fun TrackingDemo() {
    var liveDataManager by remember { mutableStateOf<LiveDataManager?>(null) }
    var objectListManager by remember { mutableStateOf<ObjectListManager?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }

    val sdk = remember { TrackingSdk.getInstance() }
    val navigineSdk = remember { NavigineSdk.getInstance() }
    val authManager = remember { sdk.authManager }
    val locationManager = remember { navigineSdk.locationManager }

    val authListener = remember {
        object : TrackingAuthListener() {
            override fun onTokenUpdated(token: String) {
                val appManager = sdk.getApplicationManager(authManager)
                val objects = sdk.getObjectListManager(authManager, appManager)
                val live = sdk.getLiveDataManager(authManager, appManager)
                objectListManager = objects
                liveDataManager = live
                appManager.setAppId(APPLICATION_ID)
                locationManager.locationId = LOCATION_ID
            }

            override fun onAuthError(err: java.lang.Error) {
                authError = err.message
            }
        }
    }

    DisposableEffect(authManager, authListener) {
        authManager.addAuthListener(authListener)
        onDispose { authManager.removeAuthListener(authListener) }
    }

    LaunchedEffect(Unit) {
        sdk.setConfig(Config(AUTH_SERVER, CLIENT_SERVER, TRACKING_URL))
        authManager.loginWithPassword(TEST_EMAIL, TEST_PASSWORD)
    }

    // --- visibility test harness ---
    var mapVisible by remember { mutableStateOf(true) }
    var autoCycleEnabled by remember { mutableStateOf(false) }
    var stressTestEnabled by remember { mutableStateOf(false) }
    var stressCycleCount by remember { mutableStateOf(0) }

    LaunchedEffect(autoCycleEnabled) {
        if (!autoCycleEnabled) return@LaunchedEffect
        while (true) {
            delay(3_000)
            mapVisible = false
            Log.d(VISIBILITY_TEST_TAG, "auto-cycle: hide (managers ready=${liveDataManager != null && objectListManager != null})")
            delay(2_000)
            mapVisible = true
            Log.d(VISIBILITY_TEST_TAG, "auto-cycle: show")
        }
    }

    LaunchedEffect(stressTestEnabled) {
        if (!stressTestEnabled) return@LaunchedEffect
        stressCycleCount = 0
        while (stressCycleCount < 50) {
            mapVisible = !mapVisible
            stressCycleCount++
            Log.d(VISIBILITY_TEST_TAG, "stress-cycle #$stressCycleCount: visible=$mapVisible")
            delay(150)
        }
        mapVisible = true
        stressTestEnabled = false
        Log.d(VISIBILITY_TEST_TAG, "stress-test finished, 50 cycles, no crash")
    }

    if (authError != null) {
        Text("Auth error: $authError")
        return
    }

    Column {
        DefaultTrackingNavigineLocation(
            modifier = Modifier.weight(1f),
            liveDataManager = liveDataManager,
            objectListManager = objectListManager,
            isVisible = mapVisible,
            onObjectClick = { info ->
                Log.d(VISIBILITY_TEST_TAG, "object click: ${info.`object`.id}")
            },
        )

        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { mapVisible = !mapVisible }) {
                Text(if (mapVisible) "Hide map" else "Show map")
            }

            Button(onClick = { autoCycleEnabled = !autoCycleEnabled }) {
                Text(if (autoCycleEnabled) "Stop auto-cycle" else "Start auto-cycle")
            }

            Button(
                onClick = { stressTestEnabled = !stressTestEnabled },
                enabled = !stressTestEnabled
            ) {
                Text(if (stressTestEnabled) "Stress: $stressCycleCount/50" else "Stress test")
            }
        }
    }

}