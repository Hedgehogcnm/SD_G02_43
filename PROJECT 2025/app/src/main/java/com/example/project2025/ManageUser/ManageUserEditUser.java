package com.example.project2025.ManageUser;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project2025.R;
import com.example.project2025.EditProfileLogics.ProfileImageHelper;
import com.example.project2025.SignIn_Login_Onboarding.SignInActivity;
import com.example.project2025.Specific_Admin.AdminActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

public class ManageUserEditUser extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView usernameTextView, emailTextView, UidTextView;
    private Button deleteUserButton;
    private LinearLayout changeUsername;
    private ImageView profileImageView, returnButton;
    private FirebaseFirestore db;

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
            showDeleteDialog();
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

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + sharedPreferences.getString("name", "") + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteUser();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void deleteUser(){
        String uid = sharedPreferences.getString("uid", "");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "No UID found", Toast.LENGTH_SHORT).show();
            return;
        }
        String APIurl = "https://us-central1-divine-course-467504-m2.cloudfunctions.net/deleteUser";
        JSONObject payload = new JSONObject();
        try {
            payload.put("uid", uid);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, APIurl, payload,
                response -> {
                    Toast.makeText(this, "Deleted User", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                    startActivity(intent);
                },
                error -> {
                    if(error.getMessage() != null){
                        int statusCode = error.networkResponse.statusCode;
                        String responseBody = new String(error.networkResponse.data);
                        Toast.makeText(this, "Deleted User ", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Log.d("ManageUserEditUser", "Error: " + error.getMessage());
                    }
                });

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }
}