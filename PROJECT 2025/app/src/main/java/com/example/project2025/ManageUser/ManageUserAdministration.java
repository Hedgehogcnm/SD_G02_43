package com.example.project2025.ManageUser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.project2025.R;
import com.example.project2025.Specific_Admin.AdminActivity;
import com.example.project2025.databinding.ActivityMainBinding;
import com.example.project2025.databinding.ManageUserAdministrationBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManageUserAdministration extends AppCompatActivity {

    private TextView usernameTextView;
    private ImageView returnButton;
    private SharedPreferences sharedPreferences;
    private ManageUserAdministrationBinding binding;
    private String username, email, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ManageUserAdministrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.manage_user_admin_dashboard_navigation, R.id.manage_user_admin_feeder_navigation
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Initialize UI elements
        usernameTextView = findViewById(R.id.username);
        returnButton = findViewById(R.id.returnButton);

        returnButton.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPreferences = getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);
        username = sharedPreferences.getString("name", "username");
        email = sharedPreferences.getString("email", "email");
        uid = sharedPreferences.getString("uid", "uid");
        usernameTextView.setText(username);
    }
}