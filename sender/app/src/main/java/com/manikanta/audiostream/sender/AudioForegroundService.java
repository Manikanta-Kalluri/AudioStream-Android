package com.manikanta.audiostream.sender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AudioForegroundService
        extends Service {

    private static final String CHANNEL =
            "audio_channel";

    private AudioStreamer streamer;

    @Override
    public void onCreate() {

        super.onCreate();

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL,
                        "Audio Streaming",
                        NotificationManager.IMPORTANCE_LOW
                );

        NotificationManager manager =
                getSystemService(
                        NotificationManager.class
                );

        manager.createNotificationChannel(
                channel
        );

        Notification notification =
                new Notification.Builder(
                        this,
                        CHANNEL
                )
                .setContentTitle(
                        "AudioStream"
                )
                .setContentText(
                        "Microphone is active"
                )
                .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                )
                .setOngoing(true)
                .build();

        startForeground(
                1001,
                notification
        );
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        String url =
                intent.getStringExtra(
                        "SERVER_URL"
                );

        String room =
                intent.getStringExtra(
                        "ROOM_ID"
                );

        if (streamer == null) {

            streamer =
                    new AudioStreamer(
                            url,
                            room
                    );

            streamer.start();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        if (streamer != null) {

            streamer.stop();

            streamer = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
