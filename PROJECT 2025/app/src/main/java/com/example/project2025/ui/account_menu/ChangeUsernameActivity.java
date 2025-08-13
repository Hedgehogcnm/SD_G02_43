package com.example.project2025.ui.account_menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.MainActivity;
import com.example.project2025.R;
import com.example.project2025.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChangeUsernameActivity extends AppCompatActivity {

    Button backButton, confirmButton;
    TextView currentName, newUsername;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.change_username);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeUsername(db, mAuth);

        backButton = findViewById(R.id.back_button);
        confirmButton = findViewById(R.id.confirm_button);
        currentName = findViewById(R.id.current_name);
        newUsername = findViewById(R.id.new_username);

        backButton.setOnClickListener(v -> {
            super.getOnBackPressedDispatcher();
            finish();
        });

        confirmButton.setOnClickListener(v -> {
           String newUsernameText = newUsername.getText().toString();

           if(!TextUtils.isEmpty(newUsernameText)){
                if(setNewUsername(newUsernameText, db, mAuth)){
                   Toast.makeText(this, "Username changed successfully", Toast.LENGTH_SHORT).show();
               }
           }
           else{
               Toast.makeText(this, "Please enter a new username first.", Toast.LENGTH_SHORT).show();
           }
        });
    }

    boolean setNewUsername(String newUsername, FirebaseFirestore db, FirebaseAuth mAuth){
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", newUsername);

        if(mAuth.getCurrentUser() != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("CURRENTUSER", MODE_PRIVATE);
            String documentID = sharedPreferences.getString("DocumentID", null);
            if(documentID != null) {
                DocumentReference userRef = db.collection("Users").document(documentID);
                userRef.update(userProfile).addOnSuccessListener(aVoid -> {
                    initializeUsername(db, mAuth);
                });
                return true;
            }
            else{
                Log.d("Account menu: ","Error getting current user's document");
                return false;
            }
        }
        else{
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    void initializeUsername(FirebaseFirestore db, FirebaseAuth mAuth){
        if(mAuth.getCurrentUser() != null) {
            CollectionReference usersRef = db.collection("Users");
            Query query = usersRef.whereEqualTo("email", mAuth.getCurrentUser().getEmail());
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (com.google.firebase.firestore.DocumentSnapshot document : task.getResult()) {
                        String name = document.getString("name");
                        currentName.setText(name);
                    }
                }
                else {
                    Log.d("Account menu: ","Error getting current user's name");
                }
            });
        }
        else{
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }
}