package com.navigine.navigine.demo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.utils.Constants;

public class NavigationWorker extends Worker {
    public NavigationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (NavigationService.INSTANCE == null) {
            Intent intent = new Intent(getApplicationContext(), NavigationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                getApplicationContext().startForegroundService(intent);
            else getApplicationContext().startService(intent);
        }
        return Result.success();
    }

    @NonNull
    @Override
    public ForegroundInfo getForegroundInfo() {
        return createForegroundInfo();
    }

    private ForegroundInfo createForegroundInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        return new ForegroundInfo(1, createNotification());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(getApplicationContext(), Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getApplicationContext().getString(R.string.navigation_service_name))
                .setSmallIcon(R.drawable.ic_navigation)
                .build();
    }
}
