package com.manikanta.audiostream;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends ComponentActivity {

    private static final int REQUEST_MIC = 100;

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);

        Button startButton = findViewById(R.id.start);
        Button stopButton = findViewById(R.id.stop);

        startButton.setOnClickListener(v -> startMicrophone());

        stopButton.setOnClickListener(v -> stopMicrophone());
    }

    private void startMicrophone() {

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

        Intent intent =
                new Intent(this, AudioForegroundService.class);

        ContextCompat.startForegroundService(this, intent);

        status.setText("Microphone session ACTIVE");
    }

    private void stopMicrophone() {

        Intent intent =
                new Intent(this, AudioForegroundService.class);

        stopService(intent);

        status.setText("Microphone session STOPPED");
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

            startMicrophone();
        }
    }
}
