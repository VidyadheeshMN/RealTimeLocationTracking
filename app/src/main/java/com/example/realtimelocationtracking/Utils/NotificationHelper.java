package com.example.realtimelocationtracking.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContextWrapper;
import android.content.*;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.realtimelocationtracking.R;

public class NotificationHelper extends ContextWrapper {
    private static final String LOC_TRACK_ID = "com.example.realtimelocationtracking";
    private static final String LOC_TRACK_NAME = "LocationTracking";

    private NotificationManager manager;

    public NotificationHelper(Context base){
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel locChannel = new NotificationChannel(LOC_TRACK_ID,
                LOC_TRACK_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        locChannel.enableLights(false);
        locChannel.enableVibration(true);
        locChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(locChannel);
    }

    public NotificationManager getManager(){
            if(manager == null)
                manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getRealTimeTrackingNotification(String title, String content, Uri defaultSound) {
        return new Notification.Builder(getApplicationContext(), LOC_TRACK_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultSound)
                .setAutoCancel(false);
    }
}
