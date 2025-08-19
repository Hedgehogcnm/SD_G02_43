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
import com.example.project2025.SignInActivity;
import com.example.project2025.ui.account_menu.ChangeUsernameActivity;
import com.example.project2025.ui.account_menu.ChangePasswordActivity;
import com.example.project2025.ui.account_menu.SettingProfile;
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

    TextView username, email;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        Log.d("EditProfileFragment", "Fragment Created");

        // ===== CANCEL BUTTON =====
        // When admin clicks cancel, return to the Dashboard fragment
        ImageView createGearBtn = view.findViewById(R.id.gear);
        createGearBtn.setOnClickListener(v -> {
            SettingProfile bottomSheet = new SettingProfile();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });

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
        // Initialize username
        DocumentReference userRef = db.collection(role).document(currentUser.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Display Text
                    username = getView().findViewById(R.id.menu_username);
                    email = getView().findViewById(R.id.menu_email);
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
}


