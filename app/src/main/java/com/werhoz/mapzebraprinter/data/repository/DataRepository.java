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
                result.itemNumber = entity.ItemNumber;
                result.description = entity.Description;
                result.size = entity.Size;
                result.color = entity.Color;
                result.productCategory = entity.Description;
                result.eANNumber = entity.AliasNumber;
                result.wasPrice = entity.SalesPrice;
                result.currentPrice = entity.SalesPrice;

                result.name = entity.Name;
                result.aliasNumber = entity.AliasNumber;
                result.styleNo = entity.StyleNo;
                result.configurationCode = entity.ConfigurationCode;
                result.qrCode = entity.QRCode;
                result.currency = entity.Currency;
                result.itemGroup = entity.ItemGroup;

                itemResponseLiveData.postValue(result);
            } catch (Exception e) {
                itemResponseLiveData.postValue(null);
            }
        });
    }
}

