package com.example.spartify1.ui.queue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QueueViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public QueueViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is queue fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}