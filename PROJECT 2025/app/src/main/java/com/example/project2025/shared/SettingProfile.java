package com.example.project2025.shared;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project2025.R;
import com.example.project2025.SignInActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettingProfile extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_profile, container, false);

        Log.d("SettingProfile", "Bottom Sheet Created");

        // ===== Cancel btn =====
        ImageView cancelTextView = view.findViewById(R.id.setting_cancel);
        cancelTextView.setOnClickListener(v -> {
            dismiss(); // close BottomSheet
        });

        // ===== My Account =====
        TextView myAccountTextView = view.findViewById(R.id.setting_myaccount);
        myAccountTextView.setOnClickListener(v -> showMyAccountDialog());

        // ===== Privacy =====
        TextView privacyTextView = view.findViewById(R.id.setting_privacy);
        privacyTextView.setOnClickListener(v -> showPrivacyDialog());

        // ===== Log out =====
        TextView logoutTextView = view.findViewById(R.id.setting_logout);
        logoutTextView.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    // My Account dialog =====
    private void showMyAccountDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_my_account, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.change_username).setOnClickListener(v -> {
            Log.d("Account Dialog: ", "Change Username clicked");
            Intent intent = new Intent(getActivity(), ChangeUsernameActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.change_profile_image).setOnClickListener(v -> {
            Log.d("Account Dialog: ", "Change Profile Image clicked");
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.change_password).setOnClickListener(v -> {
            Log.d("Account Dialog: ", "Change Password clicked");
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }


    // privacy dialog
    private void showPrivacyDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Privacy Policy")
                .setMessage("We value your privacy. Your data will be handled securely and will not be shared without your consent.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Log out dialog
    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                    dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
