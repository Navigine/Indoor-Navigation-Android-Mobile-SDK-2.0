package com.navigine.naviginedemocompose.ui.debug

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.navigine.naviginedemocompose.domain.model.TextRow
import com.navigine.naviginedemocompose.ui.composables.SensorsInfoCard
import com.navigine.naviginedemocompose.ui.composables.SignalSectionCard
import kotlinx.coroutines.launch

@Composable
fun DebugScreen(
    modifier: Modifier = Modifier,
    viewModel: DebugViewModel = hiltViewModel()
) {

    val ui = viewModel.uiState.collectAsState().value
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Scaffold { inner ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ui.info.forEachIndexed { idx, row ->
                        Text(
                            text = "${row.title}    ${row.value}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(
                                start = 16.dp, end = 16.dp,
                                top = if (idx == 0) 12.dp else 6.dp,
                                bottom = 6.dp
                            )
                        )
                        HorizontalDivider()
                    }
                }
            }

            item {
                SignalSectionCard(
                    section = ui.beacons,
                    expanded = ui.isExpanded(DebugSectionKey.BEACONS),
                    collapsedLimit = 6,
                    onToggle = { viewModel.toggle(DebugSectionKey.BEACONS) },
                    onCopy = {
                        scope.launch {
                            clipboard.setClipEntry(rowsToClipEntry(ui.beacons.rows))
                        }
                    }
                )
            }
            item {
                SignalSectionCard(
                    section = ui.ble,
                    expanded = ui.isExpanded(DebugSectionKey.BLE),
                    collapsedLimit = 6,
                    onToggle = { viewModel.toggle(DebugSectionKey.BLE) },
                    onCopy = {
                        scope.launch {
                            clipboard.setClipEntry(rowsToClipEntry(ui.ble.rows))
                        }
                    }
                )
            }
            item {
                SignalSectionCard(
                    section = ui.eddystone,
                    expanded = ui.isExpanded(DebugSectionKey.EDDYSTONE),
                    collapsedLimit = 6,
                    onToggle = { viewModel.toggle(DebugSectionKey.EDDYSTONE) },
                    onCopy = {
                        scope.launch {
                            clipboard.setClipEntry(rowsToClipEntry(ui.eddystone.rows))
                        }
                    }
                )
            }
            item {
                SignalSectionCard(
                    section = ui.wifi,
                    expanded = ui.isExpanded(DebugSectionKey.WIFI),
                    collapsedLimit = 6,
                    onToggle = { viewModel.toggle(DebugSectionKey.WIFI) },
                    onCopy = {
                        scope.launch {
                            clipboard.setClipEntry(rowsToClipEntry(ui.wifi.rows))
                        }
                    }
                )
            }
            item {
                SignalSectionCard(
                    section = ui.rtt,
                    expanded = ui.isExpanded(DebugSectionKey.RTT),
                    collapsedLimit = 6,
                    onToggle = { viewModel.toggle(DebugSectionKey.RTT) },
                    onCopy = {
                        scope.launch {
                            clipboard.setClipEntry(rowsToClipEntry(ui.rtt.rows))
                        }
                    }
                )
            }

            item {
                SensorsInfoCard(section = ui.sensors)
            }

        }
    }
}

private fun rowsToClipEntry(rows : List<TextRow>) : ClipEntry{
    val text = rows.joinToString("\n") { it.text }
    return ClipEntry(
        ClipData.newPlainText(
            text,
            text
        )
    )
}
