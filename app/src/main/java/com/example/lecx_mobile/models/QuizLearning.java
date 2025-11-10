package com.example.lecx_mobile.models;

public class QuizLearning {
    public int id;
    public int quizId;
    public int accountId;
    public int learnedFlashCard;
    public int learningFlashcardId;

    public QuizLearning() {}

    public QuizLearning(int quizId, int accountId, int learnedFlashCard, int learningFlashcardId) {
        this.quizId = quizId;
        this.accountId = accountId;
        this.learnedFlashCard = learnedFlashCard;
        this.learningFlashcardId = learningFlashcardId;
    }
}
