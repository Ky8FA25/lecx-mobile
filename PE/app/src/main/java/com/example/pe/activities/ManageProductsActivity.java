package com.example.pe.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pe.R;
import com.example.pe.adapters.ProductAdapter;
import com.example.pe.data.CategoryDAO;
import com.example.pe.data.ProductDAO;
import com.example.pe.models.Category;
import com.example.pe.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private EditText searchProduct;
    private Button btnAdd;
    private ProductAdapter adapter;

    private List<Product> productList;
    private ProductDAO productDAO;

    private String userRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_product);

        rvProducts = findViewById(R.id.rv_products);
        searchProduct = findViewById(R.id.search_product);
        btnAdd = findViewById(R.id.btn_add_product);

        userRole = getIntent().getStringExtra("userRole");
        productDAO = new ProductDAO(this);
        productDAO.open();

        loadProducts();

        btnAdd.setOnClickListener(v -> showProductDialog(null));

        // Search filter
        searchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterProducts(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        });
    }

    private void loadProducts() {
        productList = productDAO.getAllProducts();
        adapter = new ProductAdapter(this, productList, null );
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);

        // Thêm listener quản lý
        adapter.setManageListener(new ProductAdapter.OnProductManageListener() {
            @Override
            public void onEdit(Product product) {
                showProductDialog(product);
            }

            @Override
            public void onDelete(Product product) {
                productDAO.deleteProduct(product.getId());
                Toast.makeText(ManageProductsActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                loadProducts();
            }
        });
    }

    private void filterProducts(String keyword) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList) {
            if (p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    private void showProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_product, null);
        builder.setView(view);

        EditText edtName = view.findViewById(R.id.edt_product_name);
        EditText edtPrice = view.findViewById(R.id.edt_product_price);
        EditText edtDesc = view.findViewById(R.id.edt_product_desc);
        EditText edtImage = view.findViewById(R.id.edt_product_image);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);

        // ==== Lấy danh sách Category từ DB ====
        CategoryDAO categoryDAO = new CategoryDAO(this);
        categoryDAO.open();
        List<Category> categories = categoryDAO.getAllCategories();
        categoryDAO.close();


        // Chuyển danh sách Category -> List<String> (tên Category)
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);


        // Nếu update, chọn category hiện tại
        if (product != null) {
            edtName.setText(product.getName());
            edtPrice.setText(String.valueOf(product.getPrice()));
            edtDesc.setText(product.getDescription());
            edtImage.setText(product.getImage());

            // Chọn Spinner theo CategoryId
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == product.getCategoryId()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }



        builder.setPositiveButton(product == null ? "Add" : "Update", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String desc = edtDesc.getText().toString().trim();
            String image = edtImage.getText().toString().trim();
//            String catIdStr = edtCategoryId.getText().toString().trim();

            // ==== KIỂM TRA TRƯỜNG RỖNG ====
            if (name.isEmpty() || priceStr.isEmpty() || desc.isEmpty() || image.isEmpty() ) {
                Toast.makeText(ManageProductsActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
//            int catId = Integer.parseInt(catIdStr);

            // Lấy CategoryId từ Spinner
            int selectedPosition = spinnerCategory.getSelectedItemPosition();
            int catId = categories.get(selectedPosition).getId();



            if (product == null) {
                Product newProduct = new Product(name, price, desc, image, catId);
                productDAO.insertProduct(newProduct);
            } else {
                product.setName(name);
                product.setPrice(price);
                product.setDescription(desc);
                product.setImage(image);
                product.setCategoryId(catId);
                productDAO.updateProduct(product);
            }

            loadProducts();
        });


        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        productDAO.close();
    }
}

