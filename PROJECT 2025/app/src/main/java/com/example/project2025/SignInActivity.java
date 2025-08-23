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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView registerTextView, forgotPasswordTextView;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String role;

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

                if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                    Toast.makeText(SignInActivity.this, "Please fill in all the fields", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!validate(email)){
                    Toast.makeText(SignInActivity.this, "Invalid email format", Toast.LENGTH_LONG).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        DocumentReference userRef = db.collection("Admin").document(user.getUid());
                                        userRef.get().addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                if (task1.getResult().exists()) {
                                                    role = "Admin";
                                                }
                                                else{
                                                    role = "User";
                                                }

                                                if (user.isEmailVerified()) {
                                                    Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                    if(role.equals("Admin")){
                                                        Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                    else{
                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                } else {
                                                    Toast.makeText(SignInActivity.this, "Please verify your email first.", Toast.LENGTH_LONG).show();
                                                    user.sendEmailVerification();
                                                    mAuth.signOut();
                                                }
                                            }
                                            else{
                                                Toast.makeText(SignInActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(SignInActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", Pattern.CASE_INSENSITIVE);

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
}