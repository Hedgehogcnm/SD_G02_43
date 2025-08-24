package com.example.project2025.shared;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.project2025.EditProfile;
import com.example.project2025.SignInActivity;
import com.example.project2025.databinding.AccountFragmentBinding;
import com.example.project2025.shared.AccountViewModel;
import com.example.project2025.ProfileImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    TextView username;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    private AccountFragmentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AccountViewModel notificationViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        binding = AccountFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageView createGearBtn = binding.gearButton;
        createGearBtn.setOnClickListener(v -> {
            // Navigate to EditProfile activity instead of showing SettingProfile bottom sheet
            Intent intent = new Intent(getActivity(), EditProfile.class);
            startActivity(intent);
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
        sharedPreferences = getActivity().getSharedPreferences("ROLE", MODE_PRIVATE);
        String role = sharedPreferences.getString("Role", "Users");
        // Initialize username and profile image
        DocumentReference userRef = db.collection(role).document(currentUser.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Display username
                    username = binding.menuUsername;
                    if (currentUser != null) {
                        username.setText(document.getString("name"));
                    } else {
                        username.setText("Please login first");
                        Intent intent = new Intent(getActivity(), SignInActivity.class);
                        startActivity(intent);
                    }
                    
                    // Load and display the user's selected profile image
                    // This retrieves the profilepic field from Firebase and displays the corresponding drawable
                    String profilePic = document.getString("profilepic");
                    ProfileImageHelper.loadProfileImage(getContext(), binding.changeImage, profilePic);
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