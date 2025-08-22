package com.example.project2025.admin;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project2025.R;
import com.example.project2025.EditProfile;
import com.example.project2025.SignInActivity;
import com.example.project2025.shared.SettingProfile;
import com.example.project2025.ProfileImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * EditProfileFragment - Admin profile management interface
 * This fragment provides the same functionality as the user profile settings
 * Allows admin to manage their own profile (username, password, language, etc.)
 * Copied from user-side SettingProfile for consistency
 */
public class EditProfileFragment extends Fragment {

    TextView username;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);

        Log.d("EditProfileFragment", "Fragment Created");

        // ===== CANCEL BUTTON =====
        // When admin clicks cancel, return to the Dashboard fragment
        ImageView createGearBtn = view.findViewById(R.id.gear_button);
        createGearBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfile.class);
            startActivity(intent);
        });

        username = view.findViewById(R.id.menu_username);
        return view;
    }

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
                    if (currentUser != null) {
                        username.setText(document.getString("name"));
                    } else {
                        username.setText("Please login first");
                        Intent intent = new Intent(getActivity(), SignInActivity.class);
                        startActivity(intent);
                    }
                    
                    // Load and display the admin user's selected profile image
                    // This retrieves the profilepic field from Firebase and displays the corresponding drawable
                    String profilePic = document.getString("profilepic");
                    ImageView profileImageView = getView().findViewById(R.id.imageView6);
                    ProfileImageHelper.loadProfileImage(getContext(), profileImageView, profilePic);
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
}


