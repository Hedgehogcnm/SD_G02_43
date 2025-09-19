package com.example.project2025.Specific_Admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.EditProfileLogics.ChangePasswordActivity;
import com.example.project2025.EditProfileLogics.ChangeUsernameActivity;
import com.example.project2025.EditProfileLogics.Change_image;
import com.example.project2025.EditProfileLogics.ProfileImageHelper;
import com.example.project2025.R;
import com.example.project2025.SignIn_Login_Onboarding.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminEditProfile extends AppCompatActivity {

    private LinearLayout changeProfile, changeUsername, changeFeederIP, changePassword;
    private ImageView profileImage;
    private TextView usernameText;
    private ImageView returnButton;
    private com.google.android.material.button.MaterialButton logoutButton;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        profileImage = findViewById(R.id.profile_picture);
        changeProfile = findViewById(R.id.edit_profile_picture);
        usernameText = findViewById(R.id.current_name);
        changeUsername = findViewById(R.id.change_name);
        changePassword = findViewById(R.id.change_password);
        returnButton = findViewById(R.id.return_button);
        logoutButton = findViewById(R.id.setting_logout);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set up click listeners
        changeProfile.setOnClickListener(v -> {
            // Navigate to Change_image activity
            Intent intent = new Intent(getApplicationContext(), Change_image.class);
            startActivity(intent);
        });

        changeUsername.setOnClickListener(v -> {
            // Navigate to ChangeUsernameActivity
            Intent intent = new Intent(getApplicationContext(), ChangeUsernameActivity.class);
            startActivity(intent);
        });

        changePassword.setOnClickListener(v -> {
            // Navigate to ChangePasswordActivity
            Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        returnButton.setOnClickListener(v -> {
            // Return to previous screen
            finish();
        });

        logoutButton.setOnClickListener(v -> {
            // Show logout confirmation dialog
            showLogoutDialog();
        });

        // Load user data
        loadUserData();
    }

    private void loadUserData() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            SharedPreferences sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
            String role = sharedPreferences.getString("Role", "Users");
            DocumentReference userRef = db.collection(role).document(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Set username
                        String username = document.getString("name");
                        usernameText.setText(username);

                        // Load profile image
                        String profilePic = document.getString("profilepic");
                        if (profilePic != null) {
                            ProfileImageHelper.loadProfileImage(this, profileImage, profilePic);
                        }
                    }
                }
            });
        }
    }

    // Show logout confirmation dialog
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Sign out from Firebase
                    auth.signOut();


                    Toast.makeText(getApplicationContext(), "Sign Out Successful", Toast.LENGTH_SHORT).show();

                    // Navigate to SignInActivity
                    getSharedPreferences("ROLE", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}