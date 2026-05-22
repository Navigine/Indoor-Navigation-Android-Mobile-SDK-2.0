package com.navigine.naviginedemocompose.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.navigine.naviginedemocompose.ui.theme.NavigineDemoComposeTheme
import com.navigine.naviginedemocompose.ui.theme.spacing

/** Primary brand button (filled). */
@Composable
fun AppButtonPrimary(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        ),
        shape = shape
    ) { content() }
}

/** Secondary (tonal) button. */
@Composable
fun AppButtonTonal(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    content: @Composable () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg),
        shape = shape,
        colors = colors
    ) { content() }
}

/** Tertiary (outlined). */
@Composable
fun AppButtonOutlined(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg),
        shape = shape,
        border = ButtonDefaults.outlinedButtonBorder().copy(width = 1.dp)
    ) { content() }
}

@Preview
@Composable
private fun AppButtonsPreview() {
    NavigineDemoComposeTheme {
        Column {
            AppButtonPrimary(onClick = { }) {
                Text(text = "Primary Button")
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            AppButtonPrimary(onClick = { }, enabled = false) {
                Text(text = "Primary Disabled")
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            AppButtonTonal(onClick = { }) {
                Text(text = "Tonal Button")
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            AppButtonTonal(onClick = { }, enabled = false) {
                Text(text = "Tonal Disabled")
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

            AppButtonOutlined(onClick = { }) {
                Text(text = "Outlined Button")
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            AppButtonOutlined(onClick = { }, enabled = false) {
                Text(text = "Outlined Disabled")
            }
        }
    }
}
