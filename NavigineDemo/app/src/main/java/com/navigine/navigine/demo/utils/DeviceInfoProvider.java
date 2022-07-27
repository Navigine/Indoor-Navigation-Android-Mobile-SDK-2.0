package com.navigine.navigine.demo.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;


public class DeviceInfoProvider {

    public static String getDeviceId(Context context) {
        return generateDeviceID(context);
    }

    private static String generateDeviceID(Context context) {
        try {
            String id = getAndroidID(context);
            if (id.length() > 0) {
                return id;
            }

            if (Build.SERIAL != null && !Build.SERIAL.equals("UNKNOWN")) {
                return Build.SERIAL;
            }
        } catch (Throwable var1) {
            var1.printStackTrace();
        }

        return getDeviceHash();
    }


    private static String getAndroidID(Context context) {
        String str = Settings.Secure.getString(context.getContentResolver(), "android_id");
        return str == null ? "" : str.toUpperCase();
    }

    private static String getDeviceHash() {
        String str = Build.BOARD + Build.BRAND + Build.CPU_ABI + Build.DEVICE + Build.DISPLAY + Build.HOST + Build.ID + Build.MANUFACTURER + Build.MODEL + Build.PRODUCT + Build.TAGS + Build.TYPE + Build.USER + Build.FINGERPRINT;
        return HashingProvider.md5(str);
    }
}
