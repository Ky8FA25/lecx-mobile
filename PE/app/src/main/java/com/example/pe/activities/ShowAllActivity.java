package com.example.pe.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pe.R;
import com.example.pe.adapters.ProductAdapter;
import com.example.pe.data.CategoryDAO;
import com.example.pe.data.ProductDAO;
import com.example.pe.models.Product;

import java.util.List;

public class ShowAllActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ProductDAO productDAO;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all);

        // Ánh xạ view
        recyclerView = findViewById(R.id.show_all_rec);
        toolbar = findViewById(R.id.show_all_toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Lấy categoryId từ Intent
        int categoryId = getIntent().getIntExtra("category_id", -1);

        // Khởi tạo DAO
        productDAO = new ProductDAO(this);
        productDAO.open();

        CategoryDAO categoryDAO = new CategoryDAO(this);
        categoryDAO.open();


        List<Product> productList;

        if (categoryId != -1) {
            // Lấy sản phẩm theo CategoryId
            productList = productDAO.getProductsByCategory(categoryId);
            String categoryName = categoryDAO.getCategoryNameById(categoryId);
            toolbar.setTitle(categoryName);

            if (productList.isEmpty()) {
                Toast.makeText(this, "No products found in this category", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nếu không truyền ID, lấy tất cả sản phẩm
            productList = productDAO.getAllProducts();
            toolbar.setTitle("All Products");
        }

        productDAO.close();
        categoryDAO.close();

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, productList, product -> {
            // TODO: mở chi tiết sản phẩm nếu muốn
        });
        recyclerView.setAdapter(productAdapter);

        // Nút Back trên Toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
