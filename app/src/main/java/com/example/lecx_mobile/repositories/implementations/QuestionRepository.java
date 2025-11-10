package com.example.lecx_mobile.repositories.implementations;

import com.example.lecx_mobile.models.Question;
import com.example.lecx_mobile.repositories.interfaces.IQuestionRepository;
import com.example.lecx_mobile.utils.FirebaseDatabaseUtils;

public class QuestionRepository
        extends GenericRepository<Question>
        implements IQuestionRepository {

    public QuestionRepository() {
        super(FirebaseDatabaseUtils.questionsRef(), Question.class);
    }
}
