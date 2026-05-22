package com.navigine.locationviewcompose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.navigine.idl.java.AnimationType
import com.navigine.idl.java.Camera
import com.navigine.idl.java.Point
import com.navigine.locationview.NavigineLocation
import com.navigine.locationview.camera.rememberNavCameraPositionState
import com.navigine.locationview.settings.LocationProperties
import com.navigine.locationview.settings.LocationUiSettings
import com.navigine.locationviewcompose.Utils.SUBLOC_ID

@Composable
fun MapCameraDemo(modifier: Modifier = Modifier) {
    val cam = rememberNavCameraPositionState()

    Column {
        NavigineLocation(
            modifier = modifier.weight(1f),
            cameraPositionState = cam,
            onWindowReady = { it.sublocationId = SUBLOC_ID },
            properties = LocationProperties(sublocationId = SUBLOC_ID),
            uiSettings = LocationUiSettings(is3dEnabled = true)
        ) { /* content optional */ }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val target = Camera(Point(3f, 2f), 8f, 0f, 0f)
                cam.move(target)
            }) { Text("Move") }

            Button(onClick = {
                val target = Camera(Point(6f, 5f), 10f, 0f, 0f)
                cam.animateTo(target, durationMs = 800, type = AnimationType.QUINT)
            }) { Text("Animate") }

            Button(onClick = { cam.moveZoomTo((cam.zoomFactor ?: 9f) + 1f) }) { Text("Zoom+") }
            Button(onClick = { cam.moveZoomTo((cam.zoomFactor ?: 9f) - 1f) }) { Text("Zoom-") }
        }
    }
}