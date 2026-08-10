package com.manikanta.audiostream.sender;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class AudioStreamer {

    private static final int SAMPLE_RATE = 16000;

    private final String serverUrl;
    private final String roomId;

    private final AtomicBoolean running =
            new AtomicBoolean(false);

    private AudioRecord recorder;

    private WebSocket socket;

    private Thread audioThread;

    public AudioStreamer(
            String serverUrl,
            String roomId) {

        this.serverUrl = serverUrl;
        this.roomId = roomId;
    }

    public void start() {

        if (running.getAndSet(true)) {
            return;
        }

        Request request =
                new Request.Builder()
                        .url(serverUrl)
                        .build();

        OkHttpClient client =
                new OkHttpClient();

        socket =
                client.newWebSocket(
                        request,
                        new WebSocketListener() {

                            @Override
                            public void onOpen(
                                    WebSocket webSocket,
                                    okhttp3.Response response) {

                                try {

                                    JSONObject json =
                                            new JSONObject();

                                    json.put(
                                            "type",
                                            "join"
                                    );

                                    json.put(
                                            "roomId",
                                            roomId
                                    );

                                    webSocket.send(
                                            json.toString()
                                    );

                                    startRecording();

                                } catch (Exception e) {

                                    stop();
                                }
                            }

                            @Override
                            public void onFailure(
                                    WebSocket webSocket,
                                    Throwable t,
                                    okhttp3.Response response) {

                                stop();
                            }
                        }
                );
    }

    private void startRecording() {

        int minBuffer =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        int bufferSize =
                Math.max(
                        minBuffer,
                        4096
                );

        recorder =
                new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                );

        recorder.startRecording();

        audioThread =
                new Thread(
                        () -> captureAudio(),
                        "AudioCapture"
                );

        audioThread.start();
    }

    private void captureAudio() {

        byte[] buffer =
                new byte[2048];

        while (running.get()) {

            int count =
                    recorder.read(
                            buffer,
                            0,
                            buffer.length
                    );

            if (count > 0 &&
                    socket != null) {

                socket.send(
                        ByteString.of(
                                buffer,
                                0,
                                count
                        )
                );
            }
        }
    }

    public void stop() {

        if (!running.getAndSet(false)) {
            return;
        }

        if (recorder != null) {

            try {
                recorder.stop();
            } catch (Exception ignored) {
            }

            recorder.release();

            recorder = null;
        }

        if (socket != null) {

            socket.close(
                    1000,
                    "Stopped"
            );

            socket = null;
        }

        audioThread = null;
    }
}
