package com.example.pe.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.pe.models.Cart;
import com.example.pe.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public CartDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // === Thêm sản phẩm vào giỏ ===
    public long addToCart(int userId, int productId, int quantity) {
        // Nếu sản phẩm đã có, thì tăng số lượng
        Cursor cursor = database.query(DatabaseHelper.TABLE_CART,
                null,
                DatabaseHelper.COLUMN_CART_USER_ID + "=? AND " +
                        DatabaseHelper.COLUMN_CART_PRODUCT_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_QUANTITY));
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CART_QUANTITY, currentQty + quantity);
            long result = database.update(DatabaseHelper.TABLE_CART, values,
                    DatabaseHelper.COLUMN_CART_USER_ID + "=? AND " +
                            DatabaseHelper.COLUMN_CART_PRODUCT_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
            cursor.close();
            return result;
        }

        // Nếu chưa có, thêm mới
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CART_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_CART_PRODUCT_ID, productId);
        values.put(DatabaseHelper.COLUMN_CART_QUANTITY, quantity);
        return database.insert(DatabaseHelper.TABLE_CART, null, values);
    }

    // === Lấy toàn bộ giỏ hàng theo user ===
    public List<Cart> getCartByUserId(int userId) {
        List<Cart> cartList = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_CART,
                null,
                DatabaseHelper.COLUMN_CART_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Cart cart = new Cart();
                cart.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_ID)));
                cart.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_USER_ID)));
                cart.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_PRODUCT_ID)));
                cart.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_QUANTITY)));
                cartList.add(cart);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cartList;
    }

    // === Xoá sản phẩm khỏi giỏ ===
    public void removeFromCart(int cartId) {
        database.delete(DatabaseHelper.TABLE_CART,
                DatabaseHelper.COLUMN_CART_ID + "=?",
                new String[]{String.valueOf(cartId)});
    }

    // === Cập nhật số lượng ===
    public void updateQuantity(int cartId, int newQuantity) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CART_QUANTITY, newQuantity);
        database.update(DatabaseHelper.TABLE_CART, values,
                DatabaseHelper.COLUMN_CART_ID + "=?",
                new String[]{String.valueOf(cartId)});
    }

    // === Xoá toàn bộ giỏ của user ===
    public void clearCartByUserId(int userId) {
        database.delete(DatabaseHelper.TABLE_CART,
                DatabaseHelper.COLUMN_CART_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }


}
