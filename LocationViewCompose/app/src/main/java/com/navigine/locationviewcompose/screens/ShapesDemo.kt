package com.navigine.locationviewcompose.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolygon
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.Point
import com.navigine.idl.java.Polygon
import com.navigine.locationview.NavigineLocation
import com.navigine.locationview.objects.circle.Circle
import com.navigine.locationview.objects.polyline.DottedPolyline
import com.navigine.locationview.objects.polyline.Polyline
import com.navigine.locationviewcompose.Utils.LOC_ID
import com.navigine.locationviewcompose.Utils.SUBLOC_ID

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

    NavigineLocation(
        modifier = modifier,
        onWindowReady = { it.setSublocationId(SUBLOC_ID) }
    ) {
        Polyline(
            locationPolyline = line,
            fillColor = Color(0xFF0080FF),
            width = 3f
        )

        DottedPolyline(
            locationPolyline = line,
            color = Color(0xFFFF3D00),
            sizeWidth = 4f,
            sizeHeight = 4f,
            placementSpacing = 8f
        )

        com.navigine.locationview.objects.polygon.Polygon(
            polygon = poly,
            fillColor = Color(red = 0f, green = 1f, blue = 0f, alpha = 0.3f),
            order = 5,
            interactive = true
        )

        Circle(
            position = LocationPoint(Point(3f, 2.5f), LOC_ID, SUBLOC_ID),
            radius = 0.002f,
            colorArgb = 0x550000FF
        )
    }
}