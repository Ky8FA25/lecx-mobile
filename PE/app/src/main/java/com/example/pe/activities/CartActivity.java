package com.example.pe.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pe.R;
import com.example.pe.adapters.CartAdapter;
import com.example.pe.data.CartDAO;
import com.example.pe.data.OrderDAO;
import com.example.pe.data.ProductDAO;
import com.example.pe.data.UserDAO;
import com.example.pe.models.Cart;
import com.example.pe.models.Order;
import com.example.pe.models.Product;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {

    private RecyclerView recyclerView;
    private TextView totalPriceText;
    private Button btnCheckout;

    private CartDAO cartDAO;
    private UserDAO userDAO;

    private ProductDAO productDAO;
    private List<Cart> cartList;
    private List<Product> productList;
    private CartAdapter adapter;

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";

    private int userId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_recycler);
        totalPriceText = findViewById(R.id.cart_total);
        btnCheckout = findViewById(R.id.btn_checkout);



        cartDAO = new CartDAO(this);
        productDAO = new ProductDAO(this);
        userDAO = new UserDAO(this);


        //  Lấy email user hiện tại từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userEmail = prefs.getString(KEY_EMAIL, null);

        if (userEmail == null) {
            totalPriceText.setText("Vui lòng đăng nhập lại!");
            return;
        }

        // Lấy userId từ email
        userId = userDAO.getUserIdByEmail(userEmail);
        if (userId == -1) {
            totalPriceText.setText("Không tìm thấy người dùng!");
            return;
        }

        //  Lấy dữ liệu giỏ hàng và sản phẩm
        cartDAO.open();
        productDAO.open();

        cartList = cartDAO.getCartByUserId(userId);
        productList = productDAO.getAllProducts();

        System.out.println("🧾 Cart count: " + cartList.size());
        System.out.println("📦 Product count: " + productList.size());

        for (Cart c : cartList) {
            System.out.println("🛒 cart=" + c.getId() + " | userId=" + c.getUserId() + " | prodId=" + c.getProductId() + " | qty=" + c.getQuantity());
        }

        //  Hiển thị danh sách giỏ hàng (RecyclerView)
        adapter = new CartAdapter(this, cartList, productList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateTotal();

        cartDAO.close();
        productDAO.close();

        // 🔹 5. Bắt sự kiện checkout
        btnCheckout.setOnClickListener(v -> checkout());
    }


    private void updateTotal() {
        double total = 0;
        for (Cart c : cartList) {
            for (Product p : productList) {
                if (p.getId() == c.getProductId()) {
                    total += p.getPrice() * c.getQuantity();
                }
            }
        }
        totalPriceText.setText(String.format("Total: $%.2f", total));
    }

    @Override
    public void onQuantityChanged(Cart cart, int newQuantity) {
        cartDAO.open();
        cartDAO.updateQuantity(cart.getId(), newQuantity);
        cartDAO.close();
        updateTotal();
    }

    @Override
    public void onItemRemoved(Cart cart) {
        cartDAO.open();
        cartDAO.removeFromCart(cart.getId());
        cartDAO.close();
        cartList.remove(cart);
        adapter.notifyDataSetChanged();
        updateTotal();
    }


    private void checkout() {
        double total = 0;
        for (Cart c : cartList) {
            for (Product p : productList) {
                if (p.getId() == c.getProductId()) {
                    total += p.getPrice() * c.getQuantity();
                }
            }
        }

        if (total == 0) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔸 Ghi đơn hàng vào bảng orders
        OrderDAO orderDAO = new OrderDAO(this);
        orderDAO.open();

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new Date());
        Order order = new Order(userId, total, currentDate);

        long orderId = orderDAO.addOrder(order);
        orderDAO.close();

        if (orderId != -1) {
            // 🔸 Xóa giỏ hàng sau khi checkout
            cartDAO.open();
            cartDAO.clearCartByUserId(userId);
            cartDAO.close();

            cartList.clear();
            adapter.notifyDataSetChanged();
            updateTotal();

            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Có lỗi khi lưu đơn hàng!", Toast.LENGTH_SHORT).show();
        }
    }


}
