package com.example.lecx_mobile.utils;

public final class Constants {
    private Constants() {}

    // =======================================================
    // 🚩 CẤU HÌNH PHÁT TRIỂN
    // =======================================================

    // Đổi qua lại nếu bạn dùng JSON Server (chỉ dùng cho mục đích phát triển)
    public static final boolean USE_JSON_SERVER = false;

    // URL cho máy chủ JSON (AVD thường dùng 10.0.2.2)
    public static final String BASE_URL = "http://10.0.2.2:3000/";

    // =======================================================
    // 📂 FIREBASE REALTIME DATABASE NODES
    // =======================================================

    public static final String NODE_ACCOUNTS       = "Accounts";
    public static final String NODE_QUIZZES        = "Quizzes";
    public static final String NODE_FLASHCARDS     = "Flashcards";
    public static final String NODE_QUIZ_LEARNINGS = "QuizLearnings";
    public static final String NODE_QUESTIONS      = "Questions";

    // =======================================================
    // 📂 FIREBASE REALTIME DATABASE NODES
    // =======================================================
    public static final String FOLDER_AVATARS = "avatars";
    public static final String FOLDER_FLASHCARD_IMAGES = "flashcard-images";

    // =======================================================
    // 💾 SHARED PREFERENCES KEYS
    // =======================================================

    public static final String PREF_AUTH = "auth_pref";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_REMEMBER = "remember";
//    public static final String KEY_IS_ADMIN = "isAdmin"; // Cần nếu có vai trò Admin

    // =======================================================
    // 🗓️ ĐỊNH DẠNG & MẶC ĐỊNH
    // =======================================================

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/online-88d8b.appspot.com/o/avatars%2Fdefault-avatar.png?alt=media&token=d1675207-b896-4a85-acd8-f94e55adecce";

    // =======================================================
    // 🔄 THỨ TỰ SẮP XẾP (Sorting) - Chỉ là hằng số
    // =======================================================

    public static final String ORDER_BY_NAME_ASC   = "name_asc";
    public static final String ORDER_BY_NAME_DESC  = "name_desc";
    public static final String ORDER_BY_DATE_ASC   = "date_asc";
    public static final String ORDER_BY_DATE_DESC  = "date_desc";

    // =======================================================
    // 🤖 GEMINI API
    // =======================================================

    // TODO: Thay thế bằng API key thực tế của bạn
    // Hoặc đọc từ BuildConfig hoặc local.properties
    public static final String GEMINI_API_KEY = "AIzaSyBqTg19dWTEiFVQtswu0U63sq-ftZ1Fbzw"; // Để trống nếu chưa có key
}