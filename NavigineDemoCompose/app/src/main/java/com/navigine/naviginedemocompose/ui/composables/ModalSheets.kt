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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

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