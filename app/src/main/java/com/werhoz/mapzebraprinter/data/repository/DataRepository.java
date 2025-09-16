package com.werhoz.mapzebraprinter.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.orhanobut.hawk.Hawk;
import com.werhoz.mapzebraprinter.data.AppDatabase;
import com.werhoz.mapzebraprinter.data.entity.ProductEntity;
import com.werhoz.mapzebraprinter.data.model.BaseResponse;
import com.werhoz.mapzebraprinter.data.model.ResultModel;
import com.werhoz.mapzebraprinter.network.ApiService;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Response;

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

                int pageNumber = 1;
                int pageSize = 10000;
                boolean isLastPage = false;

                while (!isLastPage) {
                    callback.onProgress("Syncing product page " + pageNumber + "...");

                    Response<BaseResponse<ProductEntity>> response =
                            apiService.getProduct(pageNumber, pageSize).execute();

                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse<ProductEntity> body = response.body();

                        List<ProductEntity> products = body.getData();
                        if (products != null && !products.isEmpty()) {
                            db.productDao().insertAll(products);
                            callback.onProgress("Saved " + products.size() + " products.");
                        }

                        isLastPage = body.isLastPage();
                        if (pageNumber == 97)
                            Log.d("tes", "syncAllTables: "); // pakai flag dari server
                        pageNumber++;
                    } else {
                        throw new Exception("API error: " + response.message());
                    }
                }

                LiveData<Integer> total = db.productDao().getAllCount();
                callback.onProgress("✅ All tables synced successfully! Total : " + total + " data.");
            } catch (Exception e) {
                callback.onProgress("❌ Sync failed: " + e.getMessage());
            }
        });
    }

    public LiveData<Integer> getCounter(){
        return db.productDao().getAllCount();
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
                result.currentPrice = entity.nowPrice;

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

