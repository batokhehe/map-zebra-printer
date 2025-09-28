package com.werhoz.mapzebraprinter.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.werhoz.mapzebraprinter.data.AppDatabase;
import com.werhoz.mapzebraprinter.data.model.ResultModel;
import com.werhoz.mapzebraprinter.data.model.TestResponse;
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

    private final MutableLiveData<Integer> _productProgress = new MutableLiveData<>();
    private final MutableLiveData<Integer> _priceProgress = new MutableLiveData<>();

    public LiveData<Integer> getProductProgress() {
        return _productProgress;
    }

    public LiveData<Integer> getPriceProgress() {
        return _priceProgress;
    }

    public DataViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "map_zebra").build();
        ApiService api = ApiClient.getRetrofitInstance().create(ApiService.class);

        repository = new DataRepository(api, db);
    }

    public void startGenerate() {
        repository.syncGenerate();
    }

    public LiveData<TestResponse> generate() {
        return repository.generate;
    }

    public void startSync() {
        repository.syncAllTables(new DataRepository.SyncCallback() {
            @Override
            public void onProgress(String message) {
                syncStatus.postValue(message);
            }

            @Override
            public void onProductProgress(int percent) {
                _productProgress.postValue(percent);
            }

            @Override
            public void onPriceProgress(int percent) {
                _priceProgress.postValue(percent);
            }
        });
    }

    public void startCounter() {
        repository.startCounter();
    }

    public LiveData<Integer> getCounter() {
        return repository.getCounter();
    }

    public void startTest(String url) {
        repository.testConnection(url);
    }

    public LiveData<TestResponse> test() {
        return repository.test;
    }

    public LiveData<ResultModel> getItemResponseLiveData() {
        return itemResponseLiveData;
    }


    public void getProduct(String barcode) {
        repository.getProduct(barcode, itemResponseLiveData);
    }
}

