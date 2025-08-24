package com.example.project2025.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project2025.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * DashboardFragment - Admin dashboard interface
 * Currently empty as requested by user
 * This fragment will be used for admin-specific features in the future
 */
public class DashboardAdminFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView userCount;
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
        userCount = root.findViewById(R.id.user_count);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserCount();

        manualFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip_address_text = ip_address.getText().toString();
                sendFeedCommand(ip_address_text);
            }
        });
    }

    private void getUserCount(){
        Query query = db.collection("Users");
        AggregateQuery countQuery = query.count();
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Count fetched successfully
                    AggregateQuerySnapshot snapshot = task.getResult();
                    userCount.setText(String.valueOf(snapshot.getCount()));
                } else {
                    // Error occurred while fetching count
                    Exception exception = task.getException();
                    Log.d("TAG", "Error getting count", exception);
                }
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
