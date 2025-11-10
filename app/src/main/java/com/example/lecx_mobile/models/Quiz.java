package com.example.lecx_mobile.models;

public class Quiz {
    public int id;
    public String title;
    public String description;
    public int accountId;
    public int numberOfFlashcards;
    public boolean isPublic;
    public long createdAt;
    public long updatedAt;

    public Quiz() {}

    public Quiz(String title, String description, int accountId, int numberOfFlashcards, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.accountId = accountId;
        this.numberOfFlashcards = numberOfFlashcards;
        this.isPublic = isPublic;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
