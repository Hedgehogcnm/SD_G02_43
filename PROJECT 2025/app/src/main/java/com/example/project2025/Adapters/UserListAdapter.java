package com.example.project2025.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project2025.Models.UserList;
import com.example.project2025.R;
import com.google.firebase.firestore.auth.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(UserList user);
    }
    private List<UserList> userList;
    private final OnItemClickListener listener;

    public UserListAdapter(List<UserList> userList, OnItemClickListener listener) {
        if (userList != null) {
            this.userList = userList;
        } else {
            this.userList = new ArrayList<>();
        }
        this.listener = listener;
    }

    public void setUserList(List<UserList> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserListAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(userList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView userEmail;
        TextView userUID;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            userEmail = itemView.findViewById(R.id.user_email);
            userUID = itemView.findViewById(R.id.user_uid);
        }
        public void bind(final UserList user, final OnItemClickListener listener) {
            username.setText(user.getUsername());
            userEmail.setText(user.getEmail());
            userUID.setText(user.getUID());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(user);
                }
            });
        }
    }
}
