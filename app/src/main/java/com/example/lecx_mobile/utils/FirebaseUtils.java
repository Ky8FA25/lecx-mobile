package com.example.lecx_mobile.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    // URL của Realtime Database (Giả sử bạn đang dùng vùng asia-southeast1)
    private static final String DB_URL =
            "https://online-88d8b-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private static FirebaseDatabase db() {
        return FirebaseDatabase.getInstance(DB_URL);
    }

    // Lấy DatabaseReference cho Collection Accounts
    public static DatabaseReference accountsRef() {
        return db().getReference(Constants.NODE_ACCOUNTS);
    }

    // Lấy DatabaseReference cho Collection Quizzes
    public static DatabaseReference quizzesRef() {
        return db().getReference(Constants.NODE_QUIZZES);
    }

    // Lấy DatabaseReference cho Collection Flashcards
    public static DatabaseReference flashcardsRef() {
        return db().getReference(Constants.NODE_FLASHCARDS);
    }

    // Lấy DatabaseReference cho Collection QuizLearnings (Tiến trình học)
    public static DatabaseReference quizLearningsRef() {
        return db().getReference(Constants.NODE_QUIZ_LEARNINGS);
    }

    // Lấy DatabaseReference cho Collection Questions
    public static DatabaseReference questionsRef() {
        return db().getReference(Constants.NODE_QUESTIONS);
    }
}
