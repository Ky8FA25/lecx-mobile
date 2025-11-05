package com.example.pe.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pe.R;
import com.example.pe.data.UserDAO;
import com.example.pe.models.User;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private UserDAO userDAO;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        userDAO = new UserDAO(this);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Tự động đăng nhập nếu đã lưu Remember
        boolean remember = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            email.setText(sharedPreferences.getString(KEY_EMAIL, ""));
            password.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
            signIn(null); // auto login
        }
    }

    public void signIn(View view) {
        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();

        if (userEmail.isEmpty() || userPass.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = userDAO.getUser(userEmail, userPass);
        if (user != null) {
            // Lưu thông tin remember login
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_EMAIL, userEmail);
            editor.putString(KEY_PASSWORD, userPass);
            editor.apply();

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

            // Chuyển sang màn hình danh sách sản phẩm
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("userRole", user.getRole());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    public void signUp(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
