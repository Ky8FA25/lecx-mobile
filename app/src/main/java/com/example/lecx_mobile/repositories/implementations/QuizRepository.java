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

        // Tạo predicate cho từng field cần search
        Predicate<Quiz> namePredicate = PredicateUtils.containsPredicate("name", keyword);
        Predicate<Quiz> descriptionPredicate = PredicateUtils.containsPredicate("description", keyword);

        // Gọi hàm where với các predicate
        return where(namePredicate, descriptionPredicate);
    }
}
