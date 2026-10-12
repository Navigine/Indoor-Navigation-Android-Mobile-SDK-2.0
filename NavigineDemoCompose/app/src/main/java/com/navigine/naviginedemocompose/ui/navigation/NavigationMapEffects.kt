package com.navigine.naviginedemocompose.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.Placement
import com.navigine.idl.java.RouteEndpoint
import com.navigine.idl.java.RouteLayerListener
import com.navigine.idl.java.RouteLayerStatus
import com.navigine.idl.java.RouteProgress
import com.navigine.idl.java.RouteRemainingStyle
import com.navigine.idl.java.RouteView
import com.navigine.locationview.NavigineMapComposable
import com.navigine.locationview.effects.LocalLocationWindow

private const val ROUTE_WIDTH = 8f
private const val ROUTE_DOT_SIZE = 8f
private const val ROUTE_DOT_SPACING = 8f

@Composable
@NavigineMapComposable
fun NavigationRoute(
    target: MapPosition?,
    color: Color,
    onRouteChanged: (status: RouteLayerStatus, lengthMeters: Float?) -> Unit,
    onRouteAdvanced: (remainingMeters: Float, remainingSeconds: Float) -> Unit,
    onTargetReached: () -> Unit,
){
    val window = LocalLocationWindow.current ?: return

    val onChanged by rememberUpdatedState(onRouteChanged)
    val onAdvanced by rememberUpdatedState(onRouteAdvanced)
    val onReached by rememberUpdatedState(onTargetReached)

    val layer = remember(window) { NavigineSdk.getInstance().getRouteLayer(window) }

    DisposableEffect(layer) {
        val listener = object : RouteLayerListener() {
            override fun onRouteChanged(
                status: RouteLayerStatus,
                route: RouteView?
            ) {
                onChanged(status, route?.length())
            }

            override fun onRouteAdvanced(progress: RouteProgress?) {
                progress ?: return
                onAdvanced(progress.remainingDistance, progress.remainingTime)
            }

            override fun onRouteTargetReached() {
                onReached()
            }
        }

        layer.addRouteLayerListener(listener)
        layer.isVisible = true

        onDispose {
            layer.removeRouteLayerListener(listener)
            layer.clear()
        }
    }

    LaunchedEffect(layer, color) {
        layer.setWidth(ROUTE_WIDTH)
        layer.setRemainingStyle(RouteRemainingStyle.DOTTED)
        layer.setRemainingColor(color.toArgb())
        layer.setRemainingPointSize(ROUTE_DOT_SIZE, ROUTE_DOT_SIZE)
        layer.setRemainingPlacement(Placement.SPACED)
        layer.setRemainingPlacementSpacing(ROUTE_DOT_SPACING)
        layer.setRemainingPlacementMinRatio(0f)
        layer.setRemainingCollisionEnabled(false)
    }

    LaunchedEffect(layer, target) {
        if (target == null) {
            layer.clear()
        } else {
            layer.setTarget(RouteEndpoint(target.point, target.sublocationId))
        }
    }
}

@Composable
@NavigineMapComposable
fun ShownSublocation(sublocationId : Int?) {
    val window = LocalLocationWindow.current ?: return

    LaunchedEffect(window, sublocationId) {
        if (sublocationId != null) window.sublocationId = sublocationId
    }
}