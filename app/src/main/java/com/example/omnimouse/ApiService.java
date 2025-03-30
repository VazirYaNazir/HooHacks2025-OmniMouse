package com.example.omnimouse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/send_data")
    Call<ResponseData> sendMovementData(@Body MovementData data);

    @POST("/send_data")
    Call<ResponseData> sendButtonPress(@Body int data);
}
