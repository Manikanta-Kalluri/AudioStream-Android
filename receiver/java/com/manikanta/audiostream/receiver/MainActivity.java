```java
package com.manikanta.audiostream.receiver;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText ipAddress;

    private Button connectButton;
    private Button listenButton;
    private Button recordButton;

    private TextView statusText;

    private AudioReceiver audioReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ipAddress =
                findViewById(R.id.ipAddress);

        connectButton =
                findViewById(R.id.connectButton);

        listenButton =
                findViewById(R.id.listenButton);

        recordButton =
                findViewById(R.id.recordButton);

        statusText =
                findViewById(R.id.statusText);

        audioReceiver =
                new AudioReceiver();

        listenButton.setEnabled(false);
        recordButton.setEnabled(false);

        connectButton.setOnClickListener(v -> {

            String ip =
                    ipAddress
                            .getText()
                            .toString()
                            .trim();

            if (ip.isEmpty()) {

                statusText.setText(
                        "Enter Mobile 1 IP address"
                );

                return;
            }

            audioReceiver.connect(
                    ip,
                    new AudioReceiver.ConnectionListener() {

                        @Override
                        public void onConnected() {

                            runOnUiThread(() -> {

                                statusText.setText(
                                        "CONNECTED TO MOBILE 1"
                                );

                                connectButton
                                        .setEnabled(false);

                                listenButton
                                        .setEnabled(true);

                                recordButton
                                        .setEnabled(true);
                            });
                        }

                        @Override
                        public void onDisconnected() {

                            runOnUiThread(() -> {

                                statusText.setText(
                                        "DISCONNECTED"
                                );

                                connectButton
                                        .setEnabled(true);

                                listenButton
                                        .setEnabled(false);

                                recordButton
                                        .setEnabled(false);
                            });
                        }
                    }
            );
        });

        listenButton.setOnClickListener(v -> {

            boolean listening =
                    audioReceiver.toggleListening();

            listenButton.setText(
                    listening
                            ? "LISTEN OFF"
                            : "LISTEN ON"
            );
        });

        recordButton.setOnClickListener(v -> {

            boolean recording =
                    audioReceiver.toggleRecording();

            recordButton.setText(
                    recording
                            ? "RECORD OFF"
                            : "RECORD ON"
            );
        });
    }

    @Override
    protected void onDestroy() {

        if (audioReceiver != null) {
            audioReceiver.disconnect();
        }

        super.onDestroy();
    }
}
```
