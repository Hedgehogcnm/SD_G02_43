package com.example.project2025.ui.account_menu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class accountMenuViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public accountMenuViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is account Menu fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}