package com.example.project2025.Feeder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.Socket;

public class FeedingAlarmReceiver extends BroadcastReceiver {
    private static final int PORT = 12345;

    @Override
    public void onReceive(Context context, Intent intent) {
        String ip = context.getSharedPreferences("feeder", 0).getString("feeder_ip", null);
        if (ip == null || ip.isEmpty()) {
            Toast.makeText(context, "Feeder IP not set", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Socket socket = new Socket(ip, PORT);
                OutputStream output = socket.getOutputStream();
                output.write("Feed".getBytes());
                output.flush();
                socket.close();
            } catch (Exception ignored) {
            }
        }).start();
    }
}




