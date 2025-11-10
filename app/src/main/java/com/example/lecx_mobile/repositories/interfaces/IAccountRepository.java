package com.example.lecx_mobile.repositories.interfaces;

import com.example.lecx_mobile.models.Account;

import java.util.concurrent.CompletableFuture;

public interface IAccountRepository extends IGenericRepository<Account> {
    // init: IAccountRepository repo = new AccountRepository();
    CompletableFuture<Account> getByEmail(String email);
    CompletableFuture<Boolean> existsByEmailAsync(String email);
}
