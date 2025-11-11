package com.example.lecx_mobile.services.interfaces;

import android.app.Activity;
import android.content.Intent;

import com.example.lecx_mobile.models.Account;
import com.google.firebase.auth.FirebaseUser;

public interface IGoogleAuthService {

    void startSignIn(Activity activity, int requestCode);

    void handleSignInResult(Intent data, OnAuthCompleteListener listener);


    public interface OnAuthCompleteListener {
        void onSuccess(Account account); // 🔄 thay FirebaseUser -> Account
        void onFailure(Exception e);
    }
}