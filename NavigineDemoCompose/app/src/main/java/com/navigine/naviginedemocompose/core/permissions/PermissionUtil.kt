package com.navigine.naviginedemocompose.core.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

sealed interface PermissionStage {
    data object Foreground : PermissionStage   // coarse + fine + bt (S+)
    data object Notifications : PermissionStage // POST_NOTIFICATIONS (T+)
    data object Background : PermissionStage   // ACCESS_BACKGROUND_LOCATION (Q+)
}

enum class AppPermission(val manifest: String?) {
    Coarse(Manifest.permission.ACCESS_COARSE_LOCATION),
    Fine(Manifest.permission.ACCESS_FINE_LOCATION),
    BtScan(if (Build.VERSION.SDK_INT >= 31) Manifest.permission.BLUETOOTH_SCAN else null),
    BtConnect(if (Build.VERSION.SDK_INT >= 31) Manifest.permission.BLUETOOTH_CONNECT else null),
    PostNotifications(if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null),
    BackgroundLocation(
        if (Build.VERSION.SDK_INT >= 29) Manifest.permission.ACCESS_BACKGROUND_LOCATION else null
    ),
}

fun Context.isGranted(perm: String): Boolean =
    ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED

fun Context.areNotificationsEnabled(): Boolean =
    if (Build.VERSION.SDK_INT < 33) true else NotificationManagerCompat.from(this).areNotificationsEnabled()

fun requiredForegroundRuntimePerms(): List<String> = buildList {
    listOf(AppPermission.Coarse, AppPermission.Fine, AppPermission.BtScan, AppPermission.BtConnect)
        .forEach { it.manifest?.let(::add) }
}

fun needAskNotifications(ctx: Context): Boolean =
    Build.VERSION.SDK_INT >= 33 && !ctx.areNotificationsEnabled()

fun needAskBackgroundLocation(ctx: Context): Boolean {
    val perm = AppPermission.BackgroundLocation.manifest ?: return false
    return !ctx.isGranted(perm)
}

fun appSettingsIntent(ctx: Context): Intent = Intent(
    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
    Uri.fromParts("package", ctx.packageName, null)
).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)