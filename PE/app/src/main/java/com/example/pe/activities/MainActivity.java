package com.example.pe.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.example.pe.R;
import com.example.pe.data.DatabaseHelper;
import com.example.pe.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {
    Fragment homeFragment;
    private String userRole;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- Ánh xạ Toolbar ---
        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        // Nút menu bên trái (icon hamburger)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        }
        // --- Load HomeFragment vào màn chính ---
        homeFragment = new HomeFragment();
        loadFragment(homeFragment);
        userRole = getIntent().getStringExtra("userRole");



        // ===== Khởi tạo Database =====
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Kiểm tra database có tạo thành công không
            if (db != null) {
                Toast.makeText(this, "Database created/opened successfully!", Toast.LENGTH_SHORT).show();
                System.out.println("✅ Database Path: " + db.getPath());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Database creation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Ví dụ: xem tất cả sản phẩm

        Cursor cursor = db.rawQuery("SELECT * FROM products", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                System.out.println("📦 ID=" + id + " | " + name + " | $" + price + " | " + desc);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();


    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_container, fragment);
        transaction.commit();
    }

    // --- Tạo menu ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // 🔹 Ẩn/hiện nút Revenue và crud product theo role admin
        if ("admin".equalsIgnoreCase(userRole)) {
            menu.findItem(R.id.menu_revenue).setVisible(true);
            menu.findItem(R.id.menu_manage_products).setVisible(true);
        } else {
            menu.findItem(R.id.menu_revenue).setVisible(false);
            menu.findItem(R.id.menu_manage_products).setVisible(false);
        }

        return true;
    }

    // --- Xử lý sự kiện menu ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            // Xóa remember
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("remember", false); // hoặc editor.clear() nếu muốn xóa hết
            editor.apply();
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            finish();
        } else if (id == R.id.menu_my_cart) {
            startActivity(new Intent(MainActivity.this, CartActivity.class));
        }
        else if (id == R.id.menu_revenue) {
            startActivity(new Intent(MainActivity.this, RevenueActivity.class));
        }
        else if (id == R.id.menu_manage_products) {
            Intent intent = new Intent(MainActivity.this, ManageProductsActivity.class);
            intent.putExtra("userRole", userRole); // truyền role hiện tại
            startActivity(intent);        }
        return true;
    }
}
