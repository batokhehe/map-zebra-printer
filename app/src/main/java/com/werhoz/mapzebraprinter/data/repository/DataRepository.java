package com.werhoz.mapzebraprinter.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.werhoz.mapzebraprinter.data.AppDatabase;
import com.werhoz.mapzebraprinter.data.entity.AliasNumberEntity;
import com.werhoz.mapzebraprinter.data.entity.BOEODTrnEntity;
import com.werhoz.mapzebraprinter.data.entity.ProductRSFEntity;
import com.werhoz.mapzebraprinter.data.entity.SalesPriceListEntity;
import com.werhoz.mapzebraprinter.data.entity.SystemTableEntity;
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
                db.productRSFDao().deleteAll();
                db.aliasNumberDao().deleteAll();
                db.bOEODTrnDao().deleteAll();
                db.salesPriceListDao().deleteAll();
                db.systemTableDao().deleteAll();
                callback.onProgress("Cleaning Success.");


                callback.onProgress("Syncing product...");
                List<ProductRSFEntity> products = apiService.getProductRSF().execute().body();
                db.productRSFDao().insertAll(products);
                List<ProductRSFEntity> list = db.productRSFDao().getAll();
                callback.onProgress("Product saved.");

                callback.onProgress("Syncing alias...");
                List<AliasNumberEntity> aliases = apiService.getAliasNumber().execute().body();
                db.aliasNumberDao().insertAll(aliases);
                callback.onProgress("Alias saved.");

                callback.onProgress("Syncing BOEOD...");
                List<BOEODTrnEntity> bod = apiService.getBOEODTrn().execute().body();
                db.bOEODTrnDao().insertAll(bod);
                callback.onProgress("BOEOD saved.");

                callback.onProgress("Syncing Sales Price...");
                List<SalesPriceListEntity> sales = apiService.getSalesPriceList().execute().body();
                db.salesPriceListDao().insertAll(sales);
                callback.onProgress("Sales saved.");

                callback.onProgress("Syncing System...");
                List<SystemTableEntity> system = apiService.getSystemTable().execute().body();
                db.systemTableDao().insertAll(system);
                callback.onProgress("System saved.");

                callback.onProgress("✅ All tables synced successfully!");
            } catch (Exception e) {
                callback.onProgress("❌ Sync failed: " + e.getMessage());
            }
        });
    }

    public void getProduct(String barcode, MutableLiveData<ResultModel> itemResponseLiveData) {
        ResultModel product = db.productDao().getFirstProduct(barcode).getValue();

        if (product == null) {
            itemResponseLiveData.setValue(null);
            return;
        }

        List<SalesPriceListEntity> prices = db.salesPriceListDao().getById(product.itemNumber);

        Double wasPrice = null;
        Double currentPrice = null;

        for (SalesPriceListEntity p : prices) {
            if ("0".equals(p.salesCampaign)) {
                wasPrice = Double.valueOf(p.salesPrice);
            }
        }

        // Sort to get the highest SalesCampaign numeric value
        prices.sort((p1, p2) -> {
            try {
                return Integer.parseInt(p2.salesCampaign) - Integer.parseInt(p1.salesCampaign);
            } catch (Exception e) {
                return -1;
            }
        });

        if (!prices.isEmpty()) {
            currentPrice = Double.valueOf(prices.get(0).salesPrice);
        }
        product.wasPrice = String.valueOf(wasPrice);
        product.currentPrice = String.valueOf(currentPrice);

        itemResponseLiveData.setValue(product);
    }
}

