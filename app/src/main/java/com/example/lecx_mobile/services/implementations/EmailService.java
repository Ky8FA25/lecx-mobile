package com.example.lecx_mobile.services.implementations;

import android.app.Activity;
import android.widget.Toast;

import com.example.lecx_mobile.services.interfaces.IEmailService;
import com.example.lecx_mobile.utils.Prefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailService implements IEmailService {

    private final Activity activity;

    public EmailService(Activity activity) {
        this.activity = activity;
    }

    /**
     * Gửi email xác thực cho user hiện tại
     */
    public void sendEmailVerification() {
        String email = Prefs.getEmail(activity); // lấy email người dùng lưu

        if (email == null || email.isEmpty()) {
            Toast.makeText(activity, "Chưa có email người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: gọi API server hoặc Firebase Functions để gửi mail xác thực
        // Ví dụ: call Firebase Functions sendOtp(email)
        Toast.makeText(activity, "Đã gửi email xác thực tới: " + email, Toast.LENGTH_SHORT).show();
    }

    /**
     * Kiểm tra email đã xác thực chưa
     */
    public boolean isEmailVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && user.isEmailVerified();
    }
}
