package com.example.project2025.ManageUser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.R;

public class ManageUserAdministration extends AppCompatActivity {

    private TextView usernameTextView;
    private Button editUserButton;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manage_user_administration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameTextView = findViewById(R.id.username);
        editUserButton = findViewById(R.id.edit_button);

        editUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ManageUserEditUser.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPreferences = getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);
        String username = sharedPreferences.getString("name", "username");
        usernameTextView.setText(username);
    }
}