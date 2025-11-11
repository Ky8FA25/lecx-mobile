package com.example.lecx_mobile.views.Auth.Components;

import android.app.Activity;
import android.content.Intent;

import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.services.implementations.GoogleAuthService;
import com.example.lecx_mobile.services.interfaces.IGoogleAuthService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;

public class GoogleLoginButton {

    private final MaterialButton button;
    private final IGoogleAuthService googleService;
    private OnLoginListener listener;
    private final Activity activity;
    private final int requestCode;

    public interface OnLoginListener {
        void onSuccess(Account account);
        void onFailure(Exception e);
    }

    public GoogleLoginButton(Activity activity, MaterialButton button,
                             IGoogleAuthService googleService, int requestCode) {
        this.activity = activity;
        this.button = button;
        this.googleService = googleService;
        this.requestCode = requestCode;

        this.button.setOnClickListener(v -> googleService.startSignIn(activity, requestCode));
    }

    public void setOnLoginListener(OnLoginListener listener) {
        this.listener = listener;
    }

    // gọi từ onActivityResult trong Activity
    public void handleActivityResult(Intent data) {
        googleService.handleSignInResult(data, new GoogleAuthService.OnAuthCompleteListener() {
            @Override
            public void onSuccess(Account account) {
                if (listener != null) listener.onSuccess(account);
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) listener.onFailure(e);
            }
        });
    }
}
