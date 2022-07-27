package com.navigine.navigine.demo.utils;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DimensionUtils {

    // Display settings
    private static DisplayMetrics DisplayMetrics  = null;

    public static float DisplayWidthPx  = 0.0f;
    public static float DisplayHeightPx = 0.0f;
    public static float DisplayWidthDp  = 0.0f;
    public static float DisplayHeightDp = 0.0f;
    public static float DisplayDensity  = 0.0f;

    public static void setDisplayMetrics(DisplayMetrics displayMetrics) {
        DisplayWidthPx  = displayMetrics.widthPixels;
        DisplayHeightPx = displayMetrics.heightPixels;
        DisplayDensity  = displayMetrics.density;
        DisplayWidthDp  = DisplayWidthPx / DisplayDensity;
        DisplayHeightDp = DisplayHeightPx / DisplayDensity;
        DisplayMetrics  = displayMetrics;
    }

    public static float pxFromDp(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, DisplayMetrics);
    }
}
