package com.manikanta.audiostream.receiver;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

public class MainActivity
        extends ComponentActivity {

    private EditText serverUrl;

    private EditText roomId;

    private TextView status;

    private AudioReceiver receiver;

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );

        serverUrl =
                findViewById(
                        R.id.serverUrl
                );

        roomId =
                findViewById(
                        R.id.roomId
                );

        status =
                findViewById(
                        R.id.status
                );

        Button connect =
                findViewById(
                        R.id.connect
                );

        Button listen =
                findViewById(
                        R.id.listen
                );

        Button record =
                findViewById(
                        R.id.record
                );

        Button disconnect =
                findViewById(
                        R.id.disconnect
                );

        connect.setOnClickListener(
                v -> connect()
        );

        listen.setOnClickListener(
                v -> {

                    if (receiver != null) {

                        boolean enabled =
                                receiver.toggleListen();

                        listen.setText(
                                enabled
                                        ? "LISTEN OFF"
                                        : "LISTEN ON"
                        );
                    }
                }
        );

        record.setOnClickListener(
                v -> {

                    if (receiver != null) {

                        boolean enabled =
                                receiver.toggleRecord();

                        record.setText(
                                enabled
                                        ? "RECORD OFF"
                                        : "RECORD ON"
                        );
                    }
                }
        );

        disconnect.setOnClickListener(
                v -> disconnect()
        );
    }

    private void connect() {

        String url =
                serverUrl
                        .getText()
                        .toString()
                        .trim();

        String room =
                roomId
                        .getText()
                        .toString()
                        .trim();

        receiver =
                new AudioReceiver(
                        url,
                        room,
                        message ->
                                runOnUiThread(
                                        () ->
                                                status.setText(
                                                        message
                                                )
                                )
                );

        receiver.connect();
    }

    private void disconnect() {

        if (receiver != null) {

            receiver.stop();

            receiver = null;
        }

        status.setText(
                "Disconnected"
        );
    }

    @Override
    protected void onDestroy() {

        if (receiver != null) {

            receiver.stop();

            receiver = null;
        }

        super.onDestroy();
    }
}
