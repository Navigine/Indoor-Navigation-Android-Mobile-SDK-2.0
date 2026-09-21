package com.navigine.locationviewcompose.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.navigine.idl.java.GlobalPoint
import com.navigine.idl.java.LocationPolygon
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.LocationWindow
import com.navigine.idl.java.MapTheme
import com.navigine.idl.java.OperatingMode
import com.navigine.image.ImageProvider
import com.navigine.locationview.NavigineLocation
import com.navigine.locationview.camera.rememberNavCameraPositionState
import com.navigine.locationview.geometry.NavPosition
import com.navigine.locationview.objects.circle.Circle
import com.navigine.locationview.objects.config.ModelConfig
import com.navigine.locationview.objects.config.Size
import com.navigine.locationview.objects.model.Model
import com.navigine.locationview.objects.polyline.DottedPolyline
import com.navigine.locationview.objects.polyline.Polyline
import com.navigine.locationview.settings.OutdoorMapConfig
import com.navigine.locationviewcompose.Utils.SUBLOC_ID
import com.navigine.model.ModelProvider
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlin.collections.firstOrNull

private val LINE_OFFSETS_PX = listOf(0f to 0f, 30f to 60f, 80f to 60f)
private val POLY_OFFSETS_PX = listOf(10f to 4f, 26f to 56f, 84f to 18f)
private val CIRCLE_OFFSET_PX = 60f to 10f
private val MODEL_OFFSET_PX = 60f to 10f

private fun offsetsToGlobal(
    window: LocationWindow,
    anchor: GlobalPoint,
    offsetsPx: List<Pair<Float, Float>>,
): List<GlobalPoint>? {
    val anchorScreen = runCatching { window.globalToScreenPosition(anchor, false) }.getOrNull()
        ?: return null
    return offsetsPx.map { (dx, dy) ->
        val screen = android.graphics.PointF(anchorScreen.x + dx, anchorScreen.y + dy)
        runCatching { window.screenPositionToGlobal(screen) }.getOrNull() ?: return null
    }
}

private data class ShapesGeometry(
    val line: LocationPolyline,
    val poly: LocationPolygon,
    val circlePoint: GlobalPoint,
    val modelPoint: GlobalPoint,
)

@Composable
fun MapShapesDemo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cam = rememberNavCameraPositionState()
    var window by remember { mutableStateOf<LocationWindow?>(null) }
    var operatingMode by remember { mutableStateOf(OperatingMode.INDOOR_ONLY) }
    var outdoorTheme by remember { mutableStateOf(MapTheme.LIGHT) }

    Column {
        NavigineLocation(
            modifier = modifier.weight(1f),
            cameraPositionState = cam,
            operatingMode = operatingMode,
            outdoorMapConfig = OutdoorMapConfig(theme = outdoorTheme),
            onWindowReady = {
                it.setSublocationId(SUBLOC_ID)
                window = it
            }
        ) {
            val win = window ?: return@NavigineLocation

            var computed by remember { mutableStateOf<ShapesGeometry?>(null) }

            LaunchedEffect(win) {
                val anchor = snapshotFlow { cam.position }.filterNotNull().first().point
                val linePoints = offsetsToGlobal(win, anchor, LINE_OFFSETS_PX) ?: return@LaunchedEffect
                val polyPoints = offsetsToGlobal(win, anchor, POLY_OFFSETS_PX) ?: return@LaunchedEffect
                val circlePoint = offsetsToGlobal(win, anchor, listOf(CIRCLE_OFFSET_PX))?.firstOrNull()
                    ?: return@LaunchedEffect
                val modelPoint = offsetsToGlobal(win, anchor, listOf(MODEL_OFFSET_PX))?.firstOrNull()
                    ?: return@LaunchedEffect

                computed = ShapesGeometry(
                    line = LocationPolyline(ArrayList(linePoints), SUBLOC_ID),
                    poly = LocationPolygon(ArrayList(polyPoints), SUBLOC_ID),
                    circlePoint = circlePoint,
                    modelPoint = modelPoint,
                )
            }

            val geometry = computed ?: return@NavigineLocation

            Polyline(points = geometry.line, color = Color(0xFF0080FF), width = 3f)
            DottedPolyline(points = geometry.line, color = Color(0xFFFF3D00), dotSize = Size(4f, 4f))
            com.navigine.locationview.objects.polygon.Polygon(
                polygon = geometry.poly,
                color = Color(red = 0f, green = 1f, blue = 0f, alpha = 0.3f)
            )
            Circle(
                position = NavPosition.indoor(geometry.circlePoint, SUBLOC_ID),
                radius = 0.002f,
                color = Color.Yellow
            )

            val texture = ImageProvider.fromAsset(context, "texture.png")
            Model(
                position = NavPosition.indoor(geometry.modelPoint, SUBLOC_ID),
                model = ModelProvider.fromAsset(context, "FinalBaseMesh.obj", texture),
                config = ModelConfig(size = Size(20f, 20f))
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { operatingMode = OperatingMode.INDOOR_ONLY }) {
                Text("Indoor")
            }
            Button(onClick = { operatingMode = OperatingMode.OUTDOOR }) {
                Text("Outdoor")
            }
            Button(onClick = { operatingMode = OperatingMode.OUTDOOR_INDOOR }) {
                Text("Both")
            }
            Button(onClick = {
                outdoorTheme = if (outdoorTheme == MapTheme.LIGHT) MapTheme.DARK else MapTheme.LIGHT
            }) {
                Text("Theme: $outdoorTheme")
            }
        }
    }
}