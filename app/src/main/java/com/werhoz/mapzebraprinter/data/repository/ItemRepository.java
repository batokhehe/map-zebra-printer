package com.werhoz.mapzebraprinter.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.werhoz.mapzebraprinter.data.model.ItemResponse;
import com.werhoz.mapzebraprinter.network.ApiClient;
import com.werhoz.mapzebraprinter.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemRepository {
    private ApiService apiService;

    public ItemRepository() {
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    public void getItem(String code, MutableLiveData<ItemResponse> itemLiveData) {
        apiService.getItemByCode(code).enqueue(new Callback<ItemResponse>() {
            @Override
            public void onResponse(Call<ItemResponse> call, Response<ItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemLiveData.setValue(response.body());
                } else {
                    itemLiveData.setValue(null); // or handle error
                }
            }

            @Override
            public void onFailure(Call<ItemResponse> call, Throwable t) {
                itemLiveData.setValue(null);
            }
        });
    }
}
