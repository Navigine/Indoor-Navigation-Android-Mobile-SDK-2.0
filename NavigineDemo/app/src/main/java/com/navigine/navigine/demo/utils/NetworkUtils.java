package com.navigine.navigine.demo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;

public class NetworkUtils {

    private static final NetworkRequest mNetworkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build();

    public static boolean isNetworkActive(Context context) {
        ConnectivityManager cm = SystemManagersProvider.getConnectivityManager(context);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) return networkInfo.isConnectedOrConnecting();
        }
        return false;
    }
}
