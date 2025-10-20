package com.navigine.naviginedemocompose.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.navigine.naviginedemocompose.R


@Composable
fun AdjustFab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    enabled: Boolean = true,
    contentDescription: String = "Режим коррекции"
) {

    val iconColor: Color = if (selected) {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.80f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
    }

    val containerColor: Color = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.80f)
    }

    val contentColor: Color = iconColor

    val disabledContainer = containerColor.copy(alpha = 0.38f)
    val disabledContent = contentColor.copy(alpha = 0.38f)

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
        ,
        shape = androidx.compose.foundation.shape.CircleShape, // corners=24dp при размере 48dp
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(),
        content = {
            Icon(
                painter = painterResource(R.drawable.ic_adjust_mode),
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}