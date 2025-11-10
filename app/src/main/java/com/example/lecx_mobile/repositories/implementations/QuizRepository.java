package com.example.lecx_mobile.repositories.implementations;

import com.example.lecx_mobile.models.Quiz;
import com.example.lecx_mobile.utils.FirebaseUtils;

public class QuizRepository extends GenericRepository<Quiz> {

    public QuizRepository() {
        super(FirebaseUtils.quizzesRef(), Quiz.class);
    }
}
