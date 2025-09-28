package com.werhoz.mapzebraprinter.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.werhoz.mapzebraprinter.data.AppDatabase;
import com.werhoz.mapzebraprinter.data.entity.PriceEntity;
import com.werhoz.mapzebraprinter.data.entity.ProductEntity;
import com.werhoz.mapzebraprinter.data.model.PriceResponse;
import com.werhoz.mapzebraprinter.data.model.ProductResponse;
import com.werhoz.mapzebraprinter.data.model.ResultModel;
import com.werhoz.mapzebraprinter.data.model.TestResponse;
import com.werhoz.mapzebraprinter.network.ApiService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataRepository {
    private final ApiService apiService;
    private final AppDatabase db;
    private final MutableLiveData<Integer> _counter = new MutableLiveData<>();
    public LiveData<Integer> counter = _counter;

    private final MutableLiveData<TestResponse> _test = new MutableLiveData<>();
    public LiveData<TestResponse> test = _test;

    private final MutableLiveData<TestResponse> _generate = new MutableLiveData<>();
    public LiveData<TestResponse> generate = _generate;

    public interface SyncCallback {
        void onProgress(String message);

        void onProductProgress(int percent);

        void onPriceProgress(int percent);
    }

    public DataRepository(ApiService apiService, AppDatabase db) {
        this.apiService = apiService;
        this.db = db;
    }

    public void syncGenerate() {
        apiService.generate()
                .subscribeOn(Schedulers.io())               // jalanin di background
                .observeOn(AndroidSchedulers.mainThread())  // hasil ke UI thread
                .subscribe(
                        response -> {
                            _generate.postValue(response);
                        },
                        error -> {
                            _generate.postValue(new TestResponse(false, error.getMessage()));
                        }
                );
    }

    public void syncAllTables(SyncCallback callback) {
//        int pageSize = 10000;
//        Single.zip(
//                        fetchAllProducts(apiService, pageSize, callback).subscribeOn(Schedulers.io()),
//                        fetchAllPrices(apiService, pageSize, callback).subscribeOn(Schedulers.io()),
//                        (products, prices) -> {
//                            int totalProducts = db.productDao().getAllCount();
//                            int totalPrices = db.priceDao().getAllCount();
//                            int counter = totalPrices + totalProducts;
//                            _counter.postValue(counter);
//                            return "✅ Sync success! Products: " + totalProducts + ", Prices: " + totalPrices;
//                        }
//                )
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        result -> callback.onProgress(result),
//                        error -> callback.onProgress("❌ Sync failed: " + error.getMessage())
//                );

        fetchAllProductsFromFile(apiService, callback)
                .subscribeOn(Schedulers.io())
                .map(products -> {
                    int totalProducts = db.productDao().getAllCount();
                    _counter.postValue(totalProducts);
                    return "✅ Sync success! Products: " + totalProducts;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> callback.onProgress(result),
                        error -> callback.onProgress("❌ Sync failed: " + error.getMessage())
                );
    }


    private Single<List<ProductEntity>> fetchAllProducts(ApiService apiService, int pageSize, SyncCallback callback) {
        return Single.create(emitter -> {
            try {
                callback.onProgress("Cleaning Data...");
                db.productDao().deleteAll();

                List<ProductEntity> allProducts = new ArrayList<>();
                int pageNumber = 1;
                boolean isLastPage = false;

                int totalCount = apiService.getProducts(1, 1).blockingGet().getTotalRecords();
                int currentCount = 0;

                callback.onProgress("Downloading Data...");
                while (!isLastPage) {
//                    callback.onProgress("Syncing products page " + pageNumber + "...");
                    ProductResponse response = apiService.getProducts(pageNumber, pageSize).blockingGet();

                    List<ProductEntity> products = response.getProducts();
                    if (products != null && !products.isEmpty()) {
                        db.productDao().insertAll(products);
                        allProducts.addAll(products);

                        // Progress
                        currentCount += products.size();
                        int percent = (int) (((double) currentCount / totalCount) * 100);
                        callback.onProductProgress(percent);

//                        callback.onProgress("Saved " + products.size() + " products.");
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
//                callback.onProgress("Cleaning Data...");
                db.priceDao().deleteAll();

                List<PriceEntity> allPrices = new ArrayList<>();
                int pageNumber = 1;
                boolean isLastPage = false;

                int totalCount = apiService.getPrices(1, 1).blockingGet().getTotalRecords();
                int currentCount = 0;

                while (!isLastPage) {
//                    callback.onProgress("Syncing prices page " + pageNumber + "...");
                    PriceResponse response = apiService.getPrices(pageNumber, pageSize).blockingGet();

                    List<PriceEntity> prices = response.getProducts();
                    if (prices != null && !prices.isEmpty()) {
                        db.priceDao().insertAll(prices);
                        allPrices.addAll(prices);

                        // Progress
                        currentCount += prices.size();
                        int percent = (int) (((double) currentCount / totalCount) * 100);
                        callback.onPriceProgress(percent);

//                        callback.onProgress("Saved " + prices.size() + " prices.");
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

//                PriceEntity first = db.priceDao().getFirstByItemNumber(productEntity.getItemNumber());
//                PriceEntity last = db.priceDao().getLastByItemNumber(productEntity.getItemNumber());
//                result.wasPrice = String.valueOf(first != null ? first.getSalesPrice() : 0);
//                result.currentPrice = String.valueOf(last != null ? last.getSalesPrice() : 0);

                result.wasPrice = productEntity.getWasPrice() != null ? productEntity.getWasPrice() : "0";
                result.currentPrice = productEntity.getSalesPrice() != null ? productEntity.getSalesPrice() : "0";

                itemResponseLiveData.postValue(result);
            } catch (Exception e) {
                itemResponseLiveData.postValue(null);
            }
        });
    }

    public void testConnection(String url) {
        // Create and configure the logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Can be BASIC, HEADERS, BODY

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        Call<TestResponse> call = retrofit
                .create(ApiService.class)
                .testConnection();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<TestResponse> call, Response<TestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TestResponse result = response.body();
                    _test.postValue(result);
                } else {
                    _test.postValue(new TestResponse(false, String.valueOf(response.code())));
                }
            }

            @Override
            public void onFailure(Call<TestResponse> call, Throwable t) {
                _test.postValue(new TestResponse(false, t.getMessage()));
            }
        });
    }

    private Single<Integer> fetchAllProductsFromFile(ApiService apiService, SyncCallback callback) {
        return Single.create(emitter -> {
            try {
                callback.onProgress("Downloading Data...");
                ResponseBody body = apiService.downloadProductSales().blockingGet();

                callback.onProgress("Clearing Data...");
                db.productDao().deleteAll();

                callback.onProgress("Counting Data...");
                File tempFile = File.createTempFile("products", ".json.gz");
                try (InputStream is = body.byteStream();
                     OutputStream os = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                }
                int totalRecords = 0;
                try (InputStream fis = new FileInputStream(tempFile);
                     GZIPInputStream gis = new GZIPInputStream(fis);
                     JsonReader countReader = new JsonReader(new InputStreamReader(gis))) {

                    countReader.beginArray();
                    while (countReader.hasNext()) {
                        countReader.skipValue();
                        totalRecords++;
                    }
                    countReader.endArray();
                }

                // 2nd pass: parse & insert
                int count = 0;
                try (InputStream fis = new FileInputStream(tempFile);
                     GZIPInputStream gis = new GZIPInputStream(fis);
                     JsonReader reader = new JsonReader(new InputStreamReader(gis))) {

                    Gson gson = new Gson();

                    List<ProductEntity> batch = new ArrayList<>();

                    callback.onProgress("Inserting Data...");

                    reader.beginArray();
                    while (reader.hasNext()) {
                        ProductEntity product = gson.fromJson(reader, ProductEntity.class);
                        batch.add(product);

                        count++;

                        if (batch.size() >= 1000) {
                            db.productDao().insertAll(batch);
                            batch.clear();
                        }
                        int percent = (int) (((double) count / totalRecords) * 100);
                        callback.onProductProgress(percent);
                    }
                    reader.endArray();
                    if (!batch.isEmpty()) {
                        db.productDao().insertAll(batch);
                    }
                }

                callback.onProgress("✅ Done! Total products: " + count);
                emitter.onSuccess(count);

            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

}

