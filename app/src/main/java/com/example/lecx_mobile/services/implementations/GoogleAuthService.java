package com.example.lecx_mobile.services.implementations;

import android.app.Activity;
import android.content.Intent;

import com.example.lecx_mobile.R;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.services.interfaces.IGoogleAuthService;
import com.example.lecx_mobile.utils.GoogleAuthUtils;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;

public class GoogleAuthService implements IGoogleAuthService {

    private final IAccountRepository repo;

    public GoogleAuthService(Activity activity) {
        this.repo = new AccountRepository();

        // Tự đọc client ID từ strings.xml
        String clientId = activity.getString(R.string.default_web_client_id);
        GoogleAuthUtils.initGoogleSignIn(activity, clientId);
    }
    @Override
    public void startSignIn(Activity activity, int requestCode) {
        Intent intent = GoogleAuthUtils.getSignInIntentForceAccountSelection();
        if (intent != null) {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void handleSignInResult(Intent data, OnAuthCompleteListener listener) {
        try {
            String idToken = GoogleAuthUtils.getIdTokenFromIntent(data);

            GoogleAuthUtils.firebaseAuthWithGoogle(idToken, new GoogleAuthUtils.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser firebaseUser) {
                    if (firebaseUser.getEmail() == null) {
                        listener.onFailure(new Exception("Email null"));
                        return;
                    }

                    // 1. Kiểm tra tài khoản đã có trong DB chưa
                    repo.getByEmail(firebaseUser.getEmail())
                            .thenCompose(existingAccount -> {
                                if (existingAccount != null) {
                                    // Nếu có rồi, trả về luôn
                                    return CompletableFuture.completedFuture(existingAccount);
                                } else {
                                    // Nếu chưa có, tạo account mới
                                    Account newAccount = new Account(
                                            firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "",
                                            firebaseUser.getEmail(),
                                            "", // password rỗng vì Google login
                                            firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "",
                                            firebaseUser.getEmail(),
                                            true
                                    );
                                    return repo.add(newAccount);
                                }
                            })
                            .thenAccept(account -> listener.onSuccess(account))
                            .exceptionally(e -> {
                                listener.onFailure(new Exception(e));
                                return null;
                            });
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });

        } catch (Exception e) {
            listener.onFailure(e);
        }
    }
}
