package com.navigine.navigine.demo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.navigine.idl.java.Position;
import com.navigine.idl.java.PositionListener;
import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.ui.activities.MainActivity;
import com.navigine.navigine.demo.utils.Constants;
import com.navigine.navigine.demo.utils.NavigineSdkManager;

public class NavigationService extends Service {

    public static NavigationService INSTANCE = null;
    public static final String ACTION_POSITION_UPDATED = "ACTION_POSITION_UPDATED";
    public static final String ACTION_POSITION_ERROR = "ACTION_POSITION_ERROR";
    public static final String KEY_LOCATION_ID = "location_id";
    public static final String KEY_SUBLOCATION_ID = "sublocation_id";

    public static final String KEY_LOCATION_HEADING = "location_heading";
    public static final String KEY_POINT_X = "point_x";
    public static final String KEY_POINT_Y = "point_y";
    public static final String KEY_ERROR = "error";

    private PositionListener mPositionListener = new PositionListener() {
        @Override
        public void onPositionUpdated(Position position) {
            Intent intent = new Intent(ACTION_POSITION_UPDATED);
            intent.putExtra(KEY_LOCATION_ID, position.getLocationPoint().getLocationId());
            intent.putExtra(KEY_SUBLOCATION_ID, position.getLocationPoint().getSublocationId());
            intent.putExtra(KEY_POINT_X, position.getLocationPoint().getPoint().getX());
            intent.putExtra(KEY_POINT_Y, position.getLocationPoint().getPoint().getY());
            intent.putExtra(KEY_LOCATION_HEADING, position.getLocationHeading());

            sendBroadcast(intent);
        }

        @Override
        public void onPositionError(Error error) {
            Intent intent = new Intent(ACTION_POSITION_ERROR);
            intent.putExtra(KEY_ERROR, error.getMessage());
            sendBroadcast(intent);
        }
    };

    private PowerManager.WakeLock wakeLock = null;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wakeLockAcquire();
        addPositionListener();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        Notification notification = createNotification();
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        removePositionListener();
        wakeLockRelease();
        super.onDestroy();
    }

    private void addPositionListener() {
        if (NavigineSdkManager.NavigationManager != null) {
            if (mPositionListener != null) {
                NavigineSdkManager.NavigationManager.addPositionListener(mPositionListener);
            }
        }
    }

    private void removePositionListener() {
        if (NavigineSdkManager.NavigationManager != null) {
            if (mPositionListener != null) {
                NavigineSdkManager.NavigationManager.removePositionListener(mPositionListener);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        return new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.navigation_service_name))
                .setSmallIcon(R.drawable.ic_navigation)
                .setContentIntent(pendingIntent)
                .build();
    }

    public static void startService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (context != null) {
                ContextCompat.startForegroundService(context, new Intent(context, NavigationService.class));
            }
        }
    }

    public static void stopService(Context context) {
        if (context != null) context.stopService(new Intent(context, NavigationService.class));
    }

    private void wakeLockAcquire() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "navigine:wakelock");
        if (wakeLock != null) wakeLock.acquire();
    }

    private void wakeLockRelease() {
        if (wakeLock != null) wakeLock.release();
    }
}
