package com.example.project2025.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.project2025.R;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * DashboardFragment - Admin dashboard interface
 * Currently empty as requested by user
 * This fragment will be used for admin-specific features in the future
 */
public class DashboardAdminFragment extends Fragment {

    EditText ip_address ;
    Button manualFeed;
    private static final int PORT = 12345;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Currently shows "Admin Dashboard" and "(Empty for now)" text
        View root = inflater.inflate(R.layout.dashboard_fragment_admin, container, false);

        ip_address = root.findViewById(R.id.ip_address);
        manualFeed = root.findViewById(R.id.manual_feed);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        manualFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip_address_text = ip_address.getText().toString();
                sendFeedCommand(ip_address_text);
            }
        });
    }

    private void sendFeedCommand(String ip_address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ip_address, PORT);
                    OutputStream output = socket.getOutputStream();
                    output.write("Feed".getBytes());
                    output.flush();
                    socket.close();


                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Feeding time !", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Failed becauuse of : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
