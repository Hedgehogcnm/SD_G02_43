package com.example.project2025.ui.account_menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project2025.databinding.FragmentMenuBinding;

public class accountMenuFragment extends Fragment {

    private FragmentMenuBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        accountMenuViewModel notificationViewModel =
                new ViewModelProvider(this).get(accountMenuViewModel.class);

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