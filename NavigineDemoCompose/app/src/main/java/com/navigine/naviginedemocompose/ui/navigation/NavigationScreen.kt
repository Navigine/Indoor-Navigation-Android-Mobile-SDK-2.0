package com.navigine.naviginedemocompose.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.navigine.idl.java.Camera
import com.navigine.idl.java.OperatingMode
import com.navigine.image.ImageProvider
import com.navigine.locationview.DefaultNavigineLocation
import com.navigine.locationview.ExperimentalNavigineApi
import com.navigine.locationview.camera.rememberNavCameraPositionState
import com.navigine.locationview.effects.LocalLocationWindow
import com.navigine.locationview.geometry.NavPosition
import com.navigine.locationview.interaction.InputHandlers
import com.navigine.locationview.interaction.PickHandlers
import com.navigine.locationview.objects.config.DottedPolylineConfig
import com.navigine.locationview.objects.config.PlacementConfig
import com.navigine.locationview.objects.config.Size
import com.navigine.locationview.objects.icon.Icon
import com.navigine.locationview.objects.icon.rememberIconState
import com.navigine.locationview.objects.polyline.DottedPolyline
import com.navigine.locationview.settings.DefaultLocationProperties
import com.navigine.locationview.settings.LocationProperties
import com.navigine.naviginedemocompose.R
import com.navigine.naviginedemocompose.core.util.copy
import com.navigine.naviginedemocompose.core.util.getBitmapFromImage
import com.navigine.naviginedemocompose.core.util.venueName
import com.navigine.naviginedemocompose.ui.composables.ArrivedSheet
import com.navigine.naviginedemocompose.ui.composables.MakeRouteSheet
import com.navigine.naviginedemocompose.ui.composables.MapSettingsSheet
import com.navigine.naviginedemocompose.ui.composables.RouteInfoSheet
import com.navigine.naviginedemocompose.ui.composables.VenueModalSheet
import com.navigine.naviginedemocompose.ui.theme.extendedColors

private const val DEFAULT_CAMERA_ZOOM = 10f
private const val GLOBAL_MIN_ZOOM_FACTOR = 0.01f

@OptIn(ExperimentalNavigineApi::class)
@Composable
fun NavigationScreen(
    viewModel: NavigationViewModel = hiltViewModel(),
    isVisible: Boolean
) {
    val state = viewModel.state.collectAsState().value
    val mapKey by viewModel.mapRecomposeKey.collectAsState()

    val cam = rememberNavCameraPositionState()
    var indoorProperties by remember { mutableStateOf(DefaultLocationProperties) }
    val mapProperties = if (state.isGlobalMapEnabled) {
        indoorProperties.copy(minZoomFactor = GLOBAL_MIN_ZOOM_FACTOR, stickToBorder = false)
    } else {
        indoorProperties
    }
    val operatingMode = if (state.isGlobalMapEnabled) OperatingMode.OUTDOOR_INDOOR else OperatingMode.INDOOR_ONLY

    val snackbarHostState = remember { SnackbarHostState() }

    val ctx = LocalContext.current
    val pinImage = remember(ctx) { ImageProvider.fromResource(ctx, R.drawable.ic_pin_point) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NavigationEffect.ApplyVenueLayerFilter -> Unit
                is NavigationEffect.MoveCameraToPoint -> cam.flyTo(
                    Camera(
                        effect.point,
                        cam.position?.zoom ?: DEFAULT_CAMERA_ZOOM,
                        cam.position?.rotation ?: 0f,
                        cam.position?.tilt ?: 0f
                    )
                )

                is NavigationEffect.ShowToast -> Toast.makeText(
                    ctx,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()
                is NavigationEffect.ShowSnackbar -> { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (state.loadingVisible)
            CircularProgressIndicator()
        else
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding.copy(bottom = 0.dp))
            ) {
                key(mapKey) {
                    DefaultNavigineLocation(
                        isVisible = isVisible,
                        cameraPositionState = cam,
                        uiSettings = state.locationUiSettings,
                        properties = mapProperties,
                        operatingMode = operatingMode,
                        onWindowReady = { window ->
                            indoorProperties = LocationProperties(
                                minZoomFactor = window.minZoomFactor,
                                stickToBorder = window.stickToBorder,
                            )
                        },
                    ) {
                        val window = LocalLocationWindow.current

                        InputHandlers(
                            autoPickFeatureOnTap = true,
                            onTap = { _, _ -> viewModel.onEvent(NavigationEvent.MapTapped) },
                            onLongTap = { _, global ->
                                val sublocationId = window?.sublocationId ?: return@InputHandlers
                                viewModel.onEvent(NavigationEvent.LongPressAt(global, sublocationId))
                            }
                        )

                        PickHandlers(
                            onFeaturePicked = { attrs, viewPoint ->
                                val name = attrs.venueName() ?: return@PickHandlers
                                val sublocationId = window?.sublocationId ?: return@PickHandlers
                                viewModel.onEvent(NavigationEvent.VenueFeaturePicked(name, sublocationId))
                            }
                        )

                        ShownSublocation(state.requestedSublocationId)

                        state.toPin?.let { pin ->
                            Icon(
                                position = pin.toNavPosition(),
                                image = pinImage
                            )
                        }

                        NavigationRoute(
                            target = state.routeTarget,
                            color = MaterialTheme.extendedColors.success,
                            onRouteChanged = { status, length ->
                                viewModel.onEvent(NavigationEvent.RouteChanged(status, length))
                            },
                            onRouteAdvanced = { meters, seconds ->
                                viewModel.onEvent(NavigationEvent.RouteAdvanced(meters, seconds))
                            },
                            onTargetReached = { viewModel.onEvent(NavigationEvent.RouteTargetReached) }
                        )

                    }
                }

                FloatingActionButton(
                    onClick = { viewModel.onEvent(NavigationEvent.ShowMapSettings) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 48.dp),
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_map_layers),
                        contentDescription = stringResource(R.string.map_settings_title)
                    )
                }

                MakeRouteSheet(
                    visible = state.makeRouteSheetVisible,
                    onStart = { viewModel.onEvent(NavigationEvent.BuildRoute)},
                    onDismiss = {
                        viewModel.onEvent(NavigationEvent.HideMakeRouteSheet)
                        viewModel.onEvent(NavigationEvent.CancelPin)
                                },
                    fromPoint = state.fromPoint,
                    toPoint = state.toPoint,
                    toVenue = state.toVenue
                )

                RouteInfoSheet(
                    visible = state.routeInfoVisible,
                    onDismiss = {
                        viewModel.onEvent(NavigationEvent.HideRouteInfo)
                        viewModel.onEvent(NavigationEvent.CancelRoute)
                                },
                    distanceM = state.routeDistanceMeters ?: 0f,
                    etaSec = state.etaSeconds ?: 0
                )
                ArrivedSheet(
                    visible = state.isFinishNear,
                    onDismiss = {
                        viewModel.onEvent(NavigationEvent.HideFinish)
                        viewModel.onEvent(NavigationEvent.CancelPin)
                                },
                )
                VenueModalSheet(
                    visible = state.venueSheet?.isVisible == true,
                    venue = state.venueSheet?.venue,
                    canRoute = state.position != null,
                    onClose = { viewModel.onEvent(NavigationEvent.HideVenueSheet) },
                    onRoute = {
                        viewModel.onEvent(NavigationEvent.OnRouteVenue)
                        viewModel.onEvent(NavigationEvent.HideVenueSheet)
                    }
                )
                MapSettingsSheet(
                    visible = state.mapSettingsSheetVisible,
                    settings = state.locationUiSettings,
                    globalMapEnabled = state.isGlobalMapEnabled,
                    onDismiss = { viewModel.onEvent(NavigationEvent.HideMapSettings) },
                    onSettingsChanged = { viewModel.onEvent(NavigationEvent.MapSettingsChanged(it)) },
                    onGlobalMapChanged = { viewModel.onEvent(NavigationEvent.GlobalMapToggled(it))}
                )
            }
    }
}

private fun MapPosition.toNavPosition() = NavPosition(point, sublocationId)