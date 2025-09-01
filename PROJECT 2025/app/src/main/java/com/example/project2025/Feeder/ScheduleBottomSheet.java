package com.example.project2025.Feeder;

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

public class ScheduleBottomSheet extends BottomSheetDialogFragment {

    private TextView[] dayViews;
    private TextView[] levelViews;
    private View foodBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule_bottom, container, false);

        Toast.makeText(getContext(), "Bottom Sheet Opened", Toast.LENGTH_SHORT).show();

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

            Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            dismiss(); // close BottomSheet
        });

        return view;
    }
}
