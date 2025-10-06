package com.navigine.locationviewcompose.screens

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.navigine.locationview.NavigineLocation
import com.navigine.locationview.interaction.InputHandlers
import com.navigine.locationview.interaction.PickHandlers
import com.navigine.locationviewcompose.Utils.SUBLOC_ID

@Composable
fun MapInputPickDemo(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    NavigineLocation(
        modifier = modifier,
        onWindowReady = { it.setSublocationId(SUBLOC_ID) }
    ) {
        InputHandlers(
            autoPickObjectOnTap = true,
            autoPickFeatureOnTap = true,
        )
        PickHandlers(
            onObjectPicked = { result, viewPt ->
                Toast.makeText(context, "Object id=${result.mapObject} at ${result.point}", Toast.LENGTH_SHORT).show()
            },
            onFeaturePicked = { attrs, viewPt ->
                Toast.makeText(context, "Feature attrs=$attrs", Toast.LENGTH_SHORT).show()
            }
        )
    }
}