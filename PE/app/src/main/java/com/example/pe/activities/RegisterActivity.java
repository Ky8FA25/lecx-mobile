package com.example.pe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pe.R;
import com.example.pe.data.UserDAO;
import com.example.pe.models.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText name, email, password;
    private UserDAO userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // ánh xạ view theo id trong XML
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        userDao = new UserDAO(this);
    }

    // được gọi khi bấm nút Sign Up (theo thuộc tính android:onClick="signup")
    public void signup(View view) {
        String fullName = name.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();

        // kiểm tra nhập trống
        if (fullName.isEmpty() || userEmail.isEmpty() || userPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // kiểm tra định dạng email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        // có thể thêm yêu cầu độ dài mật khẩu tối thiểu nếu cần
        if (userPass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // auto set role = Customer
        User user = new User(fullName, userEmail, userPass, "Customer");

        boolean success = userDao.insertUser(user);
        if (success) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            // chuyển sang màn hình login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Email already exists!", Toast.LENGTH_SHORT).show();
        }
    }

    // được gọi khi bấm TextView "Sign In"
    public void signin(android.view.View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
