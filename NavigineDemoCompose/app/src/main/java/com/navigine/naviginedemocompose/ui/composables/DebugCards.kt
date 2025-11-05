package com.navigine.naviginedemocompose.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.navigine.naviginedemocompose.core.util.format
import com.navigine.naviginedemocompose.domain.model.Section

@Composable
fun SignalSectionCard(
    section: Section,
    expanded: Boolean,
    collapsedLimit: Int,
    onToggle: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleRows = if (expanded) section.rows else section.rows.take(collapsedLimit)
    ElevatedCard(
        modifier = modifier.animateContentSize(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        onCopy()
                    }
                )
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "${section.title} (${section.counter}), entries/sec: ${section.entriesPerSec.format(1)}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (visibleRows.isEmpty()) {
                Text("---", style = MaterialTheme.typography.bodyMedium)
            } else {
                visibleRows.forEach { row ->
                    Text(row.text, style = MaterialTheme.typography.bodyMedium)
                }
                if (!expanded && section.rows.size > visibleRows.size) {
                    Text(
                        text = "…${section.rows.size - visibleRows.size} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SensorsInfoCard(
    section: Section,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (section.rows.isEmpty()) {
                Text("---", style = MaterialTheme.typography.bodyMedium)
            } else {
                section.rows.forEach { row ->
                    Text(
                        text = row.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}