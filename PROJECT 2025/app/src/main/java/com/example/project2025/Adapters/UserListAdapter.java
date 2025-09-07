package com.example.project2025.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project2025.Models.UserList;
import com.example.project2025.R;
import com.google.firebase.firestore.auth.User;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private List<UserList> userList;

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserListAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserList userList = this.userList.get(position);
        holder.username.setText(userList.getUsername());
        holder.userEmail.setText(userList.getEmail());
        holder.userUID.setText(userList.getUID());
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
    }
}
