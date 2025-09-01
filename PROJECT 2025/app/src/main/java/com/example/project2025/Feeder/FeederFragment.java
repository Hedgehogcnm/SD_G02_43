package com.example.project2025.Feeder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.widget.ImageView;

import com.example.project2025.R;


public class FeederFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FeederViewModel dashboardViewModel =
                new ViewModelProvider(this).get(FeederViewModel.class);

        View root = inflater.inflate(R.layout.feeder_fragment, container, false);

        ImageView createAlarmBtn = root.findViewById(R.id.alarm);
        createAlarmBtn.setOnClickListener(v -> {
            ScheduleBottomSheet bottomSheet = new ScheduleBottomSheet();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}