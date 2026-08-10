```java
package com.manikanta.audiostream.sender;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioStreamer {

    private static final int PORT = 8988;

    private static final int SAMPLE_RATE = 16000;

    private static final int CHANNEL =
            AudioFormat.CHANNEL_IN_MONO;

    private static final int AUDIO_FORMAT =
            AudioFormat.ENCODING_PCM_16BIT;

    private volatile boolean running = false;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    private AudioRecord audioRecord;

    private Thread serverThread;

    public void start() {

        if (running) {
            return;
        }

        running = true;

        serverThread = new Thread(() -> {

            try {

                serverSocket =
                        new ServerSocket(PORT);

                while (running) {

                    clientSocket =
                            serverSocket.accept();

                    streamToClient(clientSocket);

                    try {
                        clientSocket.close();
                    } catch (Exception ignored) {
                    }

                    clientSocket = null;
                }

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                stop();
            }

        });

        serverThread.start();
    }

    private void streamToClient(Socket socket) {

        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE,
                        CHANNEL,
                        AUDIO_FORMAT
                );

        if (bufferSize <= 0) {
            bufferSize = SAMPLE_RATE;
        }

        audioRecord =
                new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        CHANNEL,
                        AUDIO_FORMAT,
                        bufferSize * 2
                );

        byte[] buffer =
                new byte[bufferSize];

        try {

            BufferedOutputStream output =
                    new BufferedOutputStream(
                            socket.getOutputStream(),
                            bufferSize * 2
                    );

            audioRecord.startRecording();

            while (
                    running &&
                    !socket.isClosed()
            ) {

                int bytesRead =
                        audioRecord.read(
                                buffer,
                                0,
                                buffer.length
                        );

                if (bytesRead > 0) {

                    output.write(
                            buffer,
                            0,
                            bytesRead
                    );

                    output.flush();
                }
            }

            try {
                output.close();
            } catch (Exception ignored) {
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            releaseRecorder();
        }
    }

    private void releaseRecorder() {

        if (audioRecord != null) {

            try {
                audioRecord.stop();
            } catch (Exception ignored) {
            }

            try {
                audioRecord.release();
            } catch (Exception ignored) {
            }

            audioRecord = null;
        }
    }

    public void stop() {

        running = false;

        releaseRecorder();

        if (clientSocket != null) {

            try {
                clientSocket.close();
            } catch (Exception ignored) {
            }

            clientSocket = null;
        }

        if (serverSocket != null) {

            try {
                serverSocket.close();
            } catch (Exception ignored) {
            }

            serverSocket = null;
        }
    }
}
```
