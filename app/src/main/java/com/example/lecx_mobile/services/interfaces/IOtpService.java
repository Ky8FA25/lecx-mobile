package com.example.lecx_mobile.services.interfaces;

import com.example.lecx_mobile.api.OtpApiResponse;

import java.util.concurrent.CompletableFuture;

public interface IOtpService {
    CompletableFuture<OtpApiResponse> sendOtpAsync(String email);

    CompletableFuture<OtpApiResponse> verifyOtpAsync(String email, String otpCode);
}
