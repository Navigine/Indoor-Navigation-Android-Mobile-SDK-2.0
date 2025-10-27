package com.navigine.naviginedemocompose.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.navigine.idl.java.Sublocation


@Composable
fun SublocationsList(
    sublocations: List<Sublocation>,
    onSublocationClick: (Sublocation) -> Unit,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 3

) {
    var selectedItem by rememberSaveable { mutableIntStateOf(if (sublocations.isNotEmpty()) 0 else -1) }

    LazyColumn(
        modifier = modifier.height((48 * maxVisibleItems).dp)
    ) {
        items(sublocations.size) { index ->
            val item = sublocations[index]
            SubLocationListItem(
                modifier = Modifier.alpha(if (index == selectedItem) 0.9f else 0.75f),
                name = item.levelId,
                color = MaterialTheme.colorScheme.secondary.takeIf { index == selectedItem } ?: Color.White,
                onClick = {
                    selectedItem = index
                    onSublocationClick(item)
                }
            )
        }
    }
}

@Composable
fun SubLocationListItem(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    Card(
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                onClick = onClick
            ),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
            Text(
                text = name,
                modifier = modifier.padding(5.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}