package com.example.project2025.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project2025.R;

import com.example.project2025.databinding.FragmentDashboardUserBinding;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class DashboardUserFragment extends Fragment {

    private FragmentDashboardUserBinding binding;
    private Button feedButton;
    private static final String PI_IP = "192.168.135.157";
    private static final int PORT = 12345;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardUserViewModel homeViewModel =
                new ViewModelProvider(this).get(DashboardUserViewModel.class);

        View root = inflater.inflate(R.layout.dashboard_fragment_user, container, false);

        feedButton = root.findViewById(R.id.feedButton);

        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedCommand();
            }
        });

        return root;
    }

    private void sendFeedCommand() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(PI_IP, PORT);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}