package com.navigine.navigine.demo.utils;

import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.util.Log;

import java.security.MessageDigest;

public class HashingProvider {

    public static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < messageDigest.length; ++i) {
                String h = Integer.toHexString(255 & messageDigest[i]);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }

            return hexString.toString().toUpperCase();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }
}
