package com.navigine.naviginedemocompose.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.navigine.naviginedemocompose.ui.auth.ServerCheckUi

@Composable
fun ServerStateIcon(
    state: ServerCheckUi,
    successComp: LottieComposition?,
    errorComp: LottieComposition?,
    modifier: Modifier = Modifier,
) {
    val size = 32.dp
    val animSize = 56.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        when (state) {
            ServerCheckUi.Loading -> {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(size)
                )
            }
            is ServerCheckUi.Success -> {
                val progress by animateLottieCompositionAsState(
                    composition = successComp,
                    iterations = 1
                )
                LottieAnimation(
                    composition = successComp,
                    progress = { progress },
                    modifier = Modifier.size(animSize),
                    contentScale = ContentScale.FillBounds,
                    clipToCompositionBounds = true,
                    alignment = Alignment.Center
                )
            }
            is ServerCheckUi.Error -> {
                val progress by animateLottieCompositionAsState(
                    composition = errorComp,
                    iterations = 1
                )
                LottieAnimation(
                    composition = errorComp,
                    progress = { progress },
                    modifier = Modifier.size(animSize),
                    contentScale = ContentScale.FillBounds,
                    clipToCompositionBounds = true,
                    alignment = Alignment.Center
                )
            }
            ServerCheckUi.Idle -> {}
        }
    }
}
