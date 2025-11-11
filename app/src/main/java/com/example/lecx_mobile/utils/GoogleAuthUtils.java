package com.example.lecx_mobile.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleAuthUtils {

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static GoogleSignInClient mGoogleSignInClient;

    public static void initGoogleSignIn(Context context, String webClientId) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public static Intent getSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    public static void firebaseAuthWithGoogle(String idToken, OnAuthCompleteListener listener) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) listener.onSuccess(mAuth.getCurrentUser());
                    else listener.onFailure(task.getException());
                });
    }

    public static String getIdTokenFromIntent(Intent data) throws Exception {
        return GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult()
                .getIdToken();
    }

    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
}