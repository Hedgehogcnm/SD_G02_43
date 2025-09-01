package com.example.project2025.SignIn_Login_Onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.R;
import com.example.project2025.Specific_Admin.AdminActivity;
import com.example.project2025.Specific_User.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Onboarding extends AppCompatActivity {

    MaterialButton login, register;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private String role;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        login = findViewById(R.id.onboarding_logInButton);
        register = findViewById(R.id.onboarding_RegisterButton);

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
        role = sharedPreferences.getString("Role", null);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            proceedAfterLogin(currentUser, role);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Onboarding.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Onboarding.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void proceedAfterLogin(FirebaseUser user, String role) {
        if (user.isEmailVerified()) {
            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
            Intent intent;
            if ("Admin".equals(role)) {
                intent = new Intent(getApplicationContext(), AdminActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), MainActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Please verify your email first.", Toast.LENGTH_LONG).show();
            user.sendEmailVerification();
            mAuth.signOut();
        }
    }
}