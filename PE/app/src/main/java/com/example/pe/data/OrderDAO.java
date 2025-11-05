package com.example.pe.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pe.models.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public OrderDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addOrder(Order order) {
        ContentValues values = new ContentValues();
        values.put("user_id", order.getUserId());
        values.put("total_price", order.getTotalPrice());
        values.put("order_date", order.getOrderDate());

        // Chèn vào bảng orders
        return db.insert("orders", null, values);
    }

    // ====================================================
    // 🔹 Lấy doanh thu tổng (tất cả đơn)
    // ====================================================
    public double getTotalRevenue() {
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(total_price) FROM orders", null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // ====================================================
    // 🔹 Doanh thu hôm nay
    // ====================================================
    public double getRevenueToday() {
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(total_price) FROM orders " +
                        "WHERE DATE(order_date) = DATE('now', 'localtime')", null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // ====================================================
    // 🔹 Doanh thu tháng này
    // ====================================================
    public double getRevenueThisMonth() {
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(total_price) FROM orders " +
                        "WHERE strftime('%Y-%m', order_date) = strftime('%Y-%m', 'now', 'localtime')",
                null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // 🔹 Doanh thu năm nay
    // ====================================================
    public double getRevenueThisYear() {
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(total_price) FROM orders WHERE strftime('%Y', order_date) = strftime('%Y', 'now', 'localtime')",
                null
        );
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }



    // 🔹 Doanh thu theo ngày cụ thể
    public double getRevenueByDate(String date) {
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(total_price) FROM orders WHERE DATE(order_date) = DATE(?)",
                new String[]{date});
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    // 🔹 Doanh thu theo tháng cụ thể
    public double getRevenueByMonth(int year, int month) {
        String ym = String.format("%04d-%02d", year, month);
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(total_price) FROM orders WHERE strftime('%Y-%m', order_date) = ?",
                new String[]{ym});
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    // 🔹 Doanh thu theo năm cụ thể
    public double getRevenueByYear(int year) {
        String y = String.format("%04d", year);
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(total_price) FROM orders WHERE strftime('%Y', order_date) = ?",
                new String[]{y});
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    // ====================================================
    // 🔹 Lấy tất cả đơn hàng
    // ====================================================
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT id, user_id, total_price, order_date FROM orders ORDER BY order_date DESC",
                null);

        if (cursor.moveToFirst()) {
            do {
                Order o = new Order();
                o.setId(cursor.getInt(0));
                o.setUserId(cursor.getInt(1));
                o.setTotalPrice(cursor.getDouble(2));
                o.setOrderDate(cursor.getString(3));
                orders.add(o);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    // ====================================================
    // 🔹 Lấy đơn hôm nay
    // ====================================================
    public List<Order> getOrdersToday() {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT id, user_id, total_price, order_date FROM orders " +
                        "WHERE DATE(order_date) = DATE('now', 'localtime') " +
                        "ORDER BY order_date DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Order o = new Order();
                o.setId(cursor.getInt(0));
                o.setUserId(cursor.getInt(1));
                o.setTotalPrice(cursor.getDouble(2));
                o.setOrderDate(cursor.getString(3));
                orders.add(o);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    // ====================================================
    // 🔹 Lấy đơn trong tháng này
    // ====================================================
    public List<Order> getOrdersThisMonth() {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT id, user_id, total_price, order_date FROM orders " +
                        "WHERE strftime('%Y-%m', order_date) = strftime('%Y-%m', 'now', 'localtime') " +
                        "ORDER BY order_date DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Order o = new Order();
                o.setId(cursor.getInt(0));
                o.setUserId(cursor.getInt(1));
                o.setTotalPrice(cursor.getDouble(2));
                o.setOrderDate(cursor.getString(3));
                orders.add(o);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public List<Order> getOrdersThisYear() {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT id, user_id, total_price, order_date FROM orders " +
                        "WHERE strftime('%Y', order_date) = strftime('%Y', 'now', 'localtime') " +
                        "ORDER BY order_date DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Order o = new Order();
                o.setId(cursor.getInt(0));
                o.setUserId(cursor.getInt(1));
                o.setTotalPrice(cursor.getDouble(2));
                o.setOrderDate(cursor.getString(3));
                orders.add(o);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    // 🔹 Các hàm linh hoạt hơn
    public List<Order> getOrdersByDate(String date) {
        List<Order> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM orders WHERE DATE(order_date) = DATE(?)",
                new String[]{date}
        );
        while (cursor.moveToNext()) {
            list.add(mapOrder(cursor));
        }
        cursor.close();
        return list;
    }


    public List<Order> getOrdersByMonth(int year, int month) {
        List<Order> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM orders WHERE strftime('%Y', order_date) = ? AND strftime('%m', order_date) = ?",
                new String[]{String.valueOf(year), String.format(Locale.getDefault(), "%02d", month)}
        );
        while (cursor.moveToNext()) {
            list.add(mapOrder(cursor));
        }
        cursor.close();
        return list;
    }

    public List<Order> getOrdersByYear(int year) {
        List<Order> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM orders WHERE strftime('%Y', order_date) = ?",
                new String[]{String.valueOf(year)}
        );
        while (cursor.moveToNext()) {
            list.add(mapOrder(cursor));
        }
        cursor.close();
        return list;
    }

    // 🔸 Helper để map dữ liệu từ Cursor sang Order
    private Order mapOrder(Cursor cursor) {
        Order o = new Order();
        o.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        o.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        o.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("total_price")));
        o.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
        return o;
    }
}
