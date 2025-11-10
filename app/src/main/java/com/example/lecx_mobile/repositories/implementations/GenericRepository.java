package com.example.lecx_mobile.repositories.implementations;

import androidx.annotation.NonNull;

import com.example.lecx_mobile.repositories.interfaces.IGenericRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GenericRepository<T> implements IGenericRepository<T> {

    protected final DatabaseReference dbRef;
    protected final Class<T> clazz;

    public GenericRepository(DatabaseReference dbRef, Class<T> clazz) {
        this.dbRef = dbRef;
        this.clazz = clazz;
    }

    // ================= GET ALL =================
    @Override
    public CompletableFuture<List<T>> getAll() {
        CompletableFuture<List<T>> future = new CompletableFuture<>();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    T entity = child.getValue(clazz);
                    if (entity != null) list.add(entity);
                }
                future.complete(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });

        return future;
    }

    // ================= GET BY ID =================
    @Override
    public CompletableFuture<T> getById(int id) {
        CompletableFuture<T> future = new CompletableFuture<>();

        dbRef.orderByChild("id").equalTo(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                T entity = child.getValue(clazz);
                                future.complete(entity);
                                return;
                            }
                        }
                        future.complete(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });

        return future;
    }

    // ================= ADD (tự sinh ID) =================
    @Override
    public CompletableFuture<T> add(T entity) {
        CompletableFuture<T> future = new CompletableFuture<>();

        try {
            Field idField = getEntityIdField(entity);
            if (idField == null)
                throw new IllegalArgumentException("Entity has no ID-like field");

            generateNewId().thenAccept(newId -> {
                try {
                    idField.setInt(entity, newId);
                    String nodeKey = String.valueOf(newId);

                    dbRef.child(nodeKey).setValue(entity)
                            .addOnSuccessListener(unused -> future.complete(entity))
                            .addOnFailureListener(future::completeExceptionally);

                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }).exceptionally(e -> {
                future.completeExceptionally(e);
                return null;
            });

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    // ================= UPDATE =================
    @Override
    public CompletableFuture<T> update(T entity) {
        CompletableFuture<T> future = new CompletableFuture<>();

        try {
            Field idField = getEntityIdField(entity);
            if (idField == null)
                throw new IllegalArgumentException("Entity has no ID-like field");

            int id = idField.getInt(entity);
            String nodeKey = String.valueOf(id);

            dbRef.child(nodeKey).setValue(entity)
                    .addOnSuccessListener(unused -> future.complete(entity))
                    .addOnFailureListener(future::completeExceptionally);

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    // ================= DELETE =================
    @Override
    public CompletableFuture<Boolean> delete(int id) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        dbRef.child(String.valueOf(id)).removeValue()
                .addOnSuccessListener(unused -> future.complete(true))
                .addOnFailureListener(e -> future.complete(false));

        return future;
    }

    @Override
    public CompletableFuture<Boolean> delete(T entity) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            Field idField = getEntityIdField(entity);
            if (idField == null)
                throw new IllegalArgumentException("Entity has no ID-like field");

            int id = idField.getInt(entity);
            dbRef.child(String.valueOf(id)).removeValue()
                    .addOnSuccessListener(unused -> future.complete(true))
                    .addOnFailureListener(e -> future.complete(false));

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    // ================= QUERY =================
    @Override
    public CompletableFuture<Integer> count() {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                future.complete((int) snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> exists(Predicate<T> predicate) {
        return where(predicate)
                .thenApply(results -> results != null && !results.isEmpty());
    }

    @Override
    public CompletableFuture<T> firstOrDefault(Predicate<T> predicate) {
        return getAll().thenApply(list -> {
            if (list == null) return null;
            for (T item : list) {
                if (predicate.test(item)) return item;
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<T>> where(Predicate<T>... predicates) {
        return getAll().thenApply(list -> {
            if (list == null) return new ArrayList<>();

            return list.stream()
                    .filter(item -> {
                        for (Predicate<T> predicate : predicates) {
                            if (!predicate.test(item)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        });
    }

    // ================= HELPERS =================
    private CompletableFuture<Integer> generateNewId() {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        getAll().thenAccept(list -> {
            int maxId = 0;
            if (list != null) {
                for (T item : list) {
                    try {
                        Field idField = getEntityIdField(item);
                        if (idField != null) {
                            int value = idField.getInt(item);
                            if (value > maxId) maxId = value;
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(
                                new RuntimeException("Cannot access ID field", e)
                        );
                        return; // dừng vòng lặp
                    }
                }
            }
            future.complete(maxId + 1);
        }).exceptionally(e -> {
            future.completeExceptionally(e);
            return null;
        });

        return future;
    }

    private Field getEntityIdField(T entity) {
        for (Field field : entity.getClass().getFields()) {
            String name = field.getName().toLowerCase();
            if (name.endsWith("id")) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }
}
