package com.navigine.locationviewcompose.screens

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.navigine.idl.java.GlobalPoint
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.Point
import com.navigine.image.ImageProvider
import com.navigine.locationview.DefaultNavigineLocation
import com.navigine.locationview.clustering.ClusterController
import com.navigine.locationview.geometry.NavPosition
import com.navigine.locationview.objects.config.AppearanceConfig
import com.navigine.locationview.objects.config.ClusterConfig
import com.navigine.locationview.objects.config.IconConfig
import com.navigine.locationview.objects.config.PositionAnimationConfig
import com.navigine.locationview.objects.config.Size
import com.navigine.locationview.objects.icon.Icon
import com.navigine.locationview.objects.icon.rememberIconState
import com.navigine.locationviewcompose.R
import com.navigine.locationviewcompose.Utils.LOC_ID
import com.navigine.locationviewcompose.Utils.SUBLOC_ID
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Pin(val id: String, val position: NavPosition, val title: String)

private fun offsetPosition(
    window: LocationWindow,
    anchor: GlobalPoint,
    dxPixels: Float,
    dyPixels: Float,
): NavPosition? {
    val anchorScreen = runCatching { window.globalToScreenPosition(anchor, false) }.getOrNull()
        ?: return null
    val targetScreen = PointF(anchorScreen.x + dxPixels, anchorScreen.y + dyPixels)
    val targetGlobal = runCatching { window.screenPositionToGlobal(targetScreen) }.getOrNull()
        ?: return null
    return NavPosition.indoor(targetGlobal, SUBLOC_ID)
}

private val PIN_OFFSETS_PX = listOf(
    "a" to (20f to 20f), "b" to (22f to 21f), "c" to (24f to 22f),
    "d" to (40f to 30f), "e" to (42f to 31f), "f" to (44f to 32f),
    "g" to (60f to 60f), "h" to (62f to 61f), "i" to (64f to 62f),
    "j" to (80f to 80f), "k" to (82f to 81f), "l" to (84f to 82f),
)

@Composable
fun MapIconsList(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    var window by remember { mutableStateOf<LocationWindow?>(null) }
    var pins by remember { mutableStateOf<List<Pin>>(emptyList()) }
    var isMoving by remember { mutableStateOf(false) }

    LaunchedEffect(window) {
        val win = window ?: return@LaunchedEffect
        val anchor = win.camera?.point ?: return@LaunchedEffect
        pins = PIN_OFFSETS_PX.mapNotNull { (id, offset) ->
            val (dx, dy) = offset
            offsetPosition(win, anchor, dx, dy)?.let { Pin(id, it, id.uppercase()) }
        }
    }

    LaunchedEffect(isMoving) {
        while (isMoving) {
            delay(1_000)
            val win = window ?: continue
            val anchor = win.camera?.point ?: continue
            pins = pins.map { pin ->
                val dx = Random.nextFloat() * 30f
                val dy = Random.nextFloat() * 30f
                offsetPosition(win, anchor, dx, dy)?.let { pin.copy(position = it) } ?: pin
            }
        }
    }

    Column {
        DefaultNavigineLocation(
            modifier = modifier.weight(1f),
            onWindowReady = {
                it.setSublocationId(SUBLOC_ID)
                window = it
            }
        ) {

            val states = pins.map { pin ->
                key(pin.id) {
                    val state = rememberIconState()

                    Icon(
                        state = state,
                        position = pin.position,
                        animatePosition = true,
                        image = ImageProvider.fromResource(context, R.drawable.gun),
                        config = IconConfig(
                            size = Size(24f, 24f),
                            appearance = AppearanceConfig(title = pin.title),
                            animation = PositionAnimationConfig()
                        ),
                        onObjectReady = { id, _ ->
                            Log.d("Icon", "icon ${pin.id} -> sdkId=$id")
                        }
                    )

                    state
                }
            }

            ClusterController(
                icons = states,
                onClusterCreated = { cluster ->
                    Log.d("Cluster", "created id=${cluster.id}, count=${cluster.count}")
                },
                onClusterChanged = { cluster ->
                    Log.d("Cluster", "changed id=${cluster.id}, count=${cluster.count}")
                },
                onClusterDestroyed = { clusterId ->
                    Log.d("Cluster", "destroyed id=$clusterId")
                }
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                val win = window ?: return@Button
                val anchor = win.camera?.point ?: return@Button
                val nid = ('C'..'Z').random().toString()
                offsetPosition(win, anchor, Random.nextFloat() * 80f, Random.nextFloat() * 80f)
                    ?.let { pins = pins + Pin(nid, it, nid) }
            }) { Text("Add") }

            Button(onClick = { pins = pins.drop(1) }) { Text("Remove first") }

            Button(onClick = { isMoving = !isMoving }) { Text("Move random") }
        }
    }
}