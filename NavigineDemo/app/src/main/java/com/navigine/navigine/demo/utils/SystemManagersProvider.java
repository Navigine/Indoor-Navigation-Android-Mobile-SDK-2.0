package com.navigine.navigine.demo.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.Nullable;

public class SystemManagersProvider {

    private static ConnectivityManager mConnectivityManager = null;

    @Nullable
    public static ConnectivityManager getConnectivityManager(Context context) {
        if (mConnectivityManager == null) {
            mConnectivityManager = context.getSystemService(ConnectivityManager.class);
        }
        return mConnectivityManager;
    }
}
