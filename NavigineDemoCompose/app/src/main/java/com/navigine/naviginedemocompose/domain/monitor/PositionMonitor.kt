package com.navigine.naviginedemocompose.domain.monitor

import com.navigine.idl.java.Position
import kotlinx.coroutines.flow.Flow

interface PositionMonitor {
    val positions: Flow<Position?>
}