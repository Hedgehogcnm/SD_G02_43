package com.example.project2025.ui.account_menu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project2025.SignInActivity;
import com.example.project2025.databinding.FragmentMenuBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class accountMenuFragment extends Fragment {

    TextView username, email;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    private FragmentMenuBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        accountMenuViewModel notificationViewModel =
                new ViewModelProvider(this).get(accountMenuViewModel.class);

        binding = FragmentMenuBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageView createGearBtn = binding.gear;
        createGearBtn.setOnClickListener(v -> {
            SettingProfile bottomSheet = new SettingProfile();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Initialize username
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Display Text
                    username = binding.menuUsername;
                    email = binding.menuEmail;
                    if (currentUser != null) {
                        email.setText(auth.getCurrentUser().getEmail());
                        username.setText(document.getString("name"));
                    } else {
                        username.setText("Please login first");
                        Intent intent = new Intent(getActivity(), SignInActivity.class);
                        startActivity(intent);
                    }
                }
                else {
                    Log.d("accountMenuFragment: ", "No such document");
                }
            }
            else {
                Log.d("accountMenuFragment: ", "get failed with ", task.getException());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}