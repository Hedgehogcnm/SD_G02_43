package com.example.project2025;

/**
 * Login screen for existing users. Enforces that only email-verified accounts
 * can access the main app. New users can navigate to Register from here.
 *
 * Flow:
 * - If already signed in onStart: proceed only if email is verified; otherwise prompt and sign out
 * - On login button: authenticate via Firebase Auth email/password; allow into MainActivity
 *   only if user.isEmailVerified() returns true. If not verified, resend verification and sign out.
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.admin.AdminActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", Pattern.CASE_INSENSITIVE);
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView, forgotPasswordTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String role;

    @Override
    public void onStart() {
        super.onStart();
        // Auto-redirect removed - users must manually log in each time
        // This prevents immediate redirect to MainActivity when opening the app
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sign_in);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.logInButton);
        registerTextView = findViewById(R.id.registerTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPasswordDialog();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(SignInActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!validate(email)) {
                    Toast.makeText(SignInActivity.this, "Invalid email format", Toast.LENGTH_LONG).show();
                    return;
                }

                AtomicBoolean userExistInDb = new AtomicBoolean(false);


                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                DocumentReference adminRef = db.collection("Admin").document(user.getUid());
                                DocumentReference userRef = db.collection("Users").document(user.getUid());

                                adminRef.get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (task1.getResult().exists()) {
                                            role = "Admin";
                                            proceedAfterLogin(user, role);
                                        } else {
                                            userRef.get().addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    if (task2.getResult().exists()) {
                                                        role = "User";
                                                        proceedAfterLogin(user, role);
                                                    } else {
                                                        // To Resolve Ghost user
                                                        role = "User";
                                                        Map<String, Object> userProfile = new HashMap<>();
                                                        userProfile.put("name", "Please change again.");
                                                        userProfile.put("email", user.getEmail());
                                                        userProfile.put("uid", user.getUid());
                                                        userProfile.put("profilepic", "desperate_dog.jpg");
                                                        userProfile.put("createdAt", System.currentTimeMillis());

                                                        db.collection("Users").document(user.getUid())
                                                                .set(userProfile, SetOptions.merge())
                                                                .addOnSuccessListener(aVoid -> proceedAfterLogin(user, role))
                                                                .addOnFailureListener(e ->
                                                                        Toast.makeText(SignInActivity.this, "Failed to create profile", Toast.LENGTH_SHORT).show()
                                                                );
                                                    }
                                                } else {
                                                    Log.e("Firestore", "Error checking Users collection", task2.getException());
                                                }
                                            });
                                        }
                                    } else {
                                        Toast.makeText(SignInActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            CollectionReference adminRefExist = db.collection("Admin");
                            CollectionReference userRefExist = db.collection("Users");

                            Log.d("Debug", "Checking if user exists: " + email);

                            adminRefExist.whereEqualTo("email", email).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    if (!task1.getResult().isEmpty()) {
                                        // Email found in Admin collection but password is wrong
                                        Toast.makeText(SignInActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // If not found in Admin, check in Users
                                        userRefExist.whereEqualTo("email", email).get().addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                if (!task2.getResult().isEmpty()) {
                                                    // Email found in Users collection but password is wrong
                                                    Toast.makeText(SignInActivity.this, "Your password is incorrect", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    // Email not found in either collection
                                                    Toast.makeText(SignInActivity.this, "Please register your email", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Log.e("Firestore", "Error checking Users collection", task2.getException());
                                            }
                                        });
                                    }
                                } else {
                                    Log.e("Firestore", "Error checking Admin collection", task1.getException());
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("RESET PASSWORD");
        builder.setMessage("\nEnter your email address to receive a password reset link");

        // Create the dialog layout with proper margins
        final EditText emailInput = new EditText(this);
        emailInput.setHint("Enter your email address");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Add margin to the EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10); // Left, Top, Right, Bottom padding
        layout.addView(emailInput);

        builder.setView(layout);

        builder.setPositiveButton("Reset", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                String email = emailInput.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SignInActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!validate(email)) {
                    Toast.makeText(SignInActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Send password reset email
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this,
                                            "Password reset email sent! Check your inbox.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SignInActivity.this,
                                            "Failed to send reset email. Please check your email address.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }
    private void proceedAfterLogin(FirebaseUser user, String role) {
        if (user.isEmailVerified()) {
            Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent intent;
            if ("Admin".equals(role)) {
                intent = new Intent(getApplicationContext(), AdminActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), MainActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignInActivity.this, "Please verify your email first.", Toast.LENGTH_LONG).show();
            user.sendEmailVerification();
            mAuth.signOut();
        }
    }
}