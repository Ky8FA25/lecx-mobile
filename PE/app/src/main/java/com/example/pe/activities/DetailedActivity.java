package com.example.pe.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.pe.R;
import com.example.pe.data.CartDAO;
import com.example.pe.data.OrderDAO;
import com.example.pe.data.UserDAO;
import com.example.pe.models.Order;
import com.example.pe.models.Product;

import java.text.SimpleDateFormat;

public class DetailedActivity extends AppCompatActivity {

    private ImageView detailedImg, addItem, removeItem;
    private TextView detailedName, detailedDesc, detailedPrice, quantityText;
    private RatingBar ratingBar;
    private Button addToCartBtn, buyNowBtn;
    private Toolbar toolbar;

    private int quantity = 1;
    private double totalPrice = 0;

    private Product product;



    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);


        toolbar = findViewById(R.id.detailed_toolbar);
        detailedImg = findViewById(R.id.detailed_img);
        detailedName = findViewById(R.id.detailed_name);
        detailedDesc = findViewById(R.id.detailed_desc);
        detailedPrice = findViewById(R.id.detailed_price);
        quantityText = findViewById(R.id.quantity);
        addItem = findViewById(R.id.add_item);
        removeItem = findViewById(R.id.remove_item);
        addToCartBtn = findViewById(R.id.add_to_cart);
        buyNowBtn = findViewById(R.id.buy_now);
        ratingBar = findViewById(R.id.my_rating);

        // === Toolbar setup ===
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // === Nhận dữ liệu từ intent ===
        if (getIntent() != null && getIntent().hasExtra("product")) {
            product = (Product) getIntent().getSerializableExtra("product");

            // Gán dữ liệu lên UI
            detailedName.setText(product.getName());
            detailedDesc.setText(product.getDescription());
            detailedPrice.setText(String.format("%.2f", product.getPrice()));

            Glide.with(this)
                    .load(product.getImage())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(detailedImg);

            toolbar.setTitle(product.getName());
            totalPrice = product.getPrice();
        }

        // Sự kiện tăng giảm số lượng
        addItem.setOnClickListener(v -> {
            quantity++;
            quantityText.setText(String.valueOf(quantity));
            totalPrice = product.getPrice() * quantity;
            detailedPrice.setText(String.format("%.2f", totalPrice));
        });

        removeItem.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
                totalPrice = product.getPrice() * quantity;
                detailedPrice.setText(String.format("%.2f", totalPrice));
            }
        });

        // Xử lý Add to Cart
        addToCartBtn.setOnClickListener(v -> {
            if (product == null) {
                Toast.makeText(this, "Không có sản phẩm để thêm!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy userId dựa vào email trong SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String userEmail = prefs.getString(KEY_EMAIL, null);

            if (userEmail == null) {
                Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy userId từ email
            com.example.pe.data.UserDAO userDAO = new com.example.pe.data.UserDAO(this);
            int userId = userDAO.getUserIdByEmail(userEmail);
            if (userId == -1) {
                Toast.makeText(this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thêm sản phẩm vào giỏ
            CartDAO cartDAO = new CartDAO(this);
            cartDAO.open();
            long result = cartDAO.addToCart(userId, product.getId(), quantity);
            cartDAO.close();

            if (result != -1) {
                Toast.makeText(this,
                        "Đã thêm " + quantity + " x " + product.getName() + " vào giỏ hàng!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi thêm vào giỏ!", Toast.LENGTH_SHORT).show();
            }
        });

        // Buy Now
        buyNowBtn.setOnClickListener(v -> {
            if (product == null) {
                Toast.makeText(this, "Không có sản phẩm để mua!", Toast.LENGTH_SHORT).show();
                return;
            }

            //  Lấy email user hiện tại từ SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String userEmail = prefs.getString(KEY_EMAIL, null);

            if (userEmail == null) {
                Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy userId từ email
            UserDAO userDAO = new UserDAO(this);
            int userId = userDAO.getUserIdByEmail(userEmail);
            if (userId == -1) {
                Toast.makeText(this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                return;
            }

            //  Tạo đơn hàng mới (Order)
            OrderDAO orderDAO = new OrderDAO(this);
            orderDAO.open();

            double orderTotal = product.getPrice() * quantity;
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                    .format(new java.util.Date());

            Order order = new Order(userId, orderTotal, currentDate);
            long orderId = orderDAO.addOrder(order);

            orderDAO.close();

            if (orderId != -1) {
                Toast.makeText(this,
                        "Đã mua ngay: " + product.getName() + " - $" + String.format("%.2f", orderTotal),
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi tạo đơn hàng!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
