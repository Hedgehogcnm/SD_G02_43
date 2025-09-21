package com.example.project2025.Specific_Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.project2025.ManageUser.UserListFragment;
import com.example.project2025.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * AdminActivity - Main admin interface for the application
 * This activity is only accessible to admin users (kaifeedcat@gmail.com)
 * Contains bottom navigation with Dashboard and Edit Profile fragments
 */
public class AdminActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Save role
        sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
        sharedPreferences.edit().putString("Role", "Admin").apply();

        // Setup bottom navigation with NavController
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.admin_nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}
