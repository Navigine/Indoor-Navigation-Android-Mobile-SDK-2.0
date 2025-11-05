package com.navigine.naviginedemocompose.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.navigine.naviginedemocompose.R


@Composable
fun ZoomPanel(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.then(modifier)) {
        ZoomItem(icon = painterResource(id = R.drawable.ic_zoom_in_24)) { onZoomIn() }
        ZoomItem(icon = painterResource(id = R.drawable.ic_zoom_out_24)) { onZoomOut() }
    }
}


@Composable
private fun ZoomItem(icon: Painter, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White
        )
    ) {
        Icon(painter = icon, contentDescription = "zoom button")
    }
}
