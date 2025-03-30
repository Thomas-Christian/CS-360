package com.zybooks.weightlogger.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoggedInLiveData() {
        return isLoggedInLiveData;
    }

    public void setLoggedInState(boolean isLoggedIn) {
        isLoggedInLiveData.setValue(isLoggedIn);
    }
}