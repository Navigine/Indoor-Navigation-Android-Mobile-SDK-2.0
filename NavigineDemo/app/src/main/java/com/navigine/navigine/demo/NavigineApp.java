package com.navigine.navigine.demo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.navigine.idl.java.LocationListManager;
import com.navigine.idl.java.LocationManager;
import com.navigine.idl.java.MeasurementManager;
import com.navigine.idl.java.NavigationManager;
import com.navigine.idl.java.NavigineSdk;
import com.navigine.idl.java.NotificationManager;
import com.navigine.idl.java.ResourceManager;
import com.navigine.idl.java.RouteManager;
import com.navigine.idl.java.ZoneManager;
import com.navigine.sdk.Navigine;

public class NavigineApp extends Application implements LifecycleObserver {
    private final String TAG = this.getClass().getName();

    public static final String      DEFAULT_SERVER_URL = "https://api.navigine.com";
    public static final String      DEFAULT_USER_HASH  = "0000-0000-0000-0000";

    public static NavigineSdk mNavigineSdk = null;

    // Display settings
    public static float DisplayWidthPx  = 0.0f;
    public static float DisplayHeightPx = 0.0f;
    public static float DisplayWidthDp  = 0.0f;
    public static float DisplayHeightDp = 0.0f;
    public static float DisplayDensity  = 0.0f;

    public static String LocationServer = null;
    public static String UserHash = null;

    public static SharedPreferences Settings = null;

    // managers

    public static LocationListManager LocationListManager = null;
    public static LocationManager     LocationManager     = null;
    public static ResourceManager     ResourceManager     = null;
    public static NavigationManager   NavigationManager   = null;
    public static NotificationManager NotificationManager = null;
    public static MeasurementManager  MeasurementManager  = null;
    public static RouteManager        RouteManager        = null;
    public static ZoneManager         ZoneManager     = null;

    public static int LocationId = 0;

    public synchronized static void createInstance(Context context)
    {
        Navigine.initialize(context);
        Navigine.setMode(Navigine.Mode.NORMAL);

        Settings = context.getSharedPreferences("Navigine", 0);
        LocationServer = Settings.getString ("location_server", DEFAULT_SERVER_URL);
        UserHash = Settings.getString ("user_hash", DEFAULT_USER_HASH);

        // Initializing display parameters
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        DisplayWidthPx  = displayMetrics.widthPixels;
        DisplayHeightPx = displayMetrics.heightPixels;
        DisplayDensity  = displayMetrics.density;
        DisplayWidthDp  = DisplayWidthPx  / DisplayDensity;
        DisplayHeightDp = DisplayHeightPx / DisplayDensity;
    }

    public synchronized static boolean initializeSdk()
    {
        try {
            NavigineSdk.setUserHash(UserHash);
            NavigineSdk.setServer(LocationServer);
            mNavigineSdk = NavigineSdk.getInstance();
            LocationListManager = mNavigineSdk.getLocationListManager();
            LocationManager = mNavigineSdk.getLocationManager();
            ResourceManager = mNavigineSdk.getResourceManager(LocationManager);
            NavigationManager = mNavigineSdk.getNavigationManager(LocationManager);
            MeasurementManager = mNavigineSdk.getMeasurementManager();
            RouteManager = mNavigineSdk.getRouteManager(LocationManager, NavigationManager);
            NotificationManager = mNavigineSdk.getNotificationManager(LocationManager);
            ZoneManager = mNavigineSdk.getZoneManager(LocationManager, NavigationManager);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Navigine.setMode(Navigine.Mode.NORMAL);
        Log.d(TAG, "Lifecycle.Event.ON_START onAppForegrounded!");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        Navigine.setMode(Navigine.Mode.NORMAL);
        Log.d(TAG, "Lifecycle.Event.ON_RESUME");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Navigine.setMode(Navigine.Mode.BACKGROUND);
        Log.d(TAG, "Lifecycle.Event.ON_PAUSE");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Navigine.setMode(Navigine.Mode.BACKGROUND);
        Log.d(TAG, "Lifecycle.Event.ON_STOP onAppBackgrounded!");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        Navigine.setMode(Navigine.Mode.BACKGROUND);
        Log.d(TAG, "Lifecycle.Event.ON_DESTROY");
    }
}
