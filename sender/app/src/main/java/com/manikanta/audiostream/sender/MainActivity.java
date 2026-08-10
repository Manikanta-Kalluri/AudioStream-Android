package com.manikanta.audiostream.sender;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 100;

    private TextView statusText;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        startButton = findViewById(R.id.startButton);

        requestPermissionsIfNeeded();

        startButton.setOnClickListener(v -> startAudioService());

        statusText.setText(
                "Mobile 1\n\n" +
                "This phone is the microphone.\n\n" +
                "Start the microphone service.\n" +
                "Keep this phone connected to Mobile 2."
        );
    }

    private void requestPermissionsIfNeeded() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    PERMISSION_REQUEST
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        PERMISSION_REQUEST
                );
            }
        }
    }

    private void startAudioService() {

        Intent intent =
                new Intent(this, AudioForegroundService.class);

        ContextCompat.startForegroundService(this, intent);

        statusText.setText(
                "MICROPHONE ACTIVE\n\n" +
                "Audio streaming service is running.\n\n" +
                "You can leave this screen.\n" +
                "The foreground service continues running."
        );

        startButton.setEnabled(false);
    }
}
