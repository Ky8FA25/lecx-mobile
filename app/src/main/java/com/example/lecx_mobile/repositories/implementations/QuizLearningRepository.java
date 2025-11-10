package com.example.lecx_mobile.repositories.implementations;

import com.example.lecx_mobile.models.QuizLearning;
import com.example.lecx_mobile.repositories.interfaces.IQuizLearningRepository;
import com.example.lecx_mobile.utils.FirebaseUtils;

public class QuizLearningRepository
        extends GenericRepository<QuizLearning>
        implements IQuizLearningRepository {

    public QuizLearningRepository() {
        super(FirebaseUtils.quizLearningsRef(), QuizLearning.class);
    }
}
