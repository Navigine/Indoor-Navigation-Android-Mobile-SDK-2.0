package com.navigine.naviginedemocompose.ui.profile

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.navigine.naviginedemocompose.R
import com.navigine.naviginedemocompose.ui.composables.AppButtonPrimary
import com.navigine.naviginedemocompose.ui.composables.AppButtonTonal
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    vm: ProfileViewModel = hiltViewModel(),
    onRequireReLogin: () -> Unit
) {

    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current

    val pullState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        vm.effect.collect { eff ->
            when (eff) {
                is ProfileEffect.Message -> scope.launch { snack.showSnackbar(eff.text) }
                ProfileEffect.NavigateToLogin -> onRequireReLogin()
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snack) }) { inner ->
        PullToRefreshBox(
            state = pullState,
            onRefresh = { vm.onEvent(ProfileEvent.Refresh) },
            isRefreshing = state.isRefreshing,
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                ProfileHeader(state.avatarUrl)

                Text(
                    text = state.name.ifBlank { "—" },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.isEditing) {
                        LabeledTextField(
                            label = "Name",
                            value = state.name,
                            onValueChange = { vm.onEvent(ProfileEvent.NameChange(it)) },
                            ime = ImeAction.Next
                        )
                        LabeledTextField(
                            label = "Company",
                            value = state.company,
                            onValueChange = { vm.onEvent(ProfileEvent.CompanyChange(it)) },
                            ime = ImeAction.Done
                        )
                        ReadonlyInfoCard(label = "E-mail", value = state.email)
                        ReadonlyInfoCard(
                            label = "User",
                            value = state.userHash,
                            trailing = {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            clipboard.setClipEntry(
                                                ClipEntry(
                                                    ClipData.newPlainText(
                                                        state.userHash,
                                                        state.userHash
                                                    )
                                                )
                                            )
                                            snack.showSnackbar("Hash copied")
                                        }
                                    }
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_copy),
                                        contentDescription = "Copy"
                                    )
                                }
                            }
                        )
                    } else {
                        // view mode cards
                        ReadonlyInfoCard(
                            label = "User",
                            value = state.userHash,
                            trailing = {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            clipboard.setClipEntry(
                                                ClipEntry(
                                                    ClipData.newPlainText(
                                                        state.userHash,
                                                        state.userHash
                                                    )
                                                )
                                            )
                                            snack.showSnackbar("Hash copied")
                                        }
                                    }
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_copy),
                                        contentDescription = "Copy"
                                    )
                                }
                            }
                        )
                        ReadonlyInfoCard(label = "Company", value = state.company.ifBlank { "—" })
                        ReadonlyInfoCard(label = "E-mail", value = state.email.ifBlank { "—" })
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isEditing) {
                        AppButtonPrimary(
                            onClick = { vm.onEvent(ProfileEvent.Save) },
                            enabled = state.canSave && !state.isLoading,
                            modifier = Modifier.weight(1f)
                        ) { Text("Save") }
                        AppButtonTonal(
                            onClick = { vm.onEvent(ProfileEvent.EditToggle) },
                            enabled = !state.isLoading,
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancel") }
                    } else {
                        Column {
                            AppButtonPrimary(
                                onClick = { vm.onEvent(ProfileEvent.EditToggle) },
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Edit profile") }
                            AppButtonTonal(
                                onClick = { vm.onEvent(ProfileEvent.Logout) },
                                enabled = !state.isLoading,
                                colors = ButtonDefaults.filledTonalButtonColors()
                                    .copy(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Logout") }
                        }
                    }
                }

                if (state.isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) { CircularProgressIndicator() }
                }
            }
        }
    }

}

@Composable
private fun ProfileHeader(
    avatarUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
    ) {

        Surface(
            shape = RoundedCornerShape(HeaderRadius),
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Image(
                painter = painterResource(R.drawable.ic_card_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        AsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(AvatarSize)
                .align(Alignment.BottomCenter)
                .offset(y = AvatarOffsetY)
                .clip(CircleShape)
        )
    }

    Spacer(Modifier.height(AvatarBottomSpacer))
}

@Composable
private fun ReadonlyInfoCard(
    label: String,
    value: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    ime: ImeAction
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ime
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

private val HeaderHeight = 156.dp
private val HeaderRadius = 16.dp
private val AvatarSize = 112.dp
private val AvatarOffsetY = 40.dp
private val AvatarBottomSpacer = 40.dp

