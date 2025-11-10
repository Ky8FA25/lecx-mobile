package com.example.lecx_mobile.repositories.interfaces;

import com.example.lecx_mobile.models.Account;

import java.util.concurrent.CompletableFuture;

public interface IAccountRepository extends IGenericRepository<Account> {
    CompletableFuture<Account> getByEmail(String email);
    CompletableFuture<Boolean> checkEmailExist(String email);
}
