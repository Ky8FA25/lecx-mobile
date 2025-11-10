package com.example.lecx_mobile.models;

public class Question {
    public int id;
    public int quizId;
    public String question;
    public String questionImg; // nullable
    public String answerA;
    public String answerB;
    public String answerC;
    public String answerD;
    public String correctAnswer;

    public Question() {}

    public Question(int quizId, String question, String questionImg,
                    String answerA, String answerB, String answerC, String answerD, String correctAnswer) {
        this.quizId = quizId;
        this.question = question;
        this.questionImg = questionImg;
        this.answerA = answerA;
        this.answerB = answerB;
        this.answerC = answerC;
        this.answerD = answerD;
        this.correctAnswer = correctAnswer;
    }
}
