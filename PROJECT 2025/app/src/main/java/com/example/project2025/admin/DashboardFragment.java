package com.example.project2025.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

import com.example.project2025.R;

/**
 * DashboardFragment - Admin dashboard interface
 * Currently empty as requested by user
 * This fragment will be used for admin-specific features in the future
 */
public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Currently shows "Admin Dashboard" and "(Empty for now)" text
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
}
