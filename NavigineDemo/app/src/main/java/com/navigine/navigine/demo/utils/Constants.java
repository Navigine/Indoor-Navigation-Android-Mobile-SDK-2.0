package com.navigine.navigine.demo.utils;

import com.navigine.navigine.demo.BuildConfig;

public class Constants {

    public static final String TAG = "NavigineDemo.LOG";

    // deep link query params
    public static final String DL_QUERY_SERVER         = "server";
    public static final String DL_QUERY_USERHASH       = "userhash";
    public static final String DL_QUERY_LOCATION_ID    = "loc";
    public static final String DL_QUERY_SUBLOCATION_ID = "subloc";
    public static final String DL_QUERY_VENUE_ID       = "venue_id";

    // notifications
    public static final String NOTIFICATION_CHANNEL_ID   = BuildConfig.APPLICATION_ID + ".PUSH";
    public static final String NOTIFICATION_CHANNEL_NAME = "NAVIGINE_PUSH";
    public static final int    NOTIFICATION_PUSH_ID      = 1;
    public static final int    REQUEST_CODE_NOTIFY       = 102;
    // notifications extras
    public static final String NOTIFICATION_TITLE = "notification_title";
    public static final String NOTIFICATION_TEXT  = "notification_text";
    public static final String NOTIFICATION_IMAGE = "notification_image";

    // network
    public static final String HOST_VERIFY_TAG       = "verify_request";
    public static final String ENDPOINT_HEALTH_CHECK = "/mobile/health_check";
    public static final String ENDPOINT_GET_USER     = "/mobile/v1/users/get?userHash=";
    public static final String RESPONSE_KEY_NAME     = "name";
    public static final String RESPONSE_KEY_EMAIl    = "email";
    public static final String RESPONSE_KEY_HASH     = "hash";
    public static final String RESPONSE_KEY_AVATAR   = "avatar_url";
    public static final String RESPONSE_KEY_COMPANY  = "company_name";

    // anim image sizes
    public static final int   SIZE_SUCCESS         = 52;
    public static final int   SIZE_FAILED          = 32;
    public static final float CHECK_FRAME_SELECTED = 1f;

    // broadcast events
    public static final String LOCATION_CHANGED = "LOCATION_CHANGED";
    public static final String VENUE_SELECTED   = "VENUE_SELECTED";
    public static final String VENUE_FILTER_ON  = "VENUE_FILTER_ON";
    public static final String VENUE_FILTER_OFF = "VENUE_FILTER_OFF";

    // intent keys
    public static final String KEY_VENUE_SUBLOCATION = "venue_sublocation";
    public static final String KEY_VENUE_POINT       = "venue_point";
    public static final String KEY_VENUE_CATEGORY    = "venue_category";

    // debug mode
    public static final int LIST_SIZE_DEFAULT = 6;

    // circular progress
    public static final int CIRCULAR_PROGRESS_DELAY_SHOW = 200;
    public static final int CIRCULAR_PROGRESS_DELAY_HIDE = 700;
}
