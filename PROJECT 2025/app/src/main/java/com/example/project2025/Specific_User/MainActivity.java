package com.example.project2025.Specific_User;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.project2025.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.project2025.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * This is the main activity for User
 * This activity is accessible to all users
 * Contains bottom navigation with dashboard, account menu, and feeder
 */

public class MainActivity extends AppCompatActivity {

    // Share Preferences
    private SharedPreferences sharedPreferences;
    // Firebase auth & Firestore
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and set layout first
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the bottom navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboard_fragment_user_navigation, R.id.account_fragment_navigation, R.id.feeder_fragment_navigation
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Get current user role
        sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
        sharedPreferences.edit().putString("Role", "Users").apply();

        // check login logic
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }
}

