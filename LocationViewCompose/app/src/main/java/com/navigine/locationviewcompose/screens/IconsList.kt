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
import com.navigine.locationview.NavigineLocation
import com.navigine.locationview.objects.icon.Icon
import com.navigine.locationview.objects.icon.rememberIconState
import com.navigine.locationviewcompose.R
import com.navigine.locationviewcompose.Utils.LOC_ID
import com.navigine.locationviewcompose.Utils.SUBLOC_ID
import com.navigine.locationviewcompose.Utils.getBitmapFromImage
import kotlin.random.Random

data class Pin(val id: String, val lp: LocationPoint, val title: String)

@Composable
fun MapIconsList(modifier: Modifier = Modifier) {
    var pins by remember {
        mutableStateOf(
            listOf(
                Pin("a", LocationPoint(Point(2f, 2f), LOC_ID, SUBLOC_ID), "A"),
                Pin("b", LocationPoint(Point(4f, 3f), LOC_ID, SUBLOC_ID), "B"),
            )
        )
    }
    val context = LocalContext.current

    Column {
        NavigineLocation(
            modifier = modifier.weight(1f),
            onWindowReady = { it.setSublocationId(SUBLOC_ID) }
        ) {
            pins.forEach { pin ->
                key(pin.id) {
                    val st = rememberIconState()
                    Icon(
                        state = st,
                        position = pin.lp,
                        bitmap = getBitmapFromImage(context, R.drawable.gun),
                        sizeWidth = 24f, sizeHeight = 24f,
                        title = pin.title,
                        interactive = true,
                        visible = true,
                        onObjectReady = { id, _ ->
                            Log.d("Icon", "icon ${pin.id} -> sdkId=$id")
                        }
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val nid = ('C'..'Z').random().toString()
                pins = pins + Pin(nid,
                    LocationPoint(
                        Point(Random.nextFloat() * 8f, Random.nextFloat() * 8f),
                        LOC_ID,
                        SUBLOC_ID
                    ), nid)
            }) { Text("Add") }

            Button(onClick = {
                pins = pins.drop(1)
            }) { Text("Remove first") }
        }
    }
}