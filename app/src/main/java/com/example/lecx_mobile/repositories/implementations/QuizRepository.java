package com.example.lecx_mobile.repositories.implementations;

import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.repositories.interfaces.IQuizRepository;
import com.example.lecx_mobile.utils.FirebaseDatabaseUtils;
import com.example.lecx_mobile.utils.PredicateUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class QuizRepository
        extends GenericRepository<Quiz>
        implements IQuizRepository {

    public QuizRepository() {
        super(FirebaseDatabaseUtils.quizzesRef(), Quiz.class);
    }

    public CompletableFuture<List<Quiz>> findByKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAll();
        }

        // Tạo predicate cho từng field cần search (dùng OR logic)
        Predicate<Quiz> titlePredicate = PredicateUtils.containsPredicate("title", keyword);
        Predicate<Quiz> descriptionPredicate = PredicateUtils.containsPredicate("description", keyword);

        // Kết hợp với OR logic: title chứa keyword HOẶC description chứa keyword
        Predicate<Quiz> combinedPredicate = titlePredicate.or(descriptionPredicate);

        // Gọi hàm where với predicate đã kết hợp
        return where(combinedPredicate);
    }
}
