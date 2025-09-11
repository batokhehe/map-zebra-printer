package com.werhoz.mapzebraprinter.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.werhoz.mapzebraprinter.data.AppDatabase;
import com.werhoz.mapzebraprinter.data.entity.ProductEntity;
import com.werhoz.mapzebraprinter.data.model.ResultModel;
import com.werhoz.mapzebraprinter.network.ApiService;

import java.util.List;
import java.util.concurrent.Executors;

public class DataRepository {
    private final ApiService apiService;
    private final AppDatabase db;

    public interface SyncCallback {
        void onProgress(String message);
    }

    public DataRepository(ApiService apiService, AppDatabase db) {
        this.apiService = apiService;
        this.db = db;
    }

    public void syncAllTables(SyncCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                callback.onProgress("Cleaning Data...");
                db.productDao().deleteAll();
                callback.onProgress("Cleaning Success.");

                callback.onProgress("Syncing product...");
                List<ProductEntity> products = apiService.getProduct().execute().body();
                db.productDao().insertAll(products);
                List<ProductEntity> listProduct = db.productDao().getAll();
                callback.onProgress("Product saved.");
                callback.onProgress("✅ All tables synced successfully! : " + listProduct.size() + " data.");
            } catch (Exception e) {
                callback.onProgress("❌ Sync failed: " + e.getMessage());
            }
        });
    }

    public void getProduct(String barcode, MutableLiveData<ResultModel> itemResponseLiveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ProductEntity entity = db.productDao().getByAlias(barcode);

                ResultModel result = new ResultModel();
                result.itemNumber = entity.itemNumber;
                result.description = entity.description;
                result.size = entity.size;
                result.color = entity.color;
                result.productCategory = entity.itemGroup;
                result.eANNumber = entity.aliasNumber;
                result.wasPrice = entity.wasPrice;
                result.currentPrice = entity.salesPrice;

                result.name = entity.name;
                result.aliasNumber = entity.aliasNumber;
                result.styleNo = entity.styleNo;
                result.configurationCode = entity.configurationCode;
                result.qrCode = entity.qrCode;
                result.currency = entity.currency;
                result.itemGroup = entity.itemGroup;

                itemResponseLiveData.postValue(result);
            } catch (Exception e) {
                itemResponseLiveData.postValue(null);
            }
        });
    }
}

