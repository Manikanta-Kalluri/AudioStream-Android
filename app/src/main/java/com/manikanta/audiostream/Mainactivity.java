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

    private static final int REQUEST_MIC = 100;

    private TextView status;

    private EditText roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);

        roomId = findViewById(R.id.roomId);

        Button start =
                findViewById(R.id.start);

        Button stop =
                findViewById(R.id.stop);

        start.setOnClickListener(
                v -> startStreaming()
        );

        stop.setOnClickListener(
                v -> stopStreaming()
        );
    }

    private void startStreaming() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    REQUEST_MIC
            );

            return;
        }

        String room = roomId
                .getText()
                .toString()
                .trim();

        if (room.isEmpty()) {

            status.setText(
                    "Enter a room ID"
            );

            return;
        }

        Intent intent =
                new Intent(
                        this,
                        AudioForegroundService.class
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
                "Microphone session ACTIVE"
        );
    }

    private void stopStreaming() {

        stopService(
                new Intent(
                        this,
                        AudioForegroundService.class
                )
        );

        status.setText(
                "Microphone session STOPPED"
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_MIC
                && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {

            startStreaming();
        }
    }
}
