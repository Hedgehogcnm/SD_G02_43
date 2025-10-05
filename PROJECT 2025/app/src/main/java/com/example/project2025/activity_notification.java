package com.example.project2025;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class activity_notification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // find filter icon
        ImageView filterIcon = findViewById(R.id.filterIcon);

        // show PopupMenu
        filterIcon.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity_notification.this, filterIcon);
            popupMenu.getMenu().add("All");
            popupMenu.getMenu().add("Schedule Notification");
            popupMenu.getMenu().add("Low Food Alert");

            // menu onclick
            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();
                Toast.makeText(activity_notification.this, "Filter: " + selected, Toast.LENGTH_SHORT).show();

                return true;
            });

            popupMenu.show();
        });
    }
}
