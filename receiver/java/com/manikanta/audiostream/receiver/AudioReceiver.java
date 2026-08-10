package com.manikanta.audiostream.receiver;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class AudioReceiver {

    public interface StatusListener {
        void onStatus(String message);
    }

    private static final int SAMPLE_RATE = 16000;

    private final String serverUrl;

    private final String roomId;

    private final StatusListener listener;

    private WebSocket socket;

    private AudioTrack audioTrack;

    private boolean listening = false;

    private boolean recording = false;

    private FileOutputStream output;

    private File recordingFile;

    private long audioBytes = 0;

    public AudioReceiver(
            String serverUrl,
            String roomId,
            StatusListener listener) {

        this.serverUrl = serverUrl;

        this.roomId = roomId;

        this.listener = listener;
    }

    public void connect() {

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
                                    Response response) {

                                try {

                                    String json =
                                            "{\"type\":\"join\",\"roomId\":\""
                                                    + roomId
                                                    + "\"}";

                                    webSocket.send(
                                            json
                                    );

                                    listener.onStatus(
                                            "Connected"
                                    );

                                } catch (Exception e) {

                                    listener.onStatus(
                                            "Join failed"
                                    );
                                }
                            }

                            @Override
                            public void onMessage(
                                    WebSocket webSocket,
                                    ByteString bytes) {

                                byte[] audio =
                                        bytes.toByteArray();

                                if (listening) {

                                    playAudio(
                                            audio
                                    );
                                }

                                if (recording) {

                                    saveAudio(
                                            audio
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    WebSocket webSocket,
                                    Throwable t,
                                    Response response) {

                                listener.onStatus(
                                        "Connection failed"
                                );
                            }
                        }
                );
    }

    public boolean toggleListen() {

        listening =
                !listening;

        if (listening) {

            createAudioTrack();

            listener.onStatus(
                    "LISTEN ON"
            );

        } else {

            stopAudioTrack();

            listener.onStatus(
                    "LISTEN OFF"
            );
        }

        return listening;
    }

    public boolean toggleRecord() {

        recording =
                !recording;

        if (recording) {

            try {

                startRecording();

                listener.onStatus(
                        "RECORD ON"
                );

            } catch (IOException e) {

                recording = false;

                listener.onStatus(
                        "Recording failed"
                );
            }

        } else {

            stopRecording();

            listener.onStatus(
                    "RECORD OFF"
            );
        }

        return recording;
    }

    private void createAudioTrack() {

        if (audioTrack != null) {
            return;
        }

        int buffer =
                AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );

        audioTrack =
                new AudioTrack.Builder()
                        .setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setUsage(
                                                AudioAttributes.USAGE_MEDIA
                                        )
                                        .setContentType(
                                                AudioAttributes.CONTENT_TYPE_SPEECH
                                        )
                                        .build()
                        )
                        .setAudioFormat(
                                new AudioFormat.Builder()
                                        .setSampleRate(
                                                SAMPLE_RATE
                                        )
                                        .setEncoding(
                                                AudioFormat.ENCODING_PCM_16BIT
                                        )
                                        .setChannelMask(
                                                AudioFormat.CHANNEL_OUT_MONO
                                        )
                                        .build()
                        )
                        .setBufferSizeInBytes(
                                Math.max(
                                        buffer,
                                        4096
                                )
                        )
                        .setTransferMode(
                                AudioTrack.MODE_STREAM
                        )
                        .build();

        audioTrack.play();
    }

    private void playAudio(
            byte[] audio) {

        if (audioTrack != null) {

            audioTrack.write(
                    audio,
                    0,
                    audio.length
            );
        }
    }

    private void stopAudioTrack() {

        if (audioTrack != null) {

            try {

                audioTrack.stop();

            } catch (Exception ignored) {
            }

            audioTrack.release();

            audioTrack = null;
        }
    }

    private void startRecording()
            throws IOException {

        File directory =
                new File(
                        getFilesDir(),
                        "recordings"
                );

        if (!directory.exists()) {

            if (!directory.mkdirs()) {

                throw new IOException(
                        "Cannot create directory"
                );
            }
        }

        recordingFile =
                new File(
                        directory,
                        "audio_"
                                + System.currentTimeMillis()
                                + ".pcm"
                );

        output =
                new FileOutputStream(
                        recordingFile
                );

        audioBytes = 0;
    }

    private void saveAudio(
            byte[] audio) {

        if (output == null) {
            return;
        }

        try {

            output.write(audio);

            audioBytes += audio.length;

        } catch (IOException e) {

            recording = false;

            stopRecording();
        }
    }

    private void stopRecording() {

        if (output != null) {

            try {

                output.flush();

                output.close();

            } catch (IOException ignored) {
            }

            output = null;
        }

        if (recordingFile != null) {

            listener.onStatus(
                    "Saved: "
                            + recordingFile.getAbsolutePath()
            );
        }
    }

    public void stop() {

        listening = false;

        recording = false;

        stopAudioTrack();

        stopRecording();

        if (socket != null) {

            socket.close(
                    1000,
                    "Stopped"
            );

            socket = null;
        }
    }
}
