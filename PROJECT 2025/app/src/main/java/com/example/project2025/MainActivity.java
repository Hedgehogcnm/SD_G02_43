package com.example.project2025;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.project2025.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    // Share Preferences
    private SharedPreferences sharedPreferences;
    // Firebase auth & Firestore
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    // Debug switch — true: skip login, false: need login
    private static final boolean DEBUG_SKIP_LOGIN = true;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the bottom navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_feeder, R.id.navigation_menu
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // check login logic
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (!DEBUG_SKIP_LOGIN && user == null) {
            // If it is not debug mode and there is no logged in user -> jump to the login page
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            finish();
        }

        /*
        Using Shared Preferences to share data across the whole application
        My reason for using this is to minimize the number of queries to the database
        which could save a lot of time and also our quota later on
        For example: If we have to query 10x in other operations later on,
        instead of O(n * 10), we can just use O(n + 10) based on what I understand
         */

        // Initialize Shared Preferences
        db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        Query query = usersRef.whereEqualTo("email", user.getEmail());

        // Reference object for uid and document for extracting data from firestore
        var ref = new Object() {
            String uid = "";
            String documentID = "";
        };
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()){
                    ref.uid = document.getString("uid");
                    ref.documentID = document.getId();
                    Log.d("Account menu: ","Item logged, Current user's name: " + document.getString("name"));
                }

            }
            else {
                Log.d("Account menu: ","Error getting current user's name");
            }
        });

        Log.d("MY USER IS ","user: " + user);
        Log.d("MY EMAIL IS ","email: " + user.getEmail());
        Log.d("MY UID IS ","uid: " + ref.uid);
        Log.d("MY DOCUMENT ID IS ","documentID: " + ref.documentID);

        // Inserting uid and documentID into shared preferences
        sharedPreferences = getSharedPreferences("CURRENT_USER", MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("uid", ref.uid)
                .putString("DocumentID", ref.documentID)
                .apply();
    }
}
