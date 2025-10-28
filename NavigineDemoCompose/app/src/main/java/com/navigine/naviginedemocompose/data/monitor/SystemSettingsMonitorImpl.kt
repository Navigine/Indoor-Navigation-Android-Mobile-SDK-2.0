package com.navigine.naviginedemocompose.data.monitor

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.navigine.naviginedemocompose.domain.monitor.SystemSettingsMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SystemSettingsMonitorImpl @Inject constructor(
    @ApplicationContext private val app: Context
) : SystemSettingsMonitor {

    private val _bt = MutableStateFlow(queryBluetooth())
    private val _loc = MutableStateFlow(queryLocation())

    override val bluetoothEnabled: StateFlow<Boolean> = _bt
    override val locationEnabled: StateFlow<Boolean> = _loc

    private val btReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                _bt.value = queryBluetooth()
            }
        }
    }

    private val locReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                _loc.value = queryLocation()
            }
        }
    }

    init {
        app.registerReceiver(
            btReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        app.registerReceiver(
            locReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )
        _bt.value = queryBluetooth()
        _loc.value = queryLocation()
    }

    private fun queryBluetooth(): Boolean {
        val mgr = app.getSystemService(BluetoothManager::class.java)
        val adapter = mgr?.adapter
        return adapter?.isEnabled == true
    }

    private fun queryLocation(): Boolean {
        val lm = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            runCatching {
                @Suppress("DEPRECATION")
                Settings.Secure.getInt(app.contentResolver, Settings.Secure.LOCATION_MODE) !=
                        Settings.Secure.LOCATION_MODE_OFF
            }.getOrDefault(false)
        }
    }
}