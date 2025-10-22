package com.example.project2025.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project2025.Models.NotificationItem;
import com.example.project2025.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private final List<NotificationItem> items = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public void setItems(List<NotificationItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationItem it = items.get(position);
        if ("LOW_FOOD".equals(it.type)) {
            h.icon.setImageResource(R.drawable.ic_warning);
            h.title.setText("Low Food Alert");
            h.subtitle.setText("Please add more food for your kitties!");
        } else {
            h.icon.setImageResource(R.drawable.ic_feed);
            String t = it.title != null ? it.title : "Feeding Completed";
            h.title.setText(t);
            h.subtitle.setText("Level " + it.level + (it.source != null ? " • " + it.source : ""));
        }
        if (it.timestamp != null) h.time.setText(fmt.format(it.timestamp.toDate()));
        else h.time.setText("");
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon; TextView title; TextView subtitle; TextView time;
        VH(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
            time = v.findViewById(R.id.time);
        }
    }
}





