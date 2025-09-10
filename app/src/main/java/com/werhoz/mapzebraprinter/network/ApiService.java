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
    @GET("fcb6fb7f-2534-410e-bb5f-d370def499e8")
    Call<List<ProductEntity>> getProduct();
}

