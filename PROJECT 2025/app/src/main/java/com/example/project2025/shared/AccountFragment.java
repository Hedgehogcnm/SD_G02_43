package com.example.project2025.shared;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project2025.R;
import com.example.project2025.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {

    TextView username, email;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AccountViewModel notificationViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        View root = inflater.inflate(R.layout.account_fragment, container, false);

        ImageView createGearBtn = root.findViewById(R.id.gear_button);
        createGearBtn.setOnClickListener(v -> {
            SettingProfile bottomSheet = new SettingProfile();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("ROLE", MODE_PRIVATE);
        String role = sharedPreferences.getString("Role", "Users");
        // Initialize username
        DocumentReference userRef = db.collection(role).document(currentUser.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Display Text
                    username = getView().findViewById(R.id.menu_username);
                    email = getView().findViewById(R.id.menu_email);
                    if (currentUser != null) {
                        email.setText(auth.getCurrentUser().getEmail());
                        username.setText(document.getString("name"));
                    } else {
                        username.setText("Please login first");
                        Intent intent = new Intent(getActivity(), SignInActivity.class);
                        startActivity(intent);
                    }
                }
                else {
                    Log.d("accountMenuFragment: ", "No such document");
                }
            }
            else {
                Log.d("accountMenuFragment: ", "get failed with ", task.getException());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}