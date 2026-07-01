package com.navigine.locationviewcompose.screens

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
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.Point
import com.navigine.image.ImageProvider
import com.navigine.locationview.DefaultNavigineLocation
import com.navigine.locationview.clustering.ClusterController
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

data class Pin(val id: String, val lp: LocationPoint, val title: String)

@Composable
fun MapIconsList(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    var pins by remember {
        mutableStateOf(
            listOf(
                Pin("a", mapPoint(2f, 2f), "A"),
                Pin("b", mapPoint(2.2f, 2.1f), "B"),
                Pin("c", mapPoint(2.4f, 2.2f), "C"),
                Pin("d", mapPoint(4f, 3f), "D"),
                Pin("e", mapPoint(4.2f, 3.1f), "E"),
                Pin("f", mapPoint(4.4f, 3.2f), "F"),
                Pin("g", mapPoint(6f, 6f), "G"),
                Pin("h", mapPoint(6.2f, 6.1f), "H"),
                Pin("i", mapPoint(6.4f, 6.2f), "I"),
                Pin("j", mapPoint(8f, 8f), "J"),
                Pin("k", mapPoint(8.2f, 8.1f), "K"),
                Pin("l", mapPoint(8.4f, 8.2f), "L"),
            )
        )
    }

    var isMoving by remember { mutableStateOf(false) }

    LaunchedEffect(isMoving) {
        while (isMoving) {
            delay(1_000)
            pins = pins.map { pin ->
                pin.copy(lp = randomMapPoint())
            }
        }
    }

    Column {
        DefaultNavigineLocation(
            modifier = modifier.weight(1f),
            onWindowReady = { it.setSublocationId(SUBLOC_ID) }
        ) {

            val states = pins.map { pin ->
                key(pin.id) {
                    val state = rememberIconState()

                    Icon(
                        state = state,
                        position = pin.lp,
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
                    Log.d(
                        "Cluster",
                        "created id=${cluster.id}, count=${cluster.count}"
                    )
                },
                onClusterChanged = { cluster ->
                    Log.d(
                        "Cluster",
                        "changed id=${cluster.id}, count=${cluster.count}"
                    )
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
                val nid = ('C'..'Z').random().toString()
                pins = pins + Pin(
                    nid,
                    LocationPoint(
                        Point(Random.nextFloat() * 8f, Random.nextFloat() * 8f),
                        LOC_ID,
                        SUBLOC_ID
                    ), nid
                )
            }) { Text("Add") }

            Button(onClick = {
                pins = pins.drop(1)
            }) { Text("Remove first") }

            Button(
                onClick = {
                    isMoving = !isMoving
                }
            ) {
                Text("Move random")
            }
        }
    }
}

private fun mapPoint(x: Float, y: Float): LocationPoint {
    return LocationPoint(
        Point(x, y),
        LOC_ID,
        SUBLOC_ID
    )
}

private fun randomMapPoint(): LocationPoint {
    return mapPoint(
        x = Random.nextFloat() * 3f,
        y = Random.nextFloat() * 3f
    )
}