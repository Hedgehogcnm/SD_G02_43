package com.example.project2025.ui.feeder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project2025.databinding.FragmentFeederBinding;

import android.widget.ImageView;


public class FeederFragment extends Fragment {

    private FragmentFeederBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FeederViewModel dashboardViewModel =
                new ViewModelProvider(this).get(FeederViewModel.class);

        binding = FragmentFeederBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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