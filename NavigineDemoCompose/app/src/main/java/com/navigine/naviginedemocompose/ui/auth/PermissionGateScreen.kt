package com.navigine.naviginedemocompose.ui.auth

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.navigine.naviginedemocompose.core.permissions.AppPermission
import com.navigine.naviginedemocompose.core.permissions.PermissionStage
import com.navigine.naviginedemocompose.core.permissions.appSettingsIntent
import com.navigine.naviginedemocompose.core.permissions.isGranted
import com.navigine.naviginedemocompose.core.permissions.needAskBackgroundLocation
import com.navigine.naviginedemocompose.core.permissions.needAskNotifications
import com.navigine.naviginedemocompose.core.permissions.requiredForegroundRuntimePerms
import com.navigine.naviginedemocompose.ui.composables.AppButtonPrimary
import com.navigine.naviginedemocompose.ui.composables.AppButtonTonal

/**
 * Permission gate:
 *  - Always shows rationale before each request.
 *  - Foreground (coarse+fine+bt S+) -> Notifications (T+) -> Background (Q+)
 *  - Calls onAllGranted() when all satisfied.
 */
@Composable
fun PermissionGateScreen(
    onAllGranted: () -> Unit,
    viewmodel: GateViewModel
) {
    val ctx = LocalContext.current

    fun computeStage(): PermissionStage? {
        val fg = requiredForegroundRuntimePerms()
        val fgOk = fg.all { ctx.isGranted(it) }
        if (!fgOk) return PermissionStage.Foreground

        if (needAskNotifications(ctx)) return PermissionStage.Notifications

        if (needAskBackgroundLocation(ctx)) return PermissionStage.Background

        return null
    }

    var stage by remember { mutableStateOf(computeStage()) }

    val foregroundLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            stage = computeStage()
        }
    val singlePermLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            stage = computeStage()
        }

    LaunchedEffect(stage) {
        if (stage == null) {
            onAllGranted()
            viewmodel.autoInitSdk()  // if user is logged in, init sdk
        }
    }

    when (stage) {
        PermissionStage.Foreground -> {
            PermissionRationale(
                title = "Allow Location & Bluetooth",
                description = buildString {
                    append("• Approximate & Precise location for indoor positioning\n")
                    if (Build.VERSION.SDK_INT >= 31) append("• Bluetooth scan/connect to detect beacons\n")
                },
                primary = "Continue",
                onPrimary = {
                    val toAsk = requiredForegroundRuntimePerms().toTypedArray()
                    if (toAsk.isEmpty()) stage =
                        computeStage() else foregroundLauncher.launch(toAsk)
                },
                onOpenSettings = { ctx.startActivity(appSettingsIntent(ctx)) }
            )
        }

        PermissionStage.Notifications -> {
            PermissionRationale(
                title = "Allow Notifications",
                description = "We use notifications to inform you about important events.",
                primary = "Continue",
                onPrimary = {
                    val perm = AppPermission.PostNotifications.manifest
                    if (perm == null) stage = computeStage() else singlePermLauncher.launch(perm)
                },
                onOpenSettings = { ctx.startActivity(appSettingsIntent(ctx)) }
            )
        }

        PermissionStage.Background -> {
            PermissionRationale(
                title = "Allow Background Location",
                description = "Lets navigation and zone monitoring work when the app isn't on screen.",
                primary = if (Build.VERSION.SDK_INT >= 30) "Continue" else "Open settings",
                onPrimary = {
                    val perm = AppPermission.BackgroundLocation.manifest
                    if (perm == null) stage = computeStage() else singlePermLauncher.launch(perm)
                },
                onOpenSettings = { ctx.startActivity(appSettingsIntent(ctx)) }
            )
        }

        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun PermissionRationale(
    title: String,
    description: String,
    primary: String,
    onPrimary: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(12.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            AppButtonPrimary (onClick = onPrimary, shape = MaterialTheme.shapes.extraSmall) {
                Text(primary, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            AppButtonTonal (onClick = onOpenSettings) { Text("Open app settings") }
        }
    }
}