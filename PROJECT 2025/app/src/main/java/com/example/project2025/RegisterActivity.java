package com.example.project2025;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registration screen using Firebase Authentication (email/password) with enforced
 * email verification before granting access to the main app. The flow is:
 * - User enters name, email, password → taps Register
 * - Account is created in Firebase Auth and a verification email is sent
 * - Screen stays here and shows actions: "I've verified" and "Resend verification"
 * - After the user verifies via the email link and taps "I've verified",
 *   we confirm verification, ensure a profile doc exists at Users/{uid} in Firestore,
 *   then navigate to MainActivity
 *
 * Notes:
 * - RegisterActivity is always the launcher; we do not auto-redirect on start
 * - Firestore profile creation happens after verification to avoid unverified profiles
 */
public class RegisterActivity extends AppCompatActivity {

    // UI references
    EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText, OTPEditText;
    Button registerButton;
    Button verifiedContinueButton;
    Button resendVerificationButton;
    TextView loginTextView;

    // Firebase clients
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);

        //Firebase logics
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Initialize all views
        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.registerButton);
        verifiedContinueButton = findViewById(R.id.verifiedContinueButton);
        resendVerificationButton = findViewById(R.id.resendVerificationButton);
        loginTextView = findViewById(R.id.loginTextView);

        resendVerificationButton.setVisibility(View.GONE);
        verifiedContinueButton.setVisibility(View.GONE);
    }

    protected void onStart(){
        super.onStart();

        // Navigate to the login screen when the user taps the Login text
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle registration: validate inputs then create the Auth user
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                if(TextUtils.isEmpty(name)){
                    Toast.makeText(RegisterActivity.this, "Please enter your name", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!validate(email)){
                    Toast.makeText(RegisterActivity.this, "Please enter a valid email format", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(confirmPassword)){
                    Toast.makeText(RegisterActivity.this, "Please enter your confirm password", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!password.equals(confirmPassword)){
                    Toast.makeText(RegisterActivity.this, "Please enter matching passwords", Toast.LENGTH_LONG).show();
                    return;
                }

                // Create account directly
                if(isPasswordValid(password))
                {
                    registerUser(name, email, password);
                }
            }
        });

        // After the user clicks the verification link in their email, they tap this button.
        // We reload the Auth user, confirm email is verified, ensure a Firestore profile exists,
        // then proceed into the app.
        verifiedContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    mAuth.getCurrentUser().reload().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && mAuth.getCurrentUser().isEmailVerified()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            String email = mAuth.getCurrentUser().getEmail();
                            String name = nameEditText.getText().toString();

                            db.collection("Users").document(uid).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot snapshot) {
                                            if (!snapshot.exists()) {
                                                Map<String, Object> userProfile = new HashMap<>();
                                                userProfile.put("name", name);
                                                userProfile.put("email", email);
                                                userProfile.put("uid", uid);
                                                userProfile.put("profilepic", "desperate_dog.jpg");
                                                userProfile.put("createdAt", System.currentTimeMillis());
                                                db.collection("Users").document(uid).set(userProfile, SetOptions.merge())
                                                        .addOnSuccessListener(aVoid -> proceedToMain())
                                                        .addOnFailureListener(e -> proceedToMain());
                                            } else {
                                                proceedToMain();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> proceedToMain());
                        } else {
                            Toast.makeText(RegisterActivity.this, "Email not verified yet.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Please check your email and verify, then log in.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Resend verification email for users who didn't receive it the first time
        resendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    mAuth.getCurrentUser().sendEmailVerification();
                    Toast.makeText(RegisterActivity.this, "Verification email resent.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Create your account first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Removed auto-redirect in onStart to keep Register screen visible until user proceeds

    /**
     * Creates a Firebase Auth user and sends a verification email. The UI stays
     * on this screen and reveals verification helper actions. Firestore profile
     * creation is deferred until verification is confirmed.
     */
    private void registerUser(String name, String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Send email verification and wait; profile will be created after verification
                            if (mAuth.getCurrentUser() != null) {
                                mAuth.getCurrentUser().sendEmailVerification();
                            }
                            Toast.makeText(RegisterActivity.this, "A verification email was sent please check your spam folder", Toast.LENGTH_LONG).show();
                            verifiedContinueButton.setVisibility(View.VISIBLE);
                            resendVerificationButton.setVisibility(View.VISIBLE);
                        } else {
                            Exception e = task.getException();

                            if(e instanceof FirebaseAuthWeakPasswordException){
                                Toast.makeText(RegisterActivity.this, task.getException() != null ? task.getException().getMessage() : "Authentication failed.", Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(RegisterActivity.this, task.getException() != null ? task.getException().getMessage() : "Authentication failed.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Navigate to the main app experience after verification and profile ensuring.
     */
    private void proceedToMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < 8 || password.length() > 30) {
            Toast.makeText(RegisterActivity.this, "The password must be at least 8 characters and 1 symbol", Toast.LENGTH_LONG).show();
            return false;
        }
        else if (!password.matches(".*[!@#$%^&*()_+<>=?,.{}/-].*")) {
            Toast.makeText(RegisterActivity.this, "The password must be at least 8 characters and 1 symbol", Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            return true;
        }
    }
}