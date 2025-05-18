package com.werhoz.mapzebraprinter.network;

import com.werhoz.mapzebraprinter.data.model.ItemResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/api/Items/{code}")
    Call<ItemResponse> getItemByCode(@Path("code") String code);
}

