package me.lordofleaks.authplus.core.account;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountRepository {

    void close();

    CompletableFuture<Account> getAccountByUuid(UUID uuid);

    CompletableFuture<Void> insertAccount(Account account);

    CompletableFuture<Void> updateAccount(Account account);

}