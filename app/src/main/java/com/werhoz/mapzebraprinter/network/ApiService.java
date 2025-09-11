package com.werhoz.mapzebraprinter.network;

import com.werhoz.mapzebraprinter.data.entity.ProductEntity;
import com.werhoz.mapzebraprinter.data.model.ItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("{code}")
    Call<ItemResponse> getItemByCode(@Path("code") String code);

//    @GET("Product")
    @GET("Product")
    Call<List<ProductEntity>> getProduct();
}

