package com.example.lecx_mobile.repositories.interfaces;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface IGenericRepository<T> {

    // CRUD
    CompletableFuture<List<T>> getAll();
    CompletableFuture<T> getById(int id);
    CompletableFuture<T> add(T entity);
    CompletableFuture<T> update(T entity);
    CompletableFuture<Boolean> delete(int id);
    CompletableFuture<Boolean> delete(T entity);

    // QUERY
    CompletableFuture<Integer> count();
    CompletableFuture<Boolean> exists(Predicate<T> predicate);
    CompletableFuture<T> firstOrDefault(Predicate<T> predicate);
    CompletableFuture<List<T>> where(Predicate<T>... predicates);
}

