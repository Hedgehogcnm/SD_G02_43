package com.example.project2025.ManageUser;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project2025.Adapters.UserListAdapter;
import com.example.project2025.Models.UserList;
import com.example.project2025.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment {

    private RecyclerView userListRecyclerView;
    private UserListAdapter userListAdapter;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Currently shows "Admin Dashboard" and "(Empty for now)" text
        View root = inflater.inflate(R.layout.user_list, container, false);

        db = FirebaseFirestore.getInstance();
        userListRecyclerView = root.findViewById(R.id.user_list_recycler_view);
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        userListAdapter = new UserListAdapter(new ArrayList<>(), new UserListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserList user) {
                sharedPreferences = getActivity().getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("uid", user.getUID());
                editor.putString("name", user.getUsername());
                editor.putString("email", user.getEmail());
                editor.apply();

                Intent intent = new Intent(getActivity(), ManageUserAdministration.class);
                startActivity(intent);

                Log.d("UserList", "Clicked user: " + user.getUsername());
            }
        });
        userListRecyclerView.setAdapter(userListAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("UserList", "Started");
        loadUser();
    }

    public void loadUser() {
        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<UserList> userList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("UserList", "Document ID: " + document.getId() + ", Data: " + document.getData());
                    UserList user = new UserList(document.getString("name"), document.getString("email"), document.getString("uid"));
                    userList.add(user);
                }

                if(userList.isEmpty()){
                    Log.d("UserList", "No users found");
                }
                else{
                    Log.d("UserList", "Found " + userList.size() + " users");
                    userListAdapter.setUserList(userList);
                }
            }else{
                Log.e("UserList", "Error getting user list  ", task.getException());
                // Check if it's a missing index error
                if (task.getException() != null &&
                        task.getException().getMessage() != null &&
                        task.getException().getMessage().contains("index")) {
                    Log.e("UserList", "Missing Firestore composite index! Check Firebase Console for index creation link.");
                }
            }

        });
    }
}