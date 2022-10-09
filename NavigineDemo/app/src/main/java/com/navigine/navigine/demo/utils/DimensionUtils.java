package com.navigine.navigine.demo.utils;

import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.application.NavigineApp;

public class DimensionUtils {

    public static int STROKE_WIDTH = (int) NavigineApp.AppContext.getResources().getDimension(R.dimen.search_stroke_width);

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
