package com.example.lecx_mobile.views.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lecx_mobile.MainActivity;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.utils.Prefs;
import com.example.lecx_mobile.utils.Validator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvSignUp;
    private MaterialCheckBox cbRemember;

    // Khởi tạo Repository theo yêu cầu
    private final IAccountRepository repo = new AccountRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);

        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvSignUp    = findViewById(R.id.tvSignUp);
        cbRemember  = findViewById(R.id.cbRemember);

        btnLogin.setOnClickListener(v -> handleLogin());
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void handleLogin() {
        String email = safeText(etEmail);
        String pass  = safeText(etPassword);

        // Kiểm tra đầu vào
        if (!Validator.isValidEmail(email)) { etEmail.setError("Invalid email"); etEmail.requestFocus(); return; }
        if (!Validator.isValidPassword(pass)) { etPassword.setError("Password must be at least 6 characters"); etPassword.requestFocus(); return; }

        setLoading(true);

        // GỌI REPOSITORY TRỰC TIẾP
        repo.getByEmail(email)
                .thenAccept(user -> {
                    // Cập nhật UI trên Main Thread
                    runOnUiThread(() -> {
                        setLoading(false);
                        if (user == null) {
                            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                        } else if (!pass.equals(user.password)) {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        } else {
                            // Thành công: Lưu Session và Chuyển hướng
                            boolean remember = cbRemember != null && cbRemember.isChecked();
                            Prefs.saveSession(this, user.id, user.email, remember);
                            Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                })
                .exceptionally(e -> {
                    // Xử lý lỗi trên Main Thread
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private String safeText(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Logging in..." : "Login");
    }
}