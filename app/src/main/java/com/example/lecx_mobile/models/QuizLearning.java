package com.example.lecx_mobile.models;

public class QuizLearning {
    public int id;
    public int quizId;
    public int accountId;
    public String learnedFlashCard;
    public int learningFlashcardId;
    public boolean status;

    public QuizLearning() {}

    public QuizLearning(int quizId, int accountId, String learnedFlashCard, int learningFlashcardId, boolean status) {
        this.quizId = quizId;
        this.accountId = accountId;
        this.learnedFlashCard = learnedFlashCard;
        this.learningFlashcardId = learningFlashcardId;
        this.status = status;
    }
}
