package com.example.project2025.Feeder;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project2025.Models.ScheduleData;
import com.example.project2025.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ScheduleBottomSheet extends BottomSheetDialogFragment {

    private TextView[] dayViews;
    private TextView[] levelViews;
    private View foodBar;
    private TimePicker timePicker;
    private TextView setTitleTextView;
    private int selectedFeedLevel = 0;

    public interface ScheduleDataListener {
        void onScheduleDataReceived(ScheduleData scheduleData);
    }

    private ScheduleDataListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule_bottom, container, false);

        Toast.makeText(getContext(), "Bottom Sheet Opened", Toast.LENGTH_SHORT).show();

        timePicker = view.findViewById(R.id.timePicker);
        setTitleTextView = view.findViewById(R.id.setTitle);
        try { timePicker.setIs24HourView(false); } catch (Throwable ignored) {}

        // Title click -> prompt input dialog with validation
        setTitleTextView.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setTitle("Set Schedule Title");
            final android.widget.EditText input = new android.widget.EditText(getContext());
            input.setSingleLine(true);
            input.setText(setTitleTextView.getText());
            input.setSelection(input.getText().length());
            builder.setView(input);
            builder.setPositiveButton("Save", null);
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            final android.app.AlertDialog dialog = builder.create();
            dialog.setOnShowListener(di -> {
                android.widget.Button positive = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                positive.setOnClickListener(v1 -> {
                    String t = input.getText() != null ? input.getText().toString().trim() : "";
                    if (t.isEmpty()) {
                        Toast.makeText(getContext(), "Please add your title", Toast.LENGTH_SHORT).show();
                        // Do not dismiss
                        return;
                    }
                    setTitleTextView.setText(t);
                    Toast.makeText(getContext(), "Title has been set", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            });
            dialog.show();
        });

        // ==== initialize dayViews ====
        dayViews = new TextView[]{
                view.findViewById(R.id.daySun),
                view.findViewById(R.id.dayMon),
                view.findViewById(R.id.dayTue),
                view.findViewById(R.id.dayWed),
                view.findViewById(R.id.dayThu),
                view.findViewById(R.id.dayFri),
                view.findViewById(R.id.daySat),
        };

        for (TextView day : dayViews) {
            day.setOnClickListener(v -> {
                boolean isSelected = day.isSelected();

                if (isSelected) {
                    day.setSelected(false);
                    day.setTextColor(Color.parseColor("#888888"));
                    day.setTypeface(null, Typeface.NORMAL);
                    day.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    day.setSelected(true);
                    day.setTextColor(Color.parseColor("#FF4081"));
                    day.setTypeface(null, Typeface.BOLD);
                    day.setBackgroundResource(R.drawable.selected_day_background);
                }
            });
        }

        // ==== initialize levelViews and foodBar ====
        levelViews = new TextView[]{
                view.findViewById(R.id.level1),
                view.findViewById(R.id.level2),
                view.findViewById(R.id.level3),
                view.findViewById(R.id.level4)
        };


        foodBar = view.findViewById(R.id.foodLevelBar);
        int[] heightPercents = {25, 50, 75, 100};

        for (int i = 0; i < levelViews.length; i++) {
            final int index = i;
            levelViews[i].setOnClickListener(v -> {
                for (TextView lv : levelViews) lv.setSelected(false);
                levelViews[index].setSelected(true);
                selectedFeedLevel = index + 1;

                foodBar.post(() -> {
                    int fullHeight = ((View) foodBar.getParent()).getHeight();
                    ViewGroup.LayoutParams params = foodBar.getLayoutParams();
                    params.height = (fullHeight * heightPercents[index]) / 100;
                    foodBar.setLayoutParams(params);
                });
            });
        }

        // ==== set cancel button ====
        TextView cancelTextView = view.findViewById(R.id.cancel);
        cancelTextView.setOnClickListener(v -> {
            // clear dayView status
            for (TextView day : dayViews) {
                day.setSelected(false);
                day.setTextColor(Color.parseColor("#888888"));
                day.setTypeface(null, Typeface.NORMAL);
                day.setBackgroundColor(Color.TRANSPARENT);
            }

            // clear levelView status
            for (TextView level : levelViews) {
                level.setSelected(false);
            }

            // reset foodBar height
            foodBar.post(() -> {
                ViewGroup.LayoutParams params = foodBar.getLayoutParams();
                params.height = 0;
                foodBar.setLayoutParams(params);
            });

            Toast.makeText(getContext(), "Schedule Cancelled", Toast.LENGTH_SHORT).show();
            dismiss(); // close BottomSheet
        });

        // Save button
        TextView saveTextView = view.findViewById(R.id.save);
        saveTextView.setOnClickListener(v -> saveScheduleData());

        return view;
    }

    public void setScheduleDataListener(ScheduleDataListener listener) {
        this.listener = listener;
    }

    private void saveScheduleData() {
        // Selected days
        java.util.List<String> selectedDays = new java.util.ArrayList<>();
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < dayViews.length; i++) {
            if (dayViews[i].isSelected()) selectedDays.add(dayNames[i]);
        }

        // Time formatted HH:mm (24-hour format for logic)
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        String timeString = out.format(cal.getTime());

        // Validate required fields: at least one day selected and a feed level selected
        boolean levelChosen = false;
        if (levelViews != null) {
            for (TextView lv : levelViews) {
                if (lv.isSelected()) { levelChosen = true; break; }
            }
        }
        if (selectedDays.isEmpty() || !levelChosen || timeString == null || timeString.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Title must be non-empty
        String title = setTitleTextView != null ? setTitleTextView.getText().toString() : "";
        if (title == null || title.trim().isEmpty() || "Set Title".contentEquals(title)) {
            Toast.makeText(getContext(), "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        ScheduleData scheduleData = new ScheduleData(title, timeString, selectedDays, selectedFeedLevel);
        if (listener != null) listener.onScheduleDataReceived(scheduleData);
        Toast.makeText(getContext(), "Schedule Saved", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
