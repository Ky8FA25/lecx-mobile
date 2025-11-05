package com.example.pe.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pe.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;


    public CategoryDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }
    // 👉 Mở kết nối database
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    // 👉 Đóng kết nối database
    public void close() {
        dbHelper.close();
    }

    // Lấy tất cả category từ DB
    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_CATEGORY;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
                String imgUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_IMAGE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_TYPE));

                Category category = new Category(id ,imgUrl, name, type);
                categoryList.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categoryList;
    }

    public String getCategoryNameById(int categoryId) {
        String name = "Unknown Category";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseHelper.COLUMN_CATEGORY_NAME +
                        " FROM " + DatabaseHelper.TABLE_CATEGORY +
                        " WHERE " + DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)}
        );

        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }
}
