package com.example.lecx_mobile.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OtpApi {

    @POST("/api/auth/send-otp")
    Call<OtpApiResponse> sendOtp(@Body Map<String, String> body);

    @POST("/api/auth/verify-otp")
    Call<OtpApiResponse> verifyOtp(@Body Map<String, String> body);
}