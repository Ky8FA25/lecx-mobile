package com.example.pe.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ShopApp.db";
    private static final int DATABASE_VERSION = 2;

    // ===== Bảng USERS =====
    public static final String TABLE_USER = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FULLNAME = "fullname";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    public static final String COLUMN_ROLE = "role";

    // ===== Bảng CATEGORY =====
    public static final String TABLE_CATEGORY = "categories";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_IMAGE = "img_url";
    public static final String COLUMN_CATEGORY_TYPE = "type";



    // ===== Bảng PRODUCT =====
    public static final String TABLE_PRODUCT = "products";
    public static final String COLUMN_PRODUCT_ID = "id";
    public static final String COLUMN_PRODUCT_NAME = "name";
    public static final String COLUMN_PRODUCT_PRICE = "price";
    public static final String COLUMN_PRODUCT_DESC = "description";
    public static final String COLUMN_PRODUCT_IMAGE = "image";
    public static final String COLUMN_PRODUCT_CATEGORY_ID = "categoryId";


    // ===== Bảng CART =====
    public static final String TABLE_CART = "cart";
    public static final String COLUMN_CART_ID = "id";
    public static final String COLUMN_CART_USER_ID = "userId";
    public static final String COLUMN_CART_PRODUCT_ID = "productId";
    public static final String COLUMN_CART_QUANTITY = "quantity";


    // ===== Bảng ORDER =====
    public static final String TABLE_ORDER = "orders";
    public static final String COLUMN_ORDER_ID = "id";
    public static final String COLUMN_ORDER_USER_ID = "user_id";
    public static final String COLUMN_TOTAL_PRICE = "total_price";
    public static final String COLUMN_DATE = "order_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ===== 1. Tạo bảng USER =====
        String CREATE_TABLE_USER =
                "CREATE TABLE " + TABLE_USER + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_FULLNAME + " TEXT, " +
                        COLUMN_EMAIL + " TEXT UNIQUE, " +
                        COLUMN_ROLE +" TEXT, " +
                        COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_TABLE_USER);

        // ===== 2. Tạo bảng CATEGORY =====
        String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + " (" +
                COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY_NAME + " TEXT, " +
                COLUMN_CATEGORY_IMAGE + " TEXT, " +
                COLUMN_CATEGORY_TYPE + " TEXT)";
        db.execSQL(CREATE_TABLE_CATEGORY);

        // ===== 3. Tạo bảng PRODUCT =====
        String CREATE_TABLE_PRODUCT = "CREATE TABLE " + TABLE_PRODUCT + " (" +
                COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCT_NAME + " TEXT, " +
                COLUMN_PRODUCT_PRICE + " REAL, " +
                COLUMN_PRODUCT_DESC + " TEXT, " +
                COLUMN_PRODUCT_IMAGE + " TEXT, " +
                COLUMN_PRODUCT_CATEGORY_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_PRODUCT_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + COLUMN_CATEGORY_ID + "))";
        db.execSQL(CREATE_TABLE_PRODUCT);

        // ===== 4. Tạo bảng CART =====
        String CREATE_TABLE_CART = "CREATE TABLE " + TABLE_CART + " (" +
                COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CART_USER_ID + " INTEGER, " +
                COLUMN_CART_PRODUCT_ID + " INTEGER, " +
                COLUMN_CART_QUANTITY + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_CART_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY(" + COLUMN_CART_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCT + "(" + COLUMN_PRODUCT_ID + "))";
        db.execSQL(CREATE_TABLE_CART);

        // ===== 5. Tạo bảng ORDER =====
        String CREATE_TABLE_ORDER = "CREATE TABLE " + TABLE_ORDER + " (" +
                COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ORDER_USER_ID + " INTEGER, " +
                COLUMN_TOTAL_PRICE + " REAL, " +
                COLUMN_DATE + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_ORDER_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_TABLE_ORDER);

        // Nạp sẵn dữ liệu mẫu
        // --- Users ---
        db.execSQL("INSERT INTO " + TABLE_USER + " (" +
                COLUMN_FULLNAME + ", " +
                COLUMN_EMAIL + ", " +
                COLUMN_PASSWORD + ", " +
                COLUMN_ROLE + ") VALUES " +
                "('Admin User', 'admin@gmail.com', '123456', 'Admin')," +
                "('Nguyen Van A', 'a@gmail.com', '123456', 'Customer')," +
                "('Tran Thi B', 'b@gmail.com', '123456', 'Customer');");

        // --- Categories ---
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" +
                COLUMN_CATEGORY_NAME + ", " + COLUMN_CATEGORY_IMAGE + ", " + COLUMN_CATEGORY_TYPE + ") VALUES " +
                "('Electronics', 'https://t4.ftcdn.net/jpg/03/64/41/07/360_F_364410756_Ev3WoDfNyxO9c9n4tYIsU5YBQWAP3UF8.jpg', 'electronics')," +
                "('Men', 'https://godwincharli.com/cdn/shop/collections/Group_2_40d59f9c-5889-497f-8361-87d06bf55551.jpg?v=1649759736', 'clothing')," +
                "('Home Appliances', 'https://cdn.firstcry.com/education/2023/01/13101355/Names-Of-Household-Appliances-In-English.jpg', 'home');");

        // --- Products ---
        db.execSQL("INSERT INTO " + TABLE_PRODUCT + " (" +
                COLUMN_PRODUCT_NAME + ", " + COLUMN_PRODUCT_PRICE + ", " +
                COLUMN_PRODUCT_DESC + ", " + COLUMN_PRODUCT_IMAGE + ", " + COLUMN_PRODUCT_CATEGORY_ID + ") VALUES " +
                "('Smartphone X', 999.99, 'Latest smartphone with OLED display', 'https://cdn.nguyenkimmall.com/images/detailed/985/OPPO_A3_6GB128GB_%C4%90en__CPH2669_1.jpg', 1)," +
                "('Laptop Pro', 1500.00, 'Powerful laptop for professionals', 'https://anphat.com.vn/media/lib/04-11-2024/laptop-asus-expertbook-p1403cva-i5se16-63ws-4.jpg', 1)," +
                "('T-shirt Blue', 19.99, '100% cotton T-shirt', 'https://image.uniqlo.com/UQ/ST3/vn/imagesgoods/477199/item/vngoods_66_477199_3x4.jpg?width=494', 2)," +
                "('Microwave Oven', 120.50, 'Compact microwave for home use', 'https://cdnv2.tgdd.vn/mwg-static/dmx/Products/Images/1987/332031/lo-vi-song-lg-ms2535gik-25-lit-1-638684866921607521-700x467.jpg', 3);");


    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER);
        onCreate(db);
    }
}
