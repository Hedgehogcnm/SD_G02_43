package com.example.project2025.ui.notifications;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project2025.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettingProfile extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_profile, container, false);

        Toast.makeText(getContext(), "Bottom Sheet Opened", Toast.LENGTH_SHORT).show();

        // ==== set cancel button ====
        TextView cancelTextView = view.findViewById(R.id.setting_cancel);
        cancelTextView.setOnClickListener(v -> {

            Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            dismiss(); // close BottomSheet
        });
        return view;
        }
    }
