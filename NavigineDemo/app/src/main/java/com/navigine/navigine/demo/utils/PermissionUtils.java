package com.navigine.navigine.demo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.navigine.navigine.demo.R;

public class PermissionUtils {

    private static final int RC_PERMISSION_BACKGROUND_LOCATION = 101;
    private static AlertDialog mAlertBluetoothDialog, mAlertLocationDialog = null;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static boolean hasBluetoothPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean hasLocationBackgroundPermission(Context context) {
        return hasLocationPermission(context)
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void showBackgroundPermissionRationale(Activity context) {
        if (mAlertLocationDialog == null) {
            mAlertLocationDialog = new MaterialAlertDialogBuilder(context, R.style.AppTheme_MaterialAlertDialog_Rounded)
                    .setMessage(R.string.permission_rationale_message_bg_access)
                    .setPositiveButton(R.string.dialog_action_ok, (dialog, which) ->
                            ActivityCompat.requestPermissions(context,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, RC_PERMISSION_BACKGROUND_LOCATION))
                    .create();
        }
        mAlertLocationDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void showBluetoothPermissionRationale(Activity context) {
        if (mAlertBluetoothDialog == null) {
            mAlertBluetoothDialog = new MaterialAlertDialogBuilder(context, R.style.AppTheme_MaterialAlertDialog_Rounded)
                    .setMessage(R.string.permission_rationale_message_bluetooth)
                    .setPositiveButton(R.string.dialog_action_understand, (dialog, which) -> {
                    })
                    .create();
        }
        mAlertBluetoothDialog.show();
    }
}
