package com.example.myprojcet.manageRequests;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("predict")
    Call<PredictionResponse> predict(@Body TextRequest request);
}
