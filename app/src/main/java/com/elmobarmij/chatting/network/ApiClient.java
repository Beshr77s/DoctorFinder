package com.elmobarmij.chatting.network;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
