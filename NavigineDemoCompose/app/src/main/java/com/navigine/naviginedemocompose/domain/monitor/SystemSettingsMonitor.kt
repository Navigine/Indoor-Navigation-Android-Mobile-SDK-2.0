package com.navigine.naviginedemocompose.domain.monitor

import kotlinx.coroutines.flow.StateFlow

interface SystemSettingsMonitor {
    val bluetoothEnabled: StateFlow<Boolean>
    val locationEnabled: StateFlow<Boolean>
}