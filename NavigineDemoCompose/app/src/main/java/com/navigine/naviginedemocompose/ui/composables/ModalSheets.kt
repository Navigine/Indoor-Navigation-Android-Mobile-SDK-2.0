package com.navigine.naviginedemocompose.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.navigine.locationview.settings.LocationUiSettings
import com.navigine.naviginedemocompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenueModalSheet(
    visible: Boolean,
    venue: com.navigine.idl.java.Venue?,
    canRoute: Boolean,
    onClose: () -> Unit,
    onRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible || venue == null) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        dragHandle = { DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = venue.name ?: "Venue",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(
                        painterResource(com.navigine.naviginedemocompose.R.drawable.ic_close),
                        contentDescription = "Close"
                    )
                }
            }

             runCatching { venue.descript }.getOrNull()?.takeIf { it.isNotBlank() }?.let { desc ->
                 Spacer(Modifier.height(8.dp))
                 Text(desc, style = MaterialTheme.typography.bodyMedium)
             }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = venue.imageUrl,
                    contentDescription = "Venue image",
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(com.navigine.naviginedemocompose.R.drawable.ic_venue_placeholder),
                    error = painterResource(com.navigine.naviginedemocompose.R.drawable.ic_venue_placeholder)
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onRoute,
                enabled = canRoute,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Route")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSettingsSheet(
    visible: Boolean,
    settings: LocationUiSettings,
    onDismiss: () -> Unit,
    onSettingsChanged: (LocationUiSettings) -> Unit,
) {
    if (!visible) return

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.map_settings_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            MapSettingSwitch(
                label = stringResource(R.string.map_settings_rotate),
                checked = settings.rotateGesturesEnabled,
                onCheckedChange = { onSettingsChanged(settings.copy(rotateGesturesEnabled = it)) }
            )
            MapSettingSwitch(
                label = stringResource(R.string.map_settings_tilt),
                checked = settings.tiltGesturesEnabled,
                onCheckedChange = { enabled ->
                    val updated = settings.copy(
                        tiltGesturesEnabled = enabled,
                        is3dEnabled = if (!enabled) false else settings.is3dEnabled
                    )
                    onSettingsChanged(updated)
                }
            )
            MapSettingSwitch(
                label = stringResource(R.string.map_settings_scroll),
                checked = settings.scrollGesturesEnabled,
                onCheckedChange = { onSettingsChanged(settings.copy(scrollGesturesEnabled = it)) }
            )
            MapSettingSwitch(
                label = stringResource(R.string.map_settings_zoom),
                checked = settings.zoomGesturesEnabled,
                onCheckedChange = { onSettingsChanged(settings.copy(zoomGesturesEnabled = it)) }
            )
            MapSettingSwitch(
                label = stringResource(R.string.map_settings_3d),
                checked = settings.is3dEnabled,
                enabled = settings.tiltGesturesEnabled,
                onCheckedChange = { enabled ->
                    val updated = settings.copy(
                        is3dEnabled = enabled,
                        tiltGesturesEnabled = if (enabled) true else settings.tiltGesturesEnabled
                    )
                    onSettingsChanged(updated)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MapSettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}