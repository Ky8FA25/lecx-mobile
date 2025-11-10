package com.example.lecx_mobile.models;

public class Flashcard {
    public int id;
    public int quizId;
    public String frontText;
    public String backText;
    public String frontImg; // nullable

    public Flashcard() {}

    public Flashcard(int quizId, String frontText, String backText, String frontImg) {
        this.quizId = quizId;
        this.frontText = frontText;
        this.backText = backText;
        this.frontImg = frontImg;
    }
}
