package com.werhoz.mapzebraprinter.network;

import com.werhoz.mapzebraprinter.data.entity.AliasNumberEntity;
import com.werhoz.mapzebraprinter.data.entity.BOEODTrnEntity;
import com.werhoz.mapzebraprinter.data.entity.ProductRSFEntity;
import com.werhoz.mapzebraprinter.data.entity.SalesPriceListEntity;
import com.werhoz.mapzebraprinter.data.entity.SystemTableEntity;
import com.werhoz.mapzebraprinter.data.model.ItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("{code}")
    Call<ItemResponse> getItemByCode(@Path("code") String code);

    @GET("ProductRSF")
    Call<List<ProductRSFEntity>> getProductRSF();

    @GET("AliasNumber")
    Call<List<AliasNumberEntity>> getAliasNumber();

    @GET("BOEODTrn")
    Call<List<BOEODTrnEntity>> getBOEODTrn();

    @GET("SalesPriceList")
    Call<List<SalesPriceListEntity>> getSalesPriceList();

    @GET("SystemTable")
    Call<List<SystemTableEntity>> getSystemTable();

}

