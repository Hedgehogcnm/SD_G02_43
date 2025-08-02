package com.example.project2025.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project2025.databinding.FragmentDashboardBinding;

import android.widget.ImageView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.deviceName;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        ImageView createAlarmBtn = binding.alarm;
        createAlarmBtn.setOnClickListener(v -> {
            ScheduleBottomSheet bottomSheet = new ScheduleBottomSheet();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}