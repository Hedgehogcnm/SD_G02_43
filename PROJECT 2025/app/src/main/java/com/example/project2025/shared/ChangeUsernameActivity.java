package com.example.project2025.shared;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.R;
import com.example.project2025.SignInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangeUsernameActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Button backButton, confirmButton;
    TextView currentName, newUsername;
    FirebaseAuth auth;
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

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
        String role = sharedPreferences.getString("Role", "Users");
        initializeUsername(db, auth, role);

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
               setNewUsername(newUsernameText, db, auth, role);
               initializeUsername(db, auth, role);
               newUsername.setText("");
           }
           else{
               Toast.makeText(this, "Please enter a new username first.", Toast.LENGTH_SHORT).show();
           }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
        String role = sharedPreferences.getString("ROLE", "Users");
        initializeUsername(db, auth, role);
    }

    void setNewUsername(String newUsername, FirebaseFirestore db, FirebaseAuth mAuth, String collection){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            DocumentReference userRef = db.collection(collection).document(currentUser.getUid());
            userRef.update("name", newUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d("ChangeUsername", "Update successful for UID: " + currentUser.getUid());
                        Toast.makeText(getApplicationContext(), "Username changed successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.e("ChangeUsername", "Update failed", task.getException());
                        Toast.makeText(getApplicationContext(), "Please enter a new username first.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Log.e("ChangeUsername", "Current user is NULL. Cannot update username.");
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
        }
    }

    void initializeUsername(FirebaseFirestore db, FirebaseAuth mAuth, String collection){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            DocumentReference userRef = db.collection(collection).document(currentUser.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        currentName.setText(name);
                    } else {
                        Log.d("Account menu: ", "No such document");
                    }
                } else {
                    Log.d("Account menu: ", "Error getting current user's name", task.getException());
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