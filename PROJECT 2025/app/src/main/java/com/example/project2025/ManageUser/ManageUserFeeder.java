package com.example.project2025.ManageUser;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project2025.Feeder.ScheduleBottomSheet;
import com.example.project2025.Feeder.ScheduleHelper;
import com.example.project2025.Models.ScheduleData;
import com.example.project2025.R;
import com.example.project2025.databinding.ManageUserFeederBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUserFeeder extends AppCompatActivity {
    private LinearLayout scheduleContainer;
    private List<ScheduleData> scheduleList;
    private List<String> scheduleDocIds;
    private int nextScheduleIndex = 1; // Track which schedule slot to use next
    private ListenerRegistration schedulesListener;
    private FirebaseFirestore db;
    private String PI_IP = "127.0.0.1";
    private ImageView img;
    private TextView percentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ManageUserFeederBinding binding = ManageUserFeederBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //for food level purpose (eq)
        img = findViewById(R.id.device_pic);
        percentage = findViewById(R.id.foodPercentage);

        // Initialize schedule list and container
        scheduleList = new ArrayList<>();
        scheduleDocIds = new ArrayList<>();
        scheduleContainer = findViewById(R.id.scheduleContainer);
        db = FirebaseFirestore.getInstance();

        // Initialize PI_IP
        PI_IP = getSharedPreferences("FEEDERIP", MODE_PRIVATE).getString("feeder_ip", PI_IP);

        ImageView createAlarmBtn = findViewById(R.id.alarm);
        createAlarmBtn.setOnClickListener(v -> {
            // Enforce max 4 schedules before opening sheet
            if (scheduleList != null && scheduleList.size() >= 4) {
                Toast.makeText(getApplicationContext(), "You can only create 4 schedules", Toast.LENGTH_SHORT).show();
                return;
            }
            ScheduleBottomSheet bottomSheet = new ScheduleBottomSheet();
            bottomSheet.setScheduleDataListener((ScheduleBottomSheet.ScheduleDataListener) this); // Set the listener
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });


        // Real-time schedules listener
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Get current PI_IP
        PI_IP = getSharedPreferences("FEEDERIP", MODE_PRIVATE).getString("feeder_ip", PI_IP);
        showFoodLevel();
    }

    void showFoodLevel(){
        db.collection("Feeder").whereEqualTo("ip_address", PI_IP).get().addOnCompleteListener(task ->
        {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    double foodLevel = document.getDouble("food_level");
                    checkFoodLevel(foodLevel);
                    displayPercentage(foodLevel);
                    Log.d("Feeder Fragment:", "Food Level: " + foodLevel + "%");
                }
            }
            else {
                Log.d("Feeder Fragment:", "Error getting documents: ", task.getException());
            }
        });
    }
    void checkFoodLevel(double foodLevel){
        if(foodLevel <= 3){
            img.setImageResource(R.drawable.food_level_100);
        }
        else if(foodLevel <= 6){
            img.setImageResource(R.drawable.food_level_75);
        }
        else if(foodLevel <= 9){
            img.setImageResource(R.drawable.food_level_50);
        }
        else if(foodLevel <= 12){
            img.setImageResource(R.drawable.food_level_25);
        }
        else if(foodLevel <= 15){
            img.setImageResource(R.drawable.food_level_0);
        }
    }
    void displayPercentage(double foodLevel){
        int result = (int) (125 - (foodLevel / 12 * 100));
        if(foodLevel > 15){
            percentage.setText("0%");
        }
        else if(foodLevel < 3){
            percentage.setText("100%");
        }
        else if(foodLevel >= 3 && foodLevel <= 15){
            percentage.setText(result + "%");
        }
    }
}