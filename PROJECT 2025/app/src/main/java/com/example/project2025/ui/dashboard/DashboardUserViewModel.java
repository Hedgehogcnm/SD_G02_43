package com.example.project2025.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardUserViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public DashboardUserViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}