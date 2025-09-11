package com.werhoz.mapzebraprinter.network;

import android.content.Context;

import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.orhanobut.hawk.Hawk;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
//    private static final String BASE_URL = "http://192.168.0.101:7255/api/Items/";
    private static final String BASE_URL = Hawk.get("ip_address", "http://192.168.0.102:7255") + "/api/Items/";
//    private static final String BASE_URL = "https://mocki.io/v1/";
    private static Retrofit retrofit;

    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext(); // simpan context secara aman
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Create and configure the logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Can be BASIC, HEADERS, BODY

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(new ChuckerInterceptor(appContext))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // attach OkHttpClient
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

