package com.navigine.locationviewcompose.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolygon
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.Point
import com.navigine.idl.java.Polygon
import com.navigine.image.ImageProvider
import com.navigine.locationview.NavigineLocation
import com.navigine.locationview.objects.circle.Circle
import com.navigine.locationview.objects.config.ModelConfig
import com.navigine.locationview.objects.config.Size
import com.navigine.locationview.objects.model.Model
import com.navigine.locationview.objects.polyline.DottedPolyline
import com.navigine.locationview.objects.polyline.Polyline
import com.navigine.locationviewcompose.Utils.LOC_ID
import com.navigine.locationviewcompose.Utils.SUBLOC_ID
import com.navigine.model.ModelProvider

@Composable
fun MapShapesDemo(modifier: Modifier = Modifier) {
    val line = LocationPolyline(
        com.navigine.idl.java.Polyline(arrayListOf(Point(1f, 1f), Point(2.5f, 4f), Point(5f, 4f))),
        LOC_ID, SUBLOC_ID
    )
    val poly = LocationPolygon(
        Polygon(arrayListOf(Point(1.5f, 1.2f), Point(2.8f, 3.8f), Point(5.2f, 2.1f))),
        LOC_ID, SUBLOC_ID
    )
    val context = LocalContext.current

    NavigineLocation(
        modifier = modifier,
        onWindowReady = { it.setSublocationId(SUBLOC_ID) }
    ) {
        Polyline(
            points = line,
            color = Color(0xFF0080FF),
            width = 3f
        )

        DottedPolyline(
            points = line,
            color = Color(0xFFFF3D00),
            dotSize = Size(4f,4f)
        )

        com.navigine.locationview.objects.polygon.Polygon(
            polygon = poly,
            color = Color(red = 0f, green = 1f, blue = 0f, alpha = 0.3f)
        )

        Circle(
            position = LocationPoint(Point(3f, 2.5f), LOC_ID, SUBLOC_ID),
            radius = 0.002f,
            color = Color.Yellow
        )

        val texture = ImageProvider.fromAsset(context, "texture.png")

        Model(
            LocationPoint(Point(3f, 2.5f), LOC_ID, SUBLOC_ID),
            model = ModelProvider.fromAsset(context, "FinalBaseMesh.obj", texture),
            config = ModelConfig(size = Size(20f,20f))
        )
    }
}