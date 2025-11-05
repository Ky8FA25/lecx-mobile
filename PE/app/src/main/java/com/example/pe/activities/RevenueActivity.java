package com.example.pe.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.DatePicker;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pe.R;
import com.example.pe.adapters.OrderAdapter;
import com.example.pe.data.OrderDAO;
import com.example.pe.models.Order;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RevenueActivity extends AppCompatActivity {

    private TextView txtRevenueAmount;
    private Button btnToday, btnThisMonth, btnThisYear;
    private Button btnPickDate, btnPickMonth, btnPickYear;
    private RecyclerView recyclerOrders;

    private BarChart barChart;

    private OrderDAO orderDAO;
    private List<Order> orderList;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        txtRevenueAmount = findViewById(R.id.txt_revenue_amount);
        btnToday = findViewById(R.id.btn_today);
        btnThisMonth = findViewById(R.id.btn_this_month);
        btnThisYear = findViewById(R.id.btn_this_year);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnPickMonth = findViewById(R.id.btn_pick_month);
        btnPickYear = findViewById(R.id.btn_pick_year);
        recyclerOrders = findViewById(R.id.recycler_orders);
        barChart = findViewById(R.id.barChart);

        orderDAO = new OrderDAO(this);
        orderDAO.open();

        // Mặc định: tổng doanh thu
        showTotalRevenue();
        loadOrders(orderDAO.getAllOrders());

        btnToday.setOnClickListener(v -> {
            txtRevenueAmount.setText(String.format("$%.2f", orderDAO.getRevenueToday()));
            loadOrders(orderDAO.getOrdersToday());
        });

        btnThisMonth.setOnClickListener(v -> {
            txtRevenueAmount.setText(String.format("$%.2f", orderDAO.getRevenueThisMonth()));
            loadOrders(orderDAO.getOrdersThisMonth());
        });

        btnThisYear.setOnClickListener(v -> {
            txtRevenueAmount.setText(String.format("$%.2f", orderDAO.getRevenueThisYear()));
            loadOrders(orderDAO.getOrdersThisYear());
        });

        // Bộ chọn ngày
        btnPickDate.setOnClickListener(v -> showDatePicker());

        // Bộ chọn tháng
        btnPickMonth.setOnClickListener(v -> showMonthPicker());

        // Bộ chọn năm
        btnPickYear.setOnClickListener(v -> showYearPicker());

        setupMonthlyChart();

//        orderDAO.close();
    }

    private void showTotalRevenue() {
        double total = orderDAO.getTotalRevenue();
        txtRevenueAmount.setText(String.format("$%.2f", total));
    }

    private void loadOrders(List<Order> orders) {
        orderList = orders;
        adapter = new OrderAdapter(this, orderList);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);

            double total = orderDAO.getRevenueByDate(selectedDate);
            List<Order> orders = orderDAO.getOrdersByDate(selectedDate);

            txtRevenueAmount.setText(String.format("$%.2f", total));
            loadOrders(orders);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showMonthPicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        // Giả lập chọn tháng: dùng DatePickerDialog nhưng chỉ lấy tháng và năm
        new DatePickerDialog(this, (view, y, m, d) -> {
            String ym = String.format("%04d-%02d", y, m + 1);

            double total = orderDAO.getRevenueByMonth(y, m + 1);
            List<Order> orders = orderDAO.getOrdersByMonth(y, m + 1);

            txtRevenueAmount.setText(String.format("$%.2f", total));
            loadOrders(orders);
        }, year, month - 1, 1).show();
    }

    private void showYearPicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        // Dùng DatePickerDialog nhưng chỉ lấy năm
        new DatePickerDialog(this, (view, y, m, d) -> {

            double total = orderDAO.getRevenueByYear(y);
            List<Order> orders = orderDAO.getOrdersByYear(y);

            txtRevenueAmount.setText(String.format("$%.2f", total));
            loadOrders(orders);
        }, year, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void setupMonthlyChart() {
        List<BarEntry> entries = new ArrayList<>();
        int year = Calendar.getInstance().get(Calendar.YEAR);

        for (int month = 1; month <= 12; month++) {
            double revenue = orderDAO.getRevenueByMonth(year, month);
            entries.add(new BarEntry(month, (float) revenue));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Revenue");
        dataSet.setColor(getResources().getColor(R.color.purple_500));

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        Description desc = new Description();
        desc.setText("Revenue for " + year);
        barChart.setDescription(desc);

        barChart.animateY(1000);
        barChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        orderDAO.close(); // chỉ đóng khi activity thật sự bị hủy
    }
}
