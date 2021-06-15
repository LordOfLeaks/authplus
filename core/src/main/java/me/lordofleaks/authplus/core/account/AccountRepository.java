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
     * Gets account by provided name from the repository.
     * @param name Name of the account.
     * @return Future returning account with given name or {@code null} if not found.
     */
    @NotNull
    CompletableFuture<Account> getAccountByName(String name);

    /**
     * Gets account by provided UUID from the repository.
     * @param uuid UUID of the account.
     * @return Future returning account with given unique id or {@code null} if not found.
     */
    @NotNull
    CompletableFuture<Account> getAccountByUniqueId(UUID uuid);

    /**
     * Updates account or inserts new into this repository.
     *
     * @param account Account to be updated.
     * @return Future responsible for updating given account.
     */
    @NotNull
    CompletableFuture<Void> updateAccount(Account account);

}