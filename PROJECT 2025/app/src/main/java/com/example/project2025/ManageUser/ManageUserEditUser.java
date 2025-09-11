package com.example.project2025.ManageUser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.R;
import com.example.project2025.EditProfileLogics.ProfileImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ManageUserEditUser extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView usernameTextView, emailTextView, UidTextView;
    private Button deleteUserButton;
    private LinearLayout changeUsername;
    private ImageView profileImageView, returnButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manage_user_user_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        returnButton = findViewById(R.id.returnButton);
        changeUsername = findViewById(R.id.change_name);
        usernameTextView = findViewById(R.id.current_name);
        emailTextView = findViewById(R.id.email_text);
        UidTextView = findViewById(R.id.uid_text);
        profileImageView = findViewById(R.id.profile_image);
        deleteUserButton = findViewById(R.id.delete_user);

        changeUsername.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ManageUserChangeName.class);
            startActivity(intent);
        });

        returnButton.setOnClickListener(v -> {
            finish();
        });

        deleteUserButton.setOnClickListener(v->{
            String uid = sharedPreferences.getString("uid", "");
            String APIurl = 
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeText();
        initializeProfileImage();
    }

    private void initializeText(){
        usernameTextView.setText(sharedPreferences.getString("name", ""));
        emailTextView.setText(sharedPreferences.getString("email", ""));
        UidTextView.setText(sharedPreferences.getString("uid", ""));
    }

    private void initializeProfileImage(){
        DocumentReference userRef = db.collection("users").document(sharedPreferences.getString("uid", ""));
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentProfilePic = documentSnapshot.getString("profilepic");

                if (currentProfilePic != null && !currentProfilePic.isEmpty()) {
                    ProfileImageHelper.loadProfileImage(this, profileImageView, currentProfilePic);
                } else {
                    ProfileImageHelper.loadProfileImage(this, profileImageView, null);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("ManageUserEditUser", "Error loading profile image", e);
        });
    }
}