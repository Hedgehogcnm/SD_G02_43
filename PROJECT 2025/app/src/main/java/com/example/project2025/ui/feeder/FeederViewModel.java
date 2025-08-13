package com.example.project2025.ui.feeder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FeederViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public FeederViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("IOT device name");
    }

    public LiveData<String> getText() {
        return mText;
    }
}