package com.navigine.navigine.demo.models;

import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_CHANNEL_ID;
import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_IMAGE;
import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_TEXT;
import static com.navigine.navigine.demo.utils.Constants.NOTIFICATION_TITLE;
import static com.navigine.navigine.demo.utils.Constants.REQUEST_CODE_NOTIFY;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.ui.activities.NotificationActivity;

public class PushNotification {

    public static Notification create(Context context, com.navigine.idl.java.Notification notification, @Nullable Bitmap img, String url) {

        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(NOTIFICATION_TITLE, notification.getTitle());
        intent.putExtra(NOTIFICATION_TEXT, notification.getContent());
        intent.putExtra(NOTIFICATION_IMAGE, url);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE_NOTIFY, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE_NOTIFY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getContent())
                .setLargeIcon(img)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getContent()))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .build();
    }
}
