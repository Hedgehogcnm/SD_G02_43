package com.example.project2025.ui.account_menu;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        Toast.makeText(getContext(), "Bottom Sheet Opened", Toast.LENGTH_SHORT).show();

        // ===== Cancel btn =====
        TextView cancelTextView = view.findViewById(R.id.setting_cancel);
        cancelTextView.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            dismiss(); // close BottomSheet
        });

        // ===== My Account =====
        TextView myAccountTextView = view.findViewById(R.id.setting_myaccount);
        myAccountTextView.setOnClickListener(v -> showMyAccountDialog());

        // ===== Language =====
        TextView languageTextView = view.findViewById(R.id.setting_language);
        languageTextView.setOnClickListener(v -> showLanguageDialog());

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
            Toast.makeText(getContext(), "Change Username clicked", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.change_profile_image).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change Profile Image clicked", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.change_password).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change Password clicked", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // language dialog
    private void showLanguageDialog() {
        String[] languages = {"English", "Bahasa Melayu"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Language")
                .setItems(languages, (dialog, which) -> {
                    String selectedLanguage = languages[which];
                    Toast.makeText(getContext(),
                            "Selected: " + selectedLanguage,
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
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
