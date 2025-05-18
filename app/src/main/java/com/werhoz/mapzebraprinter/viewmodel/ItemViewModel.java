package com.werhoz.mapzebraprinter.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.werhoz.mapzebraprinter.data.model.ItemResponse;
import com.werhoz.mapzebraprinter.data.repository.ItemRepository;

public class ItemViewModel extends ViewModel {
    private MutableLiveData<ItemResponse> itemResponseLiveData;
    private ItemRepository repository;

    public ItemViewModel() {
        itemResponseLiveData = new MutableLiveData<>();
        repository = new ItemRepository();
    }

    public LiveData<ItemResponse> getItemResponseLiveData() {
        return itemResponseLiveData;
    }

    public void fetchItem(String code) {
        repository.getItem(code, itemResponseLiveData);
    }
}

