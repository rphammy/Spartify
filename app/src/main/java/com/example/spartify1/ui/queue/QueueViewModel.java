package com.example.spartify1.ui.queue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QueueViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<String> mEditText;

    public QueueViewModel() {
        mText = new MutableLiveData<>();
        mEditText = new MutableLiveData<>();
        mText.setValue("Welcome to Spartify!");
    }

    public LiveData<String> getText() {
        return mEditText;
    }
    public void setText(String text) { mEditText.setValue(text);}
}