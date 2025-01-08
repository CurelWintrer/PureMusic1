package com.wintercruel.puremusic1.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ViewModel 类
public class MyViewModel extends ViewModel {
    private static MutableLiveData<String> data = new MutableLiveData<>();

    public LiveData<String> getData() {
        return data;
    }

    public static void updateData(String newData) {
        data.setValue(newData);
    }
}
