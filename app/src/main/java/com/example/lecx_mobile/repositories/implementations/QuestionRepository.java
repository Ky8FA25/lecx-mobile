package com.example.lecx_mobile.repositories.implementations;

import com.example.lecx_mobile.models.Question;
import com.example.lecx_mobile.utils.FirebaseUtils;

public class QuestionRepository extends GenericRepository<Question> {

    public QuestionRepository() {
        super(FirebaseUtils.questionsRef(), Question.class);
    }
}
