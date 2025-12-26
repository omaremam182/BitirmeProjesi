package com.example.myprojcet.manageRequests;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    // 🔥 IMPORTANT
                    // Emulator → 10.0.2.2
                    // Real phone → YOUR PC IP
                    .baseUrl("http://10.87.9.98:8000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
