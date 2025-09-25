package com.werhoz.mapzebraprinter.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.werhoz.mapzebraprinter.data.AppDatabase;
import com.werhoz.mapzebraprinter.data.entity.PriceEntity;
import com.werhoz.mapzebraprinter.data.entity.ProductEntity;
import com.werhoz.mapzebraprinter.data.model.PriceResponse;
import com.werhoz.mapzebraprinter.data.model.ProductResponse;
import com.werhoz.mapzebraprinter.data.model.ResultModel;
import com.werhoz.mapzebraprinter.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DataRepository {
    private final ApiService apiService;
    private final AppDatabase db;
    private final MutableLiveData<Integer> _counter = new MutableLiveData<>();
    public LiveData<Integer> counter = _counter;

    public interface SyncCallback {
        void onProgress(String message);
    }

    public DataRepository(ApiService apiService, AppDatabase db) {
        this.apiService = apiService;
        this.db = db;
    }

    public void syncAllTables(SyncCallback callback) {
        int pageSize = 10000;
        Single.zip(
                        fetchAllProducts(apiService, pageSize, callback).subscribeOn(Schedulers.io()),
                        fetchAllPrices(apiService, pageSize, callback).subscribeOn(Schedulers.io()),
                        (products, prices) -> {
                            int totalProducts = db.productDao().getAllCount();
                            int totalPrices = db.priceDao().getAllCount();
                            int counter = totalPrices + totalProducts;
                            _counter.postValue(counter);
                            return "✅ Sync success! Products: " + totalProducts + ", Prices: " + totalPrices;
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> callback.onProgress(result),
                        error -> callback.onProgress("❌ Sync failed: " + error.getMessage())
                );
    }


    private Single<List<ProductEntity>> fetchAllProducts(ApiService apiService, int pageSize, SyncCallback callback) {
        return Single.create(emitter -> {
            try {
                callback.onProgress("Cleaning Products...");
                db.productDao().deleteAll();

                List<ProductEntity> allProducts = new ArrayList<>();
                int pageNumber = 1;
                boolean isLastPage = false;

                while (!isLastPage) {
                    callback.onProgress("Syncing products page " + pageNumber + "...");
                    ProductResponse response = apiService.getProducts(pageNumber, pageSize).blockingGet();

                    List<ProductEntity> products = response.getProducts();
                    if (products != null && !products.isEmpty()) {
                        db.productDao().insertAll(products);
                        allProducts.addAll(products);
                        callback.onProgress("Saved " + products.size() + " products.");
                    }

                    isLastPage = response.isLastPage();
                    pageNumber++;
                }

                emitter.onSuccess(allProducts);
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    private Single<List<PriceEntity>> fetchAllPrices(ApiService apiService, int pageSize, SyncCallback callback) {
        return Single.create(emitter -> {
            try {
                callback.onProgress("Cleaning Prices...");
                db.priceDao().deleteAll();

                List<PriceEntity> allPrices = new ArrayList<>();
                int pageNumber = 1;
                boolean isLastPage = false;

                while (!isLastPage) {
                    callback.onProgress("Syncing prices page " + pageNumber + "...");
                    PriceResponse response = apiService.getPrices(pageNumber, pageSize).blockingGet();

                    List<PriceEntity> prices = response.getProducts();
                    if (prices != null && !prices.isEmpty()) {
                        db.priceDao().insertAll(prices);
                        allPrices.addAll(prices);
                        callback.onProgress("Saved " + prices.size() + " prices.");
                    }

                    isLastPage = response.isLastPage();
                    pageNumber++;
                }

                emitter.onSuccess(allPrices);
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }


    public LiveData<Integer> getCounter() {
        return counter;
    }

    public void startCounter() {
        Single.create(emitter -> {
                    try {
                        int totalProducts = db.productDao().getAllCount();
                        int totalPrices = db.priceDao().getAllCount();
                        int totalCounter = totalPrices + totalProducts;

                        _counter.postValue(totalCounter);

                        emitter.onSuccess(totalCounter); // jangan lupa sukses
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> { /* opsional kalau mau handle result */ },
                        error -> {
                            Log.e("Counter", "Error: " + error.getMessage());
                        }
                );
    }


    public void getProduct(String barcode, MutableLiveData<ResultModel> itemResponseLiveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ProductEntity productEntity = db.productDao().getByAlias(barcode);

                ResultModel result = new ResultModel();
                result.itemNumber = productEntity.getItemNumber();
                result.description = productEntity.getDescription();
                result.size = productEntity.getSize();
                result.color = productEntity.getColor();
                result.productCategory = productEntity.getItemGroup();
                result.eANNumber = productEntity.getAliasNumber();
                result.name = productEntity.getName();
                result.aliasNumber = productEntity.getAliasNumber();
                result.styleNo = productEntity.getStyleNo();
                result.configurationCode = productEntity.getConfigurationCode();
                result.qrCode = productEntity.getQrCode();
                result.currency = productEntity.getCurrency();
                result.itemGroup = productEntity.getItemGroup();

                PriceEntity first = db.priceDao().getFirstByItemNumber(productEntity.getItemNumber());
                PriceEntity last = db.priceDao().getLastByItemNumber(productEntity.getItemNumber());
                result.wasPrice = String.valueOf(first != null ? first.getSalesPrice() : 0);
                result.currentPrice = String.valueOf(last != null ? last.getSalesPrice() : 0);

                itemResponseLiveData.postValue(result);
            } catch (Exception e) {
                itemResponseLiveData.postValue(null);
            }
        });
    }
}

