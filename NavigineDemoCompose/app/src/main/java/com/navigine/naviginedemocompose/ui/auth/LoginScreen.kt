package com.navigine.naviginedemocompose.ui.auth

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.navigine.naviginedemocompose.BuildConfig
import com.navigine.naviginedemocompose.R
import com.navigine.naviginedemocompose.ui.composables.AppButtonPrimary
import com.navigine.naviginedemocompose.ui.composables.ServerStateIcon
import com.navigine.naviginedemocompose.ui.theme.spacing

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenMain: (sublocId: Long?, venueId: Long?) -> Unit = { _, _ -> },
    viewModel: LoginViewModel = hiltViewModel(),
) {

    val ui by viewModel.state.collectAsState()
    val ctx = LocalContext.current

    var showServerDialog by remember { mutableStateOf(false) }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { res ->
        res?.contents?.let { raw ->
            viewModel.handleQrScan(raw) {subloc, venueId ->
                onOpenMain(subloc, venueId)
            }
        }
    }

    val camPermLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) barcodeLauncher.launch(
                ScanOptions()
                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    .setBeepEnabled(false)
                    .setOrientationLocked(false)
                    .setPrompt("Point the camera at a QR code")
            )
            else Toast.makeText(ctx, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

    fun openQr() {
        camPermLauncher.launch(Manifest.permission.CAMERA)
    }

    if (showServerDialog) {
        ServerDialog(
            current = ui.currentServer.ifBlank { "" },
            onDismiss = { showServerDialog = false },
            state = viewModel.serverCheck.collectAsState().value,
            onSubmit = { url ->
                viewModel.validateAndSaveServer(
                    url = url,
                    onSaved = { showServerDialog = false },
                    onError = { err -> Toast.makeText(ctx, err, Toast.LENGTH_SHORT).show() }
                )
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Navigine indoor positioning app",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(MaterialTheme.spacing.lg))

            OutlinedTextField(
                value = ui.userHash,
                onValueChange = viewModel::onHashChange,
                label = { Text("User hash") },
                placeholder = { Text("0000-0000-0000-0000") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                trailingIcon = {
                    IconButton(onClick = ::openQr) {
                        Icon(painterResource(R.drawable.ic_qr_code), contentDescription = "Scan QR")
                    }
                },
                isError = ui.error != null || (ui.userHash.isNotBlank() && !ui.isValidHash),
                modifier = Modifier.fillMaxWidth()
            )
            if (ui.error != null) {
                Text(
                    ui.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(MaterialTheme.spacing.lg))

            AppButtonPrimary(
                onClick = { viewModel.login(onSuccess = onLoggedIn) },
                modifier = Modifier.fillMaxWidth(),
                enabled = ui.isValidHash && !ui.isLoading,
                shape = MaterialTheme.shapes.extraSmall
            ) { if (ui.isLoading) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Continue") }

            Spacer(Modifier.height(MaterialTheme.spacing.md))

            TextButton(onClick = { showServerDialog = true }) {
                Text(
                    text = if (ui.currentServer.isBlank()) "Change server (using default)" else "Change server: ${ui.currentServer}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerDialog(
    current: String,
    state: ServerCheckUi,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var text by remember { mutableStateOf(current) }

    val successComp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.verify))
    val errorComp   by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.failed))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.md, horizontal = MaterialTheme.spacing.lg)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Change server",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.weight(1f))
                ServerStateIcon(
                    state = state,
                    successComp = successComp,
                    errorComp = errorComp
                )
                IconButton(onClick = onDismiss) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = "Close")
                }
            }
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("Base URL") },
                placeholder = { Text(BuildConfig.DEFAULT_SERVER_URL) }
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            AppButtonPrimary(
                onClick = { onSubmit(text) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraSmall,
                enabled = text.isNotBlank()
            ) {
                Text("Change")
            }
        }
    }

}