package com.example.lecx_mobile.views.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lecx_mobile.MainActivity;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.services.implementations.GoogleAuthService;
import com.example.lecx_mobile.utils.Prefs;
import com.example.lecx_mobile.utils.Validator;
import com.example.lecx_mobile.views.Auth.Components.GoogleLoginButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvSignUp;
    private MaterialCheckBox cbRemember;
    private GoogleLoginButton googleLogin;
    private static final int GOOGLE_SIGN_IN_CODE = 1001;

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

        MaterialButton btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        GoogleAuthService googleService = new GoogleAuthService(this);

        googleLogin = new GoogleLoginButton(this, btnGoogleLogin, googleService, GOOGLE_SIGN_IN_CODE);
        googleLogin.setOnLoginListener(new GoogleLoginButton.OnLoginListener() {
            @Override
            public void onSuccess(Account account) {
                // 1. Lưu session
                boolean remember = cbRemember != null && cbRemember.isChecked();
                Prefs.saveSession(LoginActivity.this, account.id, account.email, remember);

                // 2. Thông báo
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // 3. Chuyển sang MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // 4. Kết thúc LoginActivity
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogin() {
        String email = safeText(etEmail);
        String pass  = safeText(etPassword);

        // Kiểm tra đầu vào
        if (!Validator.isValidEmail(email)) { etEmail.setError("Email không hợp lệ"); etEmail.requestFocus(); return; }
        if (!Validator.isValidPassword(pass)) { etPassword.setError("Mật khẩu ít nhất 6 kí tự"); etPassword.requestFocus(); return; }

        setLoading(true);

        // GỌI REPOSITORY TRỰC TIẾP
        repo.getByEmail(email)
                .thenAccept(user -> {
                    // Cập nhật UI trên Main Thread
                    runOnUiThread(() -> {
                        setLoading(false);
                        if (user == null) {
                            Toast.makeText(this, "Không tìm thấy email", Toast.LENGTH_SHORT).show();
                        } else if (!pass.equals(user.password)) {
                            Toast.makeText(this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        } else {
                            // Thành công: Lưu Session và Chuyển hướng
                            boolean remember = cbRemember != null && cbRemember.isChecked();
                            Prefs.saveSession(this, user.id, user.email, remember);
                            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private String safeText(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            googleLogin.handleActivityResult(data);
        }
    }
}