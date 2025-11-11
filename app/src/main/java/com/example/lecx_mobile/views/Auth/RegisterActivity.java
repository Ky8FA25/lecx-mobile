package com.example.lecx_mobile.views.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lecx_mobile.R;
import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.repositories.implementations.AccountRepository;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView tvLogin;

    // Khởi tạo Repository theo yêu cầu
    private final IAccountRepository repo = new AccountRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
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
        if (TextUtils.isEmpty(fullName)) { etFullName.setError("Full name is required"); etFullName.requestFocus(); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email is required"); etEmail.requestFocus(); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Please enter a valid email"); etEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password is required"); etPassword.requestFocus(); return; }
        if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters"); etPassword.requestFocus(); return; }
        if (TextUtils.isEmpty(confirmPassword)) { etConfirmPassword.setError("Please confirm your password"); etConfirmPassword.requestFocus(); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords do not match"); etConfirmPassword.requestFocus(); return; }

        performSignUp(fullName, email, password);
    }

    private void performSignUp(String fullName, String email, String password) {
        btnSignUp.setEnabled(false);
        btnSignUp.setText("Creating account...");

        // BƯỚC 1: Kiểm tra Email đã tồn tại chưa
        repo.existsByEmailAsync(email)
                .thenAccept(exists -> {
                    if (exists) {
                        // Cập nhật UI trên Main Thread
                        runOnUiThread(() -> {
                            btnSignUp.setEnabled(true);
                            btnSignUp.setText("Sign Up");
                            Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
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
                                    btnSignUp.setText("Sign Up");
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

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
                                    btnSignUp.setText("Sign Up");
                                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                                return null;
                            });
                })
                .exceptionally(e -> {
                    // Xử lý lỗi kiểm tra tồn tại email
                    runOnUiThread(() -> {
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("Sign Up");
                        Toast.makeText(this, "Error checking email existence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }
}