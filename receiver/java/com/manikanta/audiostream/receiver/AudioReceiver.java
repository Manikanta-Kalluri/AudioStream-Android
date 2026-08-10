```java
package com.manikanta.audiostream.receiver;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class AudioReceiver {

    private static final int PORT = 8988;

    private static final int SAMPLE_RATE = 16000;

    private static final int CHANNEL =
            AudioFormat.CHANNEL_OUT_MONO;

    private static final int AUDIO_FORMAT =
            AudioFormat.ENCODING_PCM_16BIT;

    private Socket socket;

    private AudioTrack audioTrack;

    private volatile boolean connected = false;

    private volatile boolean listening = true;

    private volatile boolean recording = false;

    private FileOutputStream recordingOutput;

    private Thread receiverThread;

    public interface ConnectionListener {

        void onConnected();

        void onDisconnected();
    }

    public void connect(
            String ip,
            ConnectionListener listener
    ) {

        receiverThread = new Thread(() -> {

            try {

                socket =
                        new Socket(
                                ip,
                                PORT
                        );

                connected = true;

                setupAudioTrack();

                listener.onConnected();

                BufferedInputStream input =
                        new BufferedInputStream(
                                socket.getInputStream()
                        );

                byte[] buffer =
                        new byte[4096];

                int bytesRead;

                while (
                        connected &&
                        (bytesRead =
                                input.read(buffer)) != -1
                ) {

                    if (recording) {

                        writeRecording(
                                buffer,
                                bytesRead
                        );
                    }

                    if (listening) {

                        audioTrack.write(
                                buffer,
                                0,
                                bytesRead
                        );
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                connected = false;

                stopAudio();

                stopRecording();

                listener.onDisconnected();
            }
        });

        receiverThread.start();
    }

    private void setupAudioTrack() {

        int bufferSize =
                AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        CHANNEL,
                        AUDIO_FORMAT
                );

        audioTrack =
                new AudioTrack.Builder()
                        .setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setUsage(
                                                AudioAttributes
                                                        .USAGE_MEDIA
                                        )
                                        .setContentType(
                                                AudioAttributes
                                                        .CONTENT_TYPE_SPEECH
                                        )
                                        .build()
                        )
                        .setAudioFormat(
                                new AudioFormat.Builder()
                                        .setEncoding(
                                                AUDIO_FORMAT
                                        )
                                        .setSampleRate(
                                                SAMPLE_RATE
                                        )
                                        .setChannelMask(
                                                CHANNEL
                                        )
                                        .build()
                        )
                        .setBufferSizeInBytes(
                                bufferSize * 2
                        )
                        .setTransferMode(
                                AudioTrack.MODE_STREAM
                        )
                        .build();

        audioTrack.play();
    }

    public boolean toggleListening() {

        listening = !listening;

        return listening;
    }

    public boolean toggleRecording() {

        recording = !recording;

        if (recording) {
            startRecording();
        } else {
            stopRecording();
        }

        return recording;
    }

    private void startRecording() {

        try {

            File directory =
                    new File(
                            getRecordingDirectory()
                    );

            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName =
                    "audio_" +
                    System.currentTimeMillis() +
                    ".pcm";

            File file =
                    new File(
                            directory,
                            fileName
                    );

            recordingOutput =
                    new FileOutputStream(file);

        } catch (Exception e) {

            e.printStackTrace();

            recording = false;
        }
    }

    private String getRecordingDirectory() {

        File base =
                Environment
                        .getExternalStoragePublicDirectory(
                                Environment
                                        .DIRECTORY_MUSIC
                        );

        File directory =
                new File(
                        base,
                        "AudioStream"
                );

        return directory.getAbsolutePath();
    }

    private void writeRecording(
            byte[] data,
            int length
    ) {

        if (recordingOutput == null) {
            return;
        }

        try {

            recordingOutput.write(
                    data,
                    0,
                    length
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void stopRecording() {

        if (recordingOutput != null) {

            try {
                recordingOutput.flush();
                recordingOutput.close();
            } catch (Exception ignored) {
            }

            recordingOutput = null;
        }
    }

    private void stopAudio() {

        if (audioTrack != null) {

            try {
                audioTrack.stop();
            } catch (Exception ignored) {
            }

            try {
                audioTrack.release();
            } catch (Exception ignored) {
            }

            audioTrack = null;
        }
    }

    public void disconnect() {

        connected = false;

        stopRecording();

        if (socket != null) {

            try {
                socket.close();
            } catch (Exception ignored) {
            }

            socket = null;
        }

        stopAudio();
    }
}
```
