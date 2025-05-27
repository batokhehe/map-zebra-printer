package com.werhoz.mapzebraprinter.network;

import com.werhoz.mapzebraprinter.data.entity.UserEntity;
import com.werhoz.mapzebraprinter.data.model.ItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/api/Items/{code}")
    Call<ItemResponse> getItemByCode(@Path("code") String code);

    @GET("c/ffbc-6672-4ec2-b16a")
    Call<List<UserEntity>> getUsers();
}

