package com.example.project2025.ManageUser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManageUserChangeName extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private Button confirmButton;
    private TextView currentNameTextView, newUsernameTextView;
    private ImageView returnButtonImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manage_user_change_name);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);

        confirmButton = findViewById(R.id.confirm_button);
        currentNameTextView = findViewById(R.id.current_name);
        newUsernameTextView = findViewById(R.id.new_username);
        returnButtonImageView = findViewById(R.id.returnButton);

        confirmButton.setOnClickListener(v -> {
            String newUsername = newUsernameTextView.getText().toString();
            if (!newUsername.isEmpty()) {
                updateUsername(newUsername);
            }
        });

        returnButtonImageView.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        initializeUsername();
    }

    private void initializeUsername(){
        currentNameTextView.setText(sharedPreferences.getString("name", ""));
    }

    private void updateUsername(String newName){
        String uid = sharedPreferences.getString("uid", "");
        db.collection("Users").document(uid).update("name", newName).addOnSuccessListener(aVoid -> {
            sharedPreferences.edit().putString("name", newName).apply();
            Toast.makeText(getApplicationContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Log.d("ManageUserChangeName", "Failed to update username");
            Toast.makeText(getApplicationContext(), "Failed to update username", Toast.LENGTH_SHORT).show();
        });
    }
}
