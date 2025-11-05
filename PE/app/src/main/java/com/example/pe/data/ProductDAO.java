package com.example.pe.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pe.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ProductDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ===== Mở / Đóng DB =====
    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // ===== 1. Thêm sản phẩm =====
    public long insertProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUCT_NAME, product.getName());
        values.put(DatabaseHelper.COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(DatabaseHelper.COLUMN_PRODUCT_DESC, product.getDescription());
        values.put(DatabaseHelper.COLUMN_PRODUCT_IMAGE, product.getImage());
        values.put(DatabaseHelper.COLUMN_PRODUCT_CATEGORY_ID, product.getCategoryId());

        return db.insert(DatabaseHelper.TABLE_PRODUCT, null, values);
    }

    // ===== 2. Lấy tất cả sản phẩm =====
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PRODUCT,
                null, // all columns
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_ID)));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_NAME)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_PRICE)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_DESC)));
                product.setImage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_IMAGE)));
                product.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_CATEGORY_ID)));

                productList.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return productList;
    }

    // ===== 3. Lấy sản phẩm theo ID =====
    public Product getProductById(int id) {
        Product product = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_PRODUCT,
                null,
                DatabaseHelper.COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            product = new Product();
            product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_ID)));
            product.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_NAME)));
            product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_PRICE)));
            product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_DESC)));
            product.setImage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_IMAGE)));
            product.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_CATEGORY_ID)));
            cursor.close();
        }

        return product;
    }

    // ===== 4. Lấy sản phẩm theo Category =====
    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> productList = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PRODUCT,
                null,
                DatabaseHelper.COLUMN_PRODUCT_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_ID)));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_NAME)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_PRICE)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_DESC)));
                product.setImage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_IMAGE)));
                product.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_CATEGORY_ID)));
                productList.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return productList;
    }

    // ===== 4b. Lấy sản phẩm theo tên Category =====
    public List<Product> getProductsByCategoryName(String categoryName) {
        List<Product> productList = new ArrayList<>();

        Cursor cursor = db.query(DatabaseHelper.TABLE_PRODUCT,
                null,
                DatabaseHelper.COLUMN_CATEGORY_NAME + " = ?",
                new String[]{categoryName},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_ID)));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_NAME)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_PRICE)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_DESC)));
                product.setImage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_IMAGE)));
                product.setCategoryId(0); // không dùng id
                productList.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return productList;
    }

    // ===== 5. Cập nhật sản phẩm =====
    public int updateProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUCT_NAME, product.getName());
        values.put(DatabaseHelper.COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(DatabaseHelper.COLUMN_PRODUCT_DESC, product.getDescription());
        values.put(DatabaseHelper.COLUMN_PRODUCT_IMAGE, product.getImage());
        values.put(DatabaseHelper.COLUMN_PRODUCT_CATEGORY_ID, product.getCategoryId());

        return db.update(DatabaseHelper.TABLE_PRODUCT,
                values,
                DatabaseHelper.COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(product.getId())});
    }

    // ===== 6. Xóa sản phẩm =====
    public int deleteProduct(int id) {
        return db.delete(DatabaseHelper.TABLE_PRODUCT,
                DatabaseHelper.COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
}
