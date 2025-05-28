package com.werhoz.mapzebraprinter.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.werhoz.mapzebraprinter.data.AppDatabase;
import com.werhoz.mapzebraprinter.data.model.ItemResponse;
import com.werhoz.mapzebraprinter.data.model.ResultModel;
import com.werhoz.mapzebraprinter.data.repository.DataRepository;
import com.werhoz.mapzebraprinter.network.ApiClient;
import com.werhoz.mapzebraprinter.network.ApiService;

public class DataViewModel extends AndroidViewModel {
    private final DataRepository repository;
    private MutableLiveData<ResultModel> itemResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>();

    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }

    public DataViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "map_zebra").build();
        ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);

        repository = new DataRepository(api, db);
    }

    public void startSync() {
        repository.syncAllTables(syncStatus::postValue);
    }

    public LiveData<ResultModel> getItemResponseLiveData() {
        return itemResponseLiveData;
    }


    public void getProduct(String barcode) {
        repository.getProduct(barcode, itemResponseLiveData);
    }
}

