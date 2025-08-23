package com.example.project2025;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.shared.ChangePasswordActivity;
import com.example.project2025.shared.ChangeUsernameActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfile extends AppCompatActivity {

    private LinearLayout changeProfile, changeUsername, privacy, changePassword;
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
        setContentView(R.layout.activity_edit_profile);
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
        privacy = findViewById(R.id.privacy);
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
            Intent intent = new Intent(EditProfile.this, Change_image.class);
            startActivity(intent);
        });

        changeUsername.setOnClickListener(v -> {
            // Navigate to ChangeUsernameActivity
            Intent intent = new Intent(EditProfile.this, ChangeUsernameActivity.class);
            startActivity(intent);
        });

        changePassword.setOnClickListener(v -> {
            // Navigate to ChangePasswordActivity
            Intent intent = new Intent(EditProfile.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        privacy.setOnClickListener(v -> {
            // Show privacy dialog
            showPrivacyDialog();
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

    // Show privacy dialog
    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("We value your privacy. Your data will be handled securely and will not be shared without your consent.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    // Show logout confirmation dialog
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Sign out from Firebase
                    auth.signOut();
                    
                    // Navigate to SignInActivity
                    Intent intent = new Intent(EditProfile.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}