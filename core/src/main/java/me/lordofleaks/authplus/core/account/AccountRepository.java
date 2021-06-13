package me.lordofleaks.authplus.core.account;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountRepository {

    /**
     * Closes this any underlying resources related to this repository.
     */
    void close();

    /**
     * Gets account by provided UUID from the repository.
     * @param uuid UUID of the account.
     * @return Future returning account with given UUID or {@code null} if not found.
     */
    @NotNull
    CompletableFuture<Account> getAccountByUuid(UUID uuid);

    /**
     * Inserts new account to this repository.
     *
     * @param account Account to be inserted.
     * @return Future responsible for inserting given account.
     */
    @NotNull
    CompletableFuture<Void> insertAccount(Account account);

    /**
     * Updates existing account in this repository.
     *
     * @param account Account to be updated.
     * @return Future responsible for updating given account.
     */
    @NotNull
    CompletableFuture<Void> updateAccount(Account account);

}