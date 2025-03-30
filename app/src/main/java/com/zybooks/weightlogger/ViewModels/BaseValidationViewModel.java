package com.zybooks.weightlogger.ViewModels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.zybooks.weightlogger.Services.ValidationService;

/**
 * Base ViewModel class that provides validation functionality for form inputs.
 * Eliminates code duplication across different ViewModels.
 */
public abstract class BaseValidationViewModel extends AndroidViewModel {

    protected final ValidationService validationService;
    protected final MutableLiveData<String> statusMessageLiveData = new MutableLiveData<>();

    /**
     * Creates a new BaseValidationViewModel.
     *
     * @param application The application context
     */
    public BaseValidationViewModel(@NonNull Application application) {
        super(application);
        validationService = new ValidationService();
    }

    /**
     * Updates a form validity LiveData based on individual field validities.
     *
     * @param formValidLiveData The LiveData to update with the overall form validity
     * @param validationStates The individual validation states to check
     */
    protected void updateFormValidity(MutableLiveData<Boolean> formValidLiveData, Boolean... validationStates) {
        formValidLiveData.setValue(validationService.isFormValid(validationStates));
    }
}