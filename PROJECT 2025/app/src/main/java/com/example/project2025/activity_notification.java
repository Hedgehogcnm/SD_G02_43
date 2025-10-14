package com.example.project2025;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project2025.Adapters.NotificationAdapter;
import com.example.project2025.Models.NotificationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
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

        RecyclerView recycler = findViewById(R.id.notificationsRecycler);
        NotificationAdapter adapter = new NotificationAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // base query: user’s history; for Admin, show selected user in ADMINISTRATION uid
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = resolveTargetUserId();
        Query baseQuery = db.collection("NotificationHistory")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        loadAndDisplay(baseQuery, adapter, null);

        // show PopupMenu
        filterIcon.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity_notification.this, filterIcon);
            popupMenu.getMenu().add("All");
            popupMenu.getMenu().add("Scheduled Feeding");
            popupMenu.getMenu().add("Low Food Alert");

            // menu onclick
            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();
                Toast.makeText(activity_notification.this, "Filter: " + selected, Toast.LENGTH_SHORT).show();
                Query q = baseQuery;
                if ("Low Food Alert".equals(selected)) {
                    q = db.collection("NotificationHistory")
                            .whereEqualTo("userId", resolveTargetUserId())
                            .whereEqualTo("type", "LOW_FOOD")
                            .orderBy("timestamp", Query.Direction.DESCENDING);
                } else if ("Scheduled Feeding".equals(selected)) {
                    q = db.collection("NotificationHistory")
                            .whereEqualTo("userId", resolveTargetUserId())
                            .whereEqualTo("type", "FEEDING")
                            .whereEqualTo("source", "SCHEDULED")
                            .orderBy("timestamp", Query.Direction.DESCENDING);
                }
                loadAndDisplay(q, adapter, selected);
                return true;
            });

            popupMenu.show();
        });
    }

    private void loadAndDisplay(Query query, NotificationAdapter adapter, String filter) {
        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                android.util.Log.e("NotificationHistory", "Query failed: " + task.getException());
                return;
            }
            List<NotificationItem> list = new ArrayList<>();
            android.util.Log.d("NotificationHistory", "Filter: " + filter + ", Found " + task.getResult().size() + " documents");
            for (QueryDocumentSnapshot d : task.getResult()) {
                android.util.Log.d("NotificationHistory", "Doc: type=" + d.getString("type") + ", source=" + d.getString("source"));
                // On user side, exclude MANUAL feeds in All view
                if (filter == null || "All".equals(filter)) {
                    String role = d.getString("role");
                    String source = d.getString("source");
                    if ("Users".equals(role) && "FEEDING".equals(d.getString("type")) && "MANUAL".equals(source)) {
                        continue;
                    }
                }
                NotificationItem it = new NotificationItem();
                it.id = d.getId();
                it.type = d.getString("type");
                it.source = d.getString("source");
                it.role = d.getString("role");
                it.title = d.getString("title");
                it.scheduleId = d.getString("scheduleId");
                Long lvl = d.getLong("level");
                it.level = lvl != null ? lvl.intValue() : 0;
                it.timestamp = d.getTimestamp("timestamp");
                list.add(it);
            }
            android.util.Log.d("NotificationHistory", "Final list size: " + list.size());
            adapter.setItems(list);
        });
    }

    private String resolveTargetUserId() {
        try {
            String role = getSharedPreferences("ROLE", 0).getString("Role", null);
            if ("Admin".equals(role)) {
                return getSharedPreferences("ADMINISTRATION", 0).getString("uid", null);
            }
        } catch (Throwable ignored) {}
        try {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            return u != null ? u.getUid() : null;
        } catch (Throwable ignored) {}
        return null;
    }
}
