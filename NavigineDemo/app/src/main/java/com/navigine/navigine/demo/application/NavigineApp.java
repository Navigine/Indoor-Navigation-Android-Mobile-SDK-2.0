package com.navigine.navigine.demo.application;

import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_CHANNEL_ID;
import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_CHANNEL_NAME;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.RequiresApi;

import com.navigine.navigine.demo.utils.DimensionUtils;
import com.navigine.sdk.Navigine;

public class NavigineApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DimensionUtils.setDisplayMetrics(displayMetrics);

        Navigine.initialize(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
    }
}
