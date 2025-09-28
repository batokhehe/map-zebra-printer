package com.werhoz.mapzebraprinter.network;

import com.werhoz.mapzebraprinter.data.entity.ProductEntity;
import com.werhoz.mapzebraprinter.data.model.BaseResponse;
import com.werhoz.mapzebraprinter.data.model.ItemResponse;
import com.werhoz.mapzebraprinter.data.model.PriceResponse;
import com.werhoz.mapzebraprinter.data.model.ProductResponse;
import com.werhoz.mapzebraprinter.data.model.TestResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApiService {
    @GET("{code}")
    Call<ItemResponse> getItemByCode(@Path("code") String code);

    //    @GET("Product")
    @GET("Product")
    Call<BaseResponse<ProductEntity>> getProduct(@Query("pageNumber") int pageNumber,
                                                 @Query("pageSize") int pageSize);

    @GET("api/products")
    Single<ProductResponse> getProducts(
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("api/prices")
    Single<PriceResponse> getPrices(
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("api/test")
    Call<TestResponse> testConnection();

    @GET("ProductSales.json.gz")
    @Streaming
    Single<ResponseBody> downloadProductSales();
}

