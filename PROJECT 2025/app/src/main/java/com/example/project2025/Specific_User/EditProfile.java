package com.example.project2025.Specific_User;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.EditProfileLogics.Change_image;
import com.example.project2025.EditProfileLogics.ProfileImageHelper;
import com.example.project2025.R;
import com.example.project2025.SignIn_Login_Onboarding.SignInActivity;
import com.example.project2025.EditProfileLogics.ChangePasswordActivity;
import com.example.project2025.EditProfileLogics.ChangeUsernameActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfile extends AppCompatActivity {

    private LinearLayout changeProfile, changeUsername, changeFeederIP, changePassword;
    private ImageView profileImage;
    private TextView usernameText, userFeederIP;
    private ImageView returnButton;
    private com.google.android.material.button.MaterialButton logoutButton;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

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
        changeFeederIP = findViewById(R.id.feederIP);
        changePassword = findViewById(R.id.change_password);
        returnButton = findViewById(R.id.return_button);
        logoutButton = findViewById(R.id.setting_logout);
        userFeederIP = findViewById(R.id.user_feeder_ip);
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

        changeFeederIP.setOnClickListener(v -> {
            // Show Change IP dialog
            showChangeIPDialog();
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
                        String ip = document.getString("feeder_ip");
                        userFeederIP.setText(ip);
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

    // Show Change IP Dialog
    private void showChangeIPDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHANGE FEEDER IP");
        builder.setMessage("\nPlease enter your pet feeder's IP address");

        // Create the dialog layout with proper margins
        final EditText ipInput = new EditText(this);
        ipInput.setHint("Enter your new IP");
        ipInput.setInputType(InputType.TYPE_CLASS_TEXT);

        // Add margin to the EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10); // Left, Top, Right, Bottom padding
        layout.addView(ipInput);

        builder.setView(layout);

        // Set positive and negative buttons
        builder.setPositiveButton("Set", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipInput.getText().toString();

                if (TextUtils.isEmpty(ip)) {
                    Log.d("ChangeIP", "IP = " + ip);
                    Toast.makeText(getApplicationContext(), "Please enter your IP", Toast.LENGTH_SHORT).show();
                    return; // Don't dismiss dialog
                }

                DocumentReference userRef = db.collection("Users").document(currentUser.getUid());
                userRef.update("feeder_ip", ip).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("ChangeIP", "Update successful for UID: " + currentUser.getUid());
                            dialog.dismiss();
                            userFeederIP.setText(ip);
                            sharedPreferences = getSharedPreferences("FEEDERIP", MODE_PRIVATE);
                            sharedPreferences.edit().putString("feeder_ip", ip).apply();
                            Toast.makeText(getApplicationContext(), "Update Successfully", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.e("ChangeIP", "Update failed", task.getException());
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    
    // Show logout confirmation dialog
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Sign out from Firebase
                    auth.signOut();
                    
                   
                    Toast.makeText(EditProfile.this, "Sign Out Successful", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to SignInActivity
                    getSharedPreferences("ROLE", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(EditProfile.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}