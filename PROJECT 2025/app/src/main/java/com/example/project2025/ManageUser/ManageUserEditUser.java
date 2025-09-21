package com.example.project2025.ManageUser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project2025.R;
import com.example.project2025.EditProfileLogics.ProfileImageHelper;
import com.example.project2025.SignIn_Login_Onboarding.SignInActivity;
import com.example.project2025.Specific_Admin.AdminActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

public class ManageUserEditUser extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView usernameTextView, emailTextView, feederIPTextView;
    private Button deleteUserButton;
    private LinearLayout changeUsername, changeIP;
    private ImageView profileImageView, returnButton;
    private FirebaseFirestore db;
    private String uid;

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
        changeIP = findViewById(R.id.feeder_ip);
        usernameTextView = findViewById(R.id.current_name);
        emailTextView = findViewById(R.id.email_text);
        feederIPTextView = findViewById(R.id.feeder_ip_text);
        profileImageView = findViewById(R.id.profile_image);
        deleteUserButton = findViewById(R.id.delete_user);

        changeUsername.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ManageUserChangeName.class);
            startActivity(intent);
        });

        changeIP.setOnClickListener(v -> {
            showChangeIPDialog();
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
        feederIPTextView.setText(sharedPreferences.getString("feeder_ip", ""));
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

    // API for delete User from Firestore and Firebase auth
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

        // Genuinely no idea whats going on with this API call, but it works
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, APIurl, payload,
                response -> {
                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                    startActivity(intent);
                },
                error -> {
                    if(error.getMessage() != null){
                        int statusCode = error.networkResponse.statusCode;
                        String responseBody = new String(error.networkResponse.data);
                        Toast.makeText(this, "Error: " + statusCode + " " + responseBody, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                        startActivity(intent);
                    }
                });

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void showChangeIPDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHANGE FEEDER IP");
        builder.setMessage("\nPlease enter your pet feeder's IP address");

        // Create the dialog layout with proper margins
        final EditText ipInput = new EditText(this);
        ipInput.setHint("Enter your new ip");
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
                    Toast.makeText(getApplicationContext(), "Please enter your ip", Toast.LENGTH_SHORT).show();
                    return; // Don't dismiss dialog
                }

                DocumentReference userRef = db.collection("Users").document(sharedPreferences.getString("uid", ""));
                userRef.update("feeder_ip", ip).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("ChangeIP", "Update successful for UID: " + sharedPreferences.getString("uid", ""));
                            dialog.dismiss();
                            feederIPTextView.setText(ip);
                            sharedPreferences = getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);
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
}