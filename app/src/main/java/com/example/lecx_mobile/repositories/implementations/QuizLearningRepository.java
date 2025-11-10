package com.example.lecx_mobile.repositories.implementations;

import com.example.lecx_mobile.models.QuizLearning;
import com.example.lecx_mobile.utils.FirebaseUtils;

public class QuizLearningRepository extends GenericRepository<QuizLearning> {

    public QuizLearningRepository() {
        super(FirebaseUtils.quizLearningsRef(), QuizLearning.class);
    }
}
