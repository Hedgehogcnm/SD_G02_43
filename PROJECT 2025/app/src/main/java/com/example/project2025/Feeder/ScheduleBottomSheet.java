package com.example.project2025.Feeder;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
            builder.setTitle("Set your schedule title");
            builder.setMessage("\nPlease enter a title");

            final android.widget.EditText input = new android.widget.EditText(requireContext());
            input.setSingleLine(true);
            input.setPadding(70, 1, 50, 30);
            input.setHint("\nEnter title");
            input.setSelection(input.getText().length());
            builder.setView(input);

            builder.setPositiveButton("Save", (dialog, which) -> {
                String newTitle = input.getText() != null ? input.getText().toString().trim() : "";
                if (newTitle.isEmpty()) {
                    Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTitleTextView.setText(newTitle);
                Toast.makeText(requireContext(), "Title has been set", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancel", (d, w) -> d.dismiss());
            builder.show();
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
                    day.setTextColor(Color.parseColor("#704533"));
                    day.setTypeface(null, Typeface.NORMAL);
                    day.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    day.setSelected(true);
                    day.setTextColor(Color.parseColor("#FFFFFF"));
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

// 找到你的图片和百分比 TextView
        ImageView devicePic = view.findViewById(R.id.device_pic);
        TextView foodPercentage = view.findViewById(R.id.foodPercentage);

// 定义每个 level 对应的图片和百分比
        int[] levelDrawables = {
                R.drawable.food_level_25, // level1
                R.drawable.food_level_50, // level2
                R.drawable.food_level_75, // level3
                R.drawable.food_level_100  // level4
        };
        String[] levelPercentages = {"25%", "50%", "75%", "100%"};

        for (int i = 0; i < levelViews.length; i++) {
            final int index = i;
            levelViews[i].setOnClickListener(v -> {
                // 重置所有 Level 的文字颜色
                for (TextView lv : levelViews) {
                    lv.setSelected(false);
                    lv.setTextColor(Color.parseColor("#704533")); // 棕色字（未选中）
                }

                // 设置当前选中的 Level
                levelViews[index].setSelected(true);
                levelViews[index].setTextColor(Color.parseColor("#FFFFFF")); // 白色字
                selectedFeedLevel = index + 1;

                // 更新图片与百分比
                if (devicePic != null) {
                    devicePic.setImageResource(levelDrawables[index]);
                }
                if (foodPercentage != null) {
                    foodPercentage.setText(levelPercentages[index]);
                }

                Toast.makeText(getContext(), "Selected Level " + selectedFeedLevel, Toast.LENGTH_SHORT).show();
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

            // reset foodBar height (guard if not present in layout)
            if (foodBar != null) {
                foodBar.post(() -> {
                    ViewGroup.LayoutParams params = foodBar.getLayoutParams();
                    params.height = 0;
                    foodBar.setLayoutParams(params);
                });
            }

            // reset selected level and title (if visible)
            selectedFeedLevel = 0;
            if (setTitleTextView != null) {
                setTitleTextView.setText("Set Title");
            }

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
