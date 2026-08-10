package com.manikanta.audiostream.sender;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends ComponentActivity {

    private static final int MIC_PERMISSION = 100;

    private EditText serverUrl;
    private EditText roomId;

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        serverUrl =
                findViewById(R.id.serverUrl);

        roomId =
                findViewById(R.id.roomId);

        status =
                findViewById(R.id.status);

        Button start =
                findViewById(R.id.start);

        Button stop =
                findViewById(R.id.stop);

        start.setOnClickListener(
                v -> startService()
        );

        stop.setOnClickListener(
                v -> stopService()
        );
    }

    private void startService() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    MIC_PERMISSION
            );

            return;
        }

        String url =
                serverUrl.getText()
                        .toString()
                        .trim();

        String room =
                roomId.getText()
                        .toString()
                        .trim();

        Intent intent =
                new Intent(
                        this,
                        AudioForegroundService.class
                );

        intent.putExtra(
                "SERVER_URL",
                url
        );

        intent.putExtra(
                "ROOM_ID",
                room
        );

        ContextCompat.startForegroundService(
                this,
                intent
        );

        status.setText(
                "Microphone ACTIVE"
        );
    }

    private void stopService() {

        stopService(
                new Intent(
                        this,
                        AudioForegroundService.class
                )
        );

        status.setText(
                "Microphone STOPPED"
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] results) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                results
        );

        if (requestCode == MIC_PERMISSION
                && results.length > 0
                && results[0] ==
                PackageManager.PERMISSION_GRANTED) {

            startService();
        }
    }
}
