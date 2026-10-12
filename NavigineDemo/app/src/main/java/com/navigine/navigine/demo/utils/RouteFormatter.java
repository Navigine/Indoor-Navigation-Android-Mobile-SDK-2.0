package com.navigine.navigine.demo.utils;

import androidx.annotation.DrawableRes;

import com.navigine.idl.java.RouteAnnotation;
import com.navigine.idl.java.RouteAnnotationType;
import com.navigine.navigine.demo.R;

import java.util.Locale;

public class RouteFormatter {
    public static final int NO_ICON = 0;

    private static final float WALKING_SPEED_MPS = 1.4f;
    private static final float SECONDS_PER_MINUTE = 60f;

    private RouteFormatter() {
    }

    public static String instruction(RouteAnnotation annotation, float distanceMeters) {
        String prefix = String.format(Locale.ENGLISH, "In %.0f m", distanceMeters);
        if (annotation.getType() == RouteAnnotationType.FINISH) return prefix + " finish!";
        String title = annotation.getTitle();
        return title == null || title.isEmpty() ? prefix : prefix + " " + title;
    }

    public static String distance(float meters) {
        return String.format(Locale.ENGLISH, "%.0f m", meters);
    }

    public static String duration(float seconds) {
        float minutes = seconds / SECONDS_PER_MINUTE;
        return minutes < 1 ? "< 1 min" : String.format(Locale.ENGLISH, "%.0f min", minutes);
    }

    public static float estimateSeconds(float meters) {
        return meters / WALKING_SPEED_MPS;
    }

    @DrawableRes
    public static int icon(RouteAnnotationType type) {
        switch (type) {
            case TRANSITION:
                return R.drawable.ic_escalator;
            case FINISH:
                return R.drawable.ic_to_point;
            default:
                return NO_ICON;
        }
    }
}
