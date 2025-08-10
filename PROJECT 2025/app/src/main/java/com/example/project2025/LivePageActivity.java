package com.example.project2025;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LivePageActivity extends AppCompatActivity {

    private String ipAddress;
    private String HTTPport = "8889";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private WebView ipCamera;
    private Button photoButton, audioButton, videoButton, feedButton, feedRecordButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.livepage);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Get user email and ip of the pet feeder
        String userEmail = mAuth.getCurrentUser().getEmail();
        CollectionReference userRef = db.collection("Users");
        Query query = userRef.whereEqualTo("email", userEmail);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if(!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Log.d("FireStore", "ip address founded: " + document.getString("ip"));
                            ipAddress = document.getString("ip");
                        }
                    }
                }
                else{
                    Log.d("FireStore","Query failed", task.getException());
                }
            }
        });

        //Initialize widgets
        ipCamera = findViewById(R.id.ipCamera);
        photoButton = findViewById(R.id.photoButton);
        audioButton = findViewById(R.id.audioButton);
        videoButton = findViewById(R.id.videoButton);
        feedButton = findViewById(R.id.feedButton);
        feedRecordButton = findViewById(R.id.feedRecordButton);
        backButton = findViewById(R.id.backButton);

        //Set up ip camera
        ipCamera.loadUrl("http://" + ipAddress + ":" + HTTPport);

        //Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}