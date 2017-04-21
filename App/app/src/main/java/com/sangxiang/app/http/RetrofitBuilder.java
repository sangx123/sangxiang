package com.sangxiang.app.http;

import com.sangxiang.app.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sangxiang on 11/4/17.
 */

public class RetrofitBuilder {
    private static String API_ROOU_URL="https://api-stage.mykaishi.com/";

    public static <T> T createJavaApi(Class<T> clazz) {
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(API_ROOU_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(getClient())
                .build();
        return builder.create(clazz);
    }

    private static OkHttpClient getClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG)
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        else
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();
    }

}
