package com.navigine.naviginedemocompose.ui.composables

import android.R.attr.visible
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.Point
import com.navigine.idl.java.Venue
import com.navigine.naviginedemocompose.core.util.format
import com.navigine.naviginedemocompose.ui.theme.NavigineDemoComposeTheme
import kotlin.math.roundToInt


@Composable
fun ArrivedSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onHeightChanged: (Dp) -> Unit = {},
) {
    NonModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier,
        onHeightChanged = onHeightChanged
    ) {
        Text("You’ve reached your destination", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Navigation ended", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MakeRouteSheet(
    visible: Boolean,
    onStart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    fromPoint: LocationPoint? = null,
    toPoint: LocationPoint? = null,
    toVenue: Venue? = null
) {
    NonModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        Text(
            "From",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            if (fromPoint != null) "Current position" else "Undefined",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "To",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        val toLabel = when {
            toVenue != null -> toVenue.name
            toPoint != null -> {
                val p = toPoint.point
                "Point (${p.x.format(2)}, ${p.y.format(2)})"
            }

            else -> "—"
        }
        Text(
            toLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Start") }
    }
}

@Composable
fun RouteInfoSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    distanceM: Float,
    etaSec: Int,
    modifier: Modifier = Modifier,
) {
    NonModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        val minutes = (etaSec / 60).coerceAtLeast(0)
        val lessThanOne = etaSec in 1..59
        Row(verticalAlignment = Alignment.CenterVertically) {
            val timeText = if (lessThanOne) "< 1 min" else "$minutes min"
            Text(
                text = "$timeText  (${distanceM.format(2)} m)",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun NonModalBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dragDismissThresholdPx: Float = 120f,
    onHeightChanged: (Dp) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    val density = LocalDensity.current
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    fun settle() {
        if (dragOffset > dragDismissThresholdPx) onDismiss()
        dragOffset = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Card(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .onGloballyPositioned {
                    sheetHeightPx = it.size.height
                    onHeightChanged(with(density) { sheetHeightPx.toDp() })
                }
                .offset { IntOffset(0, dragOffset.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, delta ->
                            dragOffset = (dragOffset + delta).coerceAtLeast(0f)
                        },
                        onDragEnd = { settle() },
                        onDragCancel = { dragOffset = 0f }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 4.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        .align(Alignment.TopCenter)
                )
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = onDismiss
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                content = content
            )
        }
    }
}

@Preview
@Composable
private fun MakeRouteSheetPreview() {
    NavigineDemoComposeTheme {
        MakeRouteSheet(
            visible = true,
            onStart = {},
            onDismiss = {},
            fromPoint = LocationPoint(Point(2f, 2f), 2074, 2562),
            toVenue = null
        )
    }
}

@Preview
@Composable
private fun MakeRouteSheetNoPointsPreview() {
    NavigineDemoComposeTheme {
        MakeRouteSheet(
            visible = true,
            onStart = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun RouteInfoSheetPreview() {
    NavigineDemoComposeTheme {
        RouteInfoSheet(visible = true, onDismiss = {}, distanceM = 123.4f, etaSec = 185)
    }
}
