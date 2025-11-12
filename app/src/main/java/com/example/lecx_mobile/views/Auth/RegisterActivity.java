package com.example.lecx_mobile.views.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lecx_mobile.MainActivity;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.services.implementations.GoogleAuthService;
import com.example.lecx_mobile.utils.Constants;
import com.example.lecx_mobile.utils.Prefs;
import com.example.lecx_mobile.views.Auth.Components.GoogleLoginButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView tvLogin;
    private  MaterialButton btnGoogleLogin;
    private GoogleLoginButton googleLogin;
    private static final int GOOGLE_SIGN_IN_CODE = 1001;
    private View loadingOverlay;


    // Khởi tạo Repository theo yêu cầu
    private final IAccountRepository repo = new AccountRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_register);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        initViews();
        setupListeners();

        GoogleAuthService googleService = new GoogleAuthService(this);

        googleLogin = new GoogleLoginButton(this, btnGoogleLogin, googleService, GOOGLE_SIGN_IN_CODE);
        googleLogin.setOnLoginListener(new GoogleLoginButton.OnLoginListener() {
            @Override
            public void onSuccess(Account account) {
                setLoading(true);
                // 1. Lưu session
                Prefs.saveSession(RegisterActivity.this, account.id, account.email, true, account.isEmailConfirmed);

                // 2. Thông báo
                Toast.makeText(RegisterActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // 3. Chuyển sang MainActivity
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // 4. Kết thúc LoginActivity
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> handleSignUp());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleSignUp() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate đầu vào
        if (TextUtils.isEmpty(fullName)) { etFullName.setError("Vui lòng nhập tên"); etFullName.requestFocus(); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Vui lòng nhập email"); etEmail.requestFocus(); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Vui lòng nhập email hợp lệ"); etEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Vui lòng nhập mật khẩu"); etPassword.requestFocus(); return; }
        if (password.length() < 6) { etPassword.setError("Mật khẩu ít nhất 6 kí tự"); etPassword.requestFocus(); return; }
        if (TextUtils.isEmpty(confirmPassword)) { etConfirmPassword.setError("Vui lòng xác nhập mật khẩu"); etConfirmPassword.requestFocus(); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Mật khẩu không khớp"); etConfirmPassword.requestFocus(); return; }

        performSignUp(fullName, email, password);
    }

    private void performSignUp(String fullName, String email, String password) {
        setLoading(true);
        btnSignUp.setEnabled(false);
        btnSignUp.setText("Đang tạo...");

        // BƯỚC 1: Kiểm tra Email đã tồn tại chưa
        repo.existsByEmail(email)
                .thenAccept(exists -> {
                    if (exists) {
                        setLoading(false);
                        runOnUiThread(() -> {
                            btnSignUp.setEnabled(true);
                            btnSignUp.setText("Đăng kí");
                            Toast.makeText(this, "Email đã có người sử dụng", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    // BƯỚC 2: Email chưa tồn tại, tạo User và lưu
                    Account newUser = new Account();
                    // BỎ: newUser.id = newId; (vì repo.addAsync đã xử lý)
                    newUser.fullname = fullName;
                    newUser.email = email;
                    newUser.password = password;
                    newUser.avatar = Constants.DEFAULT_AVATAR_URL;

                    repo.add(newUser)
                            .thenAccept(user -> {
                                // Cập nhật UI trên Main Thread (Đăng ký thành công)
                                runOnUiThread(() -> {
                                    btnSignUp.setEnabled(true);
                                    btnSignUp.setText("Đăng kí");
                                    Toast.makeText(this, "Đã tạo tài khoản!", Toast.LENGTH_SHORT).show();

                                    // Chuyển sang trang Login
                                    Intent intent = new Intent(this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                            })
                            .exceptionally(e -> {
                                // Xử lý lỗi khi thêm User
                                runOnUiThread(() -> {
                                    btnSignUp.setEnabled(true);
                                    btnSignUp.setText("Đăng kí");
                                    Toast.makeText(this, "Đăng kí thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                                return null;
                            });
                })
                .exceptionally(e -> {
                    // Xử lý lỗi kiểm tra tồn tại email
                    runOnUiThread(() -> {
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("Đăng kí");
                        Toast.makeText(this, "Lỗi xác thực email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setLoading(true);
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            googleLogin.handleActivityResult(data);
        }
    }

    private void setLoading(boolean loading) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        }

        // Vô hiệu hóa / kích hoạt tất cả input và button
        etFullName.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        btnSignUp.setEnabled(!loading);
        if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(!loading);

        // Thay đổi text nút Sign Up
        btnSignUp.setText(loading ? "Đang tạo..." : "Đăng kí");
    }
}