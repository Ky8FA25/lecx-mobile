package com.example.lecx_mobile.views.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lecx_mobile.MainActivity;
import com.example.lecx_mobile.utils.Prefs;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean remembered = Prefs.isRemember(this);
        int userId = Prefs.getUserId(this);

        Class<?> next;
        if (remembered && userId != -1) {
            next =  MainActivity.class;
        } else {
            next = LoginActivity.class;
        }

        startActivity(new Intent(this, next));
        finish();
    }
}

