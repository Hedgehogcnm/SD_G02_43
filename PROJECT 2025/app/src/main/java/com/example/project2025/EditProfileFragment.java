package com.example.project2025;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.project2025.ui.account_menu.ChangeUsernameActivity;
import com.example.project2025.ui.account_menu.ChangePasswordActivity;

/**
 * EditProfileFragment - Admin profile management interface
 * This fragment provides the same functionality as the user profile settings
 * Allows admin to manage their own profile (username, password, language, etc.)
 * Copied from user-side SettingProfile for consistency
 */
public class EditProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        Log.d("EditProfileFragment", "Fragment Created");

        // ===== CANCEL BUTTON =====
        // When admin clicks cancel, return to the Dashboard fragment
        TextView cancelTextView = view.findViewById(R.id.setting_cancel);
        cancelTextView.setOnClickListener(v -> {
            // Navigate back to dashboard instead of closing the activity
            if (getActivity() instanceof AdminActivity) {
                ((AdminActivity) getActivity()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, new DashboardFragment())
                    .commit();
            }
        });

        // ===== MY ACCOUNT =====
        // Opens dialog to change username, profile image, or password
        TextView myAccountTextView = view.findViewById(R.id.setting_myaccount);
        myAccountTextView.setOnClickListener(v -> showMyAccountDialog());

        // ===== LANGUAGE =====
        // Opens dialog to choose between English and Bahasa Melayu
        TextView languageTextView = view.findViewById(R.id.setting_language);
        languageTextView.setOnClickListener(v -> showLanguageDialog());

        // ===== PRIVACY =====
        // Shows privacy policy information
        TextView privacyTextView = view.findViewById(R.id.setting_privacy);
        privacyTextView.setOnClickListener(v -> showPrivacyDialog());

        // ===== LOG OUT =====
        // Logs out admin and returns to SignInActivity
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
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
