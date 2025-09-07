package com.example.project2025.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project2025.Models.FeedHistory;
import com.example.project2025.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeedHistoryAdapter extends RecyclerView.Adapter<FeedHistoryAdapter.FeedHistoryViewHolder> {

    private List<FeedHistory> feedHistoryList;
    private SimpleDateFormat dateFormat;

    public FeedHistoryAdapter() {
        this.feedHistoryList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMMM d, yyyy - h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public FeedHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_history_item, parent, false);
        return new FeedHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedHistoryViewHolder holder, int position) {
        FeedHistory feedHistory = feedHistoryList.get(position);
        holder.feedTypeText.setText(feedHistory.getFeedType() + " Feed");
        holder.feedDateText.setText(dateFormat.format(feedHistory.getDate()));
    }

    @Override
    public int getItemCount() {
        return feedHistoryList.size();
    }

    public void setFeedHistoryList(List<FeedHistory> feedHistoryList) {
        this.feedHistoryList = feedHistoryList;
        notifyDataSetChanged();
    }

    public static class FeedHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView feedTypeText;
        TextView feedDateText;

        public FeedHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            feedTypeText = itemView.findViewById(R.id.feed_type_text);
            feedDateText = itemView.findViewById(R.id.feed_date_text);
        }
    }
}