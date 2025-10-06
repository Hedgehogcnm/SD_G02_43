package com.example.project2025;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.OutputStream;
import java.net.Socket;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AudioHelper {
    private static final String TAG = "AudioHelper";

    private Context context;

    private Thread streamThread;
    private boolean isStreaming = false;
    public AudioHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public void startStreaming(String serverIp, int port) {
        if (isStreaming) {
            Log.w(TAG, "Already streaming!");
            return;
        }

        isStreaming = true;
        streamThread = new Thread(() -> {
            int sampleRate = 48000;
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufSize
            );

            try (Socket socket = new Socket(serverIp, port);
                 OutputStream os = socket.getOutputStream()) {

                Log.d(TAG, "Started streaming to " + serverIp + ":" + port);
                byte[] buffer = new byte[minBufSize];

                audioRecord.startRecording();

                while (isStreaming) {
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                audioRecord.stop();
                audioRecord.release();
                Log.d(TAG, "Stopped streaming.");

            } catch (Exception e) {
                Log.e(TAG, "Streaming failed", e);
            }
        });
        streamThread.start();
    }

    public void stopStreaming() {
        if (!isStreaming) {
            Log.w(TAG, "Not currently streaming");
            return;
        }
        isStreaming = false;
    }
}
