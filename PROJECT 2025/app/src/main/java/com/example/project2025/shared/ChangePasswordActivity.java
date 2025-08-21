package com.example.project2025.shared;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    Button confirmButton;
    EditText oldPassword, newPassword, confirmPassword;
    //TextView userEmail;
    FirebaseAuth auth;
    ImageView backButton;
    TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backButton = findViewById(R.id.returnButton);
        confirmButton = findViewById(R.id.confirm_button);
        newPassword = findViewById(R.id.new_password);
        oldPassword = findViewById(R.id.old_password);
        confirmPassword = findViewById(R.id.confirm_password);
        userEmail = findViewById(R.id.user_email);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        backButton.setOnClickListener(v -> {
            super.getOnBackPressedDispatcher();
            finish();
        });

        // Initialize email text
        if (currentUser != null) {
            userEmail.setText(auth.getCurrentUser().getEmail());
        } else {
            userEmail.setText("Please login first");
        }

        confirmButton.setOnClickListener(v ->{
            String newPasswordText = newPassword.getText().toString();
            String confirmPasswordText = confirmPassword.getText().toString();

            if(currentUser != null){
                // Check new password length
                if (newPasswordText.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
                    return;
                }

                // Reauth the user
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword.getText().toString());
                currentUser.reauthenticate(credential).addOnCompleteListener(taskAuth -> {
                    if(taskAuth.isSuccessful()){
                        Log.d("Reauth User: ", "Re-authentication successful");

                        if(newPasswordText.equals(confirmPasswordText)){
                            currentUser.updatePassword(newPasswordText).addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    newPassword.setText("");
                                    confirmPassword.setText("");
                                    Toast.makeText(getApplicationContext() ,"Password successfully changed!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    newPassword.setText("");
                                    confirmPassword.setText("");
                                    Toast.makeText(getApplicationContext() ,"Fail to change password", Toast.LENGTH_SHORT).show();
                                }
                                Log.d("Debug: ", currentUser.getEmail());
                            });
                        }
                        else{
                            Toast.makeText(getApplicationContext() ,"Password doesn't match", Toast.LENGTH_SHORT).show();                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext() ,"Wrong old password", Toast.LENGTH_SHORT).show();
                        Log.d("Reauth User: ", "Re-authentication Fail");
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext() ,"Please login first", Toast.LENGTH_SHORT).show();
            }
        });
    }
}