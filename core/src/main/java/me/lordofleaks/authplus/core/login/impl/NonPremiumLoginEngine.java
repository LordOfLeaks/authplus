package me.lordofleaks.authplus.core.login.impl;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.session.Session;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * LoginEngine that treats all users as if they were no premium.
 */
@RequiredArgsConstructor
public class NonPremiumLoginEngine implements LoginEngine {

    @NotNull
    private final AccountRepository accountRepository;

    @Override
    public CompletableFuture<Session> performLogin(String name, Function<String, UUID> nameToUuid) {
        return accountRepository.getAccountByName(name).thenCompose(res -> {
            if (res == null) {
                Account account = new Account(nameToUuid.apply(name));
                account.setName(name);
                return CompletableFuture.completedFuture(new Session(account));
            } else {
                return CompletableFuture.completedFuture(new Session(res));
            }
        });
    }
}
