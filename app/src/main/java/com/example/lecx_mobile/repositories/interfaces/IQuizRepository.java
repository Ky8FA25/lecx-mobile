package com.example.lecx_mobile.repositories.interfaces;

import com.example.lecx_mobile.models.Flashcard;
import com.example.lecx_mobile.models.Quiz;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IQuizRepository extends IGenericRepository<Quiz> {
    CompletableFuture<List<Quiz>> findByKeyword(String keyword);
}
