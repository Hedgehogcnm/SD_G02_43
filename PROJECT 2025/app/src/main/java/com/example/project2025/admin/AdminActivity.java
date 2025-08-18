package com.example.project2025.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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

        // Get current user role
        sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
        sharedPreferences.edit().putString("Role", "Admin").apply();
        // ===== BOTTOM NAVIGATION SETUP =====
        // Initialize the bottom navigation bar for admin interface
        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            
            // Handle navigation between admin fragments
            if (item.getItemId() == R.id.nav_dashboard) {
                // Switch to Dashboard fragment (empty for now)
                selectedFragment = new DashboardFragment();
            } else if (item.getItemId() == R.id.nav_edit_profile) {
                // Switch to Edit Profile fragment (same as user side)
                selectedFragment = new EditProfileFragment();
            }
            
            // Replace the current fragment with the selected one
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, selectedFragment)
                    .commit();
                return true;
            }
            return false;
        });

        // ===== DEFAULT FRAGMENT =====
        // Set Dashboard as the default fragment when admin first opens the app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, new DashboardFragment())
                .commit();
        }
    }
}
