package com.navigine.navigine.demo.utils;

import static com.navigine.navigine.demo.utils.Constants.TAG;

import android.util.Log;

import com.navigine.idl.java.LocationListManager;
import com.navigine.idl.java.LocationManager;
import com.navigine.idl.java.MeasurementManager;
import com.navigine.idl.java.NavigationManager;
import com.navigine.idl.java.NavigineSdk;
import com.navigine.idl.java.NotificationManager;
import com.navigine.idl.java.ResourceManager;
import com.navigine.idl.java.RouteManager;
import com.navigine.idl.java.ZoneManager;
import com.navigine.navigine.demo.models.UserSession;

public class NavigineSdkManager {

    // managers
    public static LocationListManager LocationListManager = null;
    public static LocationManager     LocationManager     = null;
    public static ResourceManager     ResourceManager     = null;
    public static NavigationManager   NavigationManager   = null;
    public static NotificationManager NotificationManager = null;
    public static MeasurementManager  MeasurementManager  = null;
    public static RouteManager        RouteManager        = null;
    public static ZoneManager         ZoneManager         = null;


    public synchronized static boolean initializeSdk() {
        if (UserSession.USER_HASH == null || UserSession.USER_HASH.isEmpty()) return false;
        try {
            NavigineSdk.setUserHash(UserSession.USER_HASH);
            NavigineSdk.setServer(UserSession.LOCATION_SERVER);

            NavigineSdk SDK = NavigineSdk.getInstance();

            LocationListManager = SDK.getLocationListManager();
            LocationManager     = SDK.getLocationManager();
            ResourceManager     = SDK.getResourceManager(LocationManager);
            NavigationManager   = SDK.getNavigationManager(LocationManager);
            MeasurementManager  = SDK.getMeasurementManager(LocationManager);
            RouteManager        = SDK.getRouteManager(LocationManager, NavigationManager);
            NotificationManager = SDK.getNotificationManager(LocationManager);
            ZoneManager         = SDK.getZoneManager(LocationManager, NavigationManager);
        } catch (Exception e) {
            Log.e(TAG, "Failed initialize Navigine SDK " + e.getMessage());
            return false;
        }
        return true;
    }
}
