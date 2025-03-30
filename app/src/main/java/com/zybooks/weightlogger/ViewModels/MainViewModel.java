package com.zybooks.weightlogger.ViewModels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * ViewModel for Main Activity logic.
 * Extends BaseValidationViewModel for consistency with other ViewModels.
 */
public class MainViewModel extends BaseValidationViewModel {
    private final MutableLiveData<Boolean> isLoggedInLiveData = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getIsLoggedInLiveData() {
        return isLoggedInLiveData;
    }

    public void setLoggedInState(boolean isLoggedIn) {
        isLoggedInLiveData.setValue(isLoggedIn);
    }
}