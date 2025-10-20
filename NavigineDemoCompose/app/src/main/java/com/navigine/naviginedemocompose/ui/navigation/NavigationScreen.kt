package com.navigine.naviginedemocompose.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.navigine.idl.java.AnimationType
import com.navigine.idl.java.Camera
import com.navigine.locationview.ExperimentalNavigineApi
import com.navigine.locationview.NavigineLocation
import com.navigine.locationview.camera.rememberNavCameraPositionState
import com.navigine.locationview.interaction.InputHandlers
import com.navigine.locationview.objects.icon.Icon
import com.navigine.locationview.objects.icon.rememberIconState
import com.navigine.naviginedemocompose.R
import com.navigine.naviginedemocompose.core.util.getBitmapFromImage
import com.navigine.naviginedemocompose.ui.composables.AdjustFab
import com.navigine.naviginedemocompose.ui.composables.SublocationsList
import com.navigine.naviginedemocompose.ui.composables.ZoomPanel

@OptIn(ExperimentalNavigineApi::class)
@Composable
fun NavigationScreen(
    viewModel: NavigationViewModel = hiltViewModel(),
    isVisible: Boolean
) {
    val state = viewModel.state.collectAsState()
    val mapKey by viewModel.mapRecomposeKey.collectAsState()

    val cam = rememberNavCameraPositionState()
    val positionIconState = rememberIconState()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NavigationEffect.ApplyVenueLayerFilter -> {}
                is NavigationEffect.DrawRoute -> {}
                NavigationEffect.HideRoute -> {}
                is NavigationEffect.MoveCameraToPoint -> cam.flyTo(
                    Camera(
                        effect.point,
                        cam.position?.zoom ?: 10f,
                        cam.position?.rotation ?: 1f
                    )
                )

                is NavigationEffect.ShowToast -> Toast.makeText(
                    ctx,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            AdjustFab(
                selected = state.value.followMyLocation,
                enabled = state.value.position != null,
                onClick = {
                    viewModel.onEvent(NavigationEvent.FollowMyLocationToggle(!state.value.followMyLocation))
                }
            )
        }
    ) { padding ->
        if (state.value.loadingVisible)
            CircularProgressIndicator()
        else
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                key(mapKey) {
                    NavigineLocation(
                        isVisible = isVisible,
                        cameraPositionState = cam,
                        onWindowReady = {
                            viewModel.onEvent(NavigationEvent.WindowReady(it))
                        }
                    ) {
                        InputHandlers(
                            onTap = { point, meters ->
                                viewModel.onEvent(NavigationEvent.CancelPin)
                            },
                            onLongTap = { point, meters ->
                                viewModel.onEvent(NavigationEvent.LongPressAt(point, meters))
                            }
                        )

                        state.value.position?.let { pos ->
                            Icon(
                                position = pos.locationPoint,
                                bitmap = getBitmapFromImage(ctx, R.drawable.ic_current_point),
                                state = positionIconState
                            )
                            positionIconState.mapObject?.setPositionAnimated(
                                pos.locationPoint, 1f,
                                AnimationType.CUBIC
                            )
                        }

                        state.value.pinPoint?.let { pin ->
                            Icon(
                                position = pin,
                                bitmap = getBitmapFromImage(ctx, R.drawable.ic_pin_point)
                            )
                        }

                    }
                }
                SublocationsList(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp, top = 24.dp),
                    sublocations = state.value.location?.location?.sublocations ?: emptyList(),
                    onSublocationClick = { viewModel.onEvent(NavigationEvent.SwitchFloor(it.id)) }
                )
                ZoomPanel(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, top = 24.dp),
                    onZoomIn = { cam.moveZoomTo((cam.zoomFactor ?: 9f) * 2f) },
                    onZoomOut = { cam.moveZoomTo((cam.zoomFactor ?: 9f) / 2f) }
                )

            }
    }
}