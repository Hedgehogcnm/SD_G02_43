package com.example.project2025.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project2025.databinding.FragmentMenuBinding;
import com.example.project2025.ui.dashboard.ScheduleBottomSheet;

public class NotificationFragment extends Fragment {

    private FragmentMenuBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationViewModel notificationViewModel =
                new ViewModelProvider(this).get(NotificationViewModel.class);

        binding = FragmentMenuBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageView createGearBtn = binding.gear;
        createGearBtn.setOnClickListener(v -> {
            SettingProfile bottomSheet = new SettingProfile();
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