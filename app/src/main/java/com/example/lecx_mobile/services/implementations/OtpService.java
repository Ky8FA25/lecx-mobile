package com.example.lecx_mobile.services.implementations;

import com.example.lecx_mobile.api.OtpApi;
import com.example.lecx_mobile.api.OtpApiResponse;
import com.example.lecx_mobile.services.interfaces.IOtpService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class OtpService implements IOtpService {

    private final OtpApi otpApi;

    public OtpService(Retrofit retrofit) {
        otpApi = retrofit.create(OtpApi.class);
    }

    // Convert Retrofit Call to CompletableFuture
    private CompletableFuture<OtpApiResponse> callToFuture(Call<OtpApiResponse> call) {
        CompletableFuture<OtpApiResponse> future = new CompletableFuture<>();
        call.enqueue(new Callback<OtpApiResponse>() {
            @Override
            public void onResponse(Call<OtpApiResponse> call, Response<OtpApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    future.complete(response.body());
                } else {
                    future.completeExceptionally(new RuntimeException("API Error: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<OtpApiResponse> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    // ================== CompletableFuture API ==================

    public CompletableFuture<OtpApiResponse> sendOtpAsync(String email) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        return callToFuture(otpApi.sendOtp(body));
    }

    public CompletableFuture<OtpApiResponse> verifyOtpAsync(String email, String otpCode) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otpCode", otpCode);
        return callToFuture(otpApi.verifyOtp(body));
    }

}
