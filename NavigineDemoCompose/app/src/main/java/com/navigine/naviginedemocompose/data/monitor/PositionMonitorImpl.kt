package com.navigine.naviginedemocompose.data.monitor

import android.util.Log
import com.navigine.idl.java.Position
import com.navigine.idl.java.PositionListener
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import com.navigine.naviginedemocompose.domain.monitor.PositionMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.lang.Error
import javax.inject.Inject

class PositionMonitorImpl @Inject constructor(
    private val sdk: NavigineSdkManager
) : PositionMonitor {

    override val positions: Flow<Position?> =
        sdk.state.flatMapLatest { state ->
            if(state is NavigineSdkManager.SdkState.Ready){
                callbackFlow {
                    val nav = sdk.navigationManager
                    val listener = object : PositionListener() {
                        override fun onPositionUpdated(pos: Position) {
                            trySend(pos)
                        }
                        override fun onPositionError(err: Error?) {
                            trySend(null)
                            Log.d("PositionMonitor", "onPositionError: $err")
                        }
                    }
                    nav.addPositionListener(listener)
                    awaitClose { nav.removePositionListener(listener) }
                }
            } else {
                emptyFlow()
            }
        }
}