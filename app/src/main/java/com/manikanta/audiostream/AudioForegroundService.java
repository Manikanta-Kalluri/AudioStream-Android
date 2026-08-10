package com.manikanta.audiostream.sender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AudioForegroundService
        extends Service {

    private static final String CHANNEL_ID =
            "audio_stream_channel";

    private static final int NOTIFICATION_ID =
            1001;

    private WebRtcSender webRtcSender;

    @Override
    public void onCreate() {

        super.onCreate();

        createNotificationChannel();

        Notification notification =
                new Notification.Builder(
                        this,
                        CHANNEL_ID
                )
                .setContentTitle(
                        "Audio Stream"
                )
                .setContentText(
                        "Microphone streaming is active"
                )
                .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                )
                .setOngoing(true)
                .build();

        startForeground(
                NOTIFICATION_ID,
                notification
        );
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        String roomId =
                intent.getStringExtra(
                        "ROOM_ID"
                );

        if (webRtcSender == null) {

            webRtcSender =
                    new WebRtcSender(
                            getApplicationContext(),
                            roomId
                    );

            webRtcSender.start();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        if (webRtcSender != null) {

            webRtcSender.stop();

            webRtcSender = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void createNotificationChannel() {

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "Audio Streaming",
                        NotificationManager.IMPORTANCE_LOW
                );

        channel.setDescription(
                "Shows when microphone streaming is active."
        );

        NotificationManager manager =
                getSystemService(
                        NotificationManager.class
                );

        if (manager != null) {

            manager.createNotificationChannel(
                    channel
            );
        }
    }
}
