package com.example.lecx_mobile.repositories.implementations;

import com.example.lecx_mobile.models.Account;
import com.example.lecx_mobile.repositories.interfaces.IAccountRepository;
import com.example.lecx_mobile.utils.FirebaseUtils;
import java.util.concurrent.CompletableFuture;

public class AccountRepository
        extends GenericRepository<Account>
        implements IAccountRepository {

    public AccountRepository() {
        super(FirebaseUtils.accountsRef(), Account.class);
    }
    @Override
    public CompletableFuture<Account> getByEmail(String email) {

        return firstOrDefault(account ->
                account.email.equalsIgnoreCase(email)
        );
    }

    @Override
    public CompletableFuture<Boolean> checkEmailExist(String email) {
        return exists(account ->
                account.email.equalsIgnoreCase(email)
        );
    }
}
