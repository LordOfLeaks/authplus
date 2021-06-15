package me.lordofleaks.authplus.core.login.impl;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.account.AccountValidator;
import me.lordofleaks.authplus.core.login.AuthPlusLoginException;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.session.Session;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * LoginEngine that automatically registers premium users.
 */
@RequiredArgsConstructor
public class NonPremiumLoginEngine implements LoginEngine {

    private final AccountValidator accountValidator;
    private final AccountRepository accountRepository;

    @Override
    public CompletableFuture<Session> performLogin(String name, Function<String, UUID> nameToUuid) {
        if(!accountValidator.isAccountNameValid(name)) {
            throw new AuthPlusLoginException("Provided account name is not valid.");
        }
        return accountRepository.getAccountByName(name).thenCompose(res -> {
            if (res == null) {
                //no premium user without an account
                Account account = new Account(nameToUuid.apply(name));
                return CompletableFuture.completedFuture(new Session(account));
            } else {
                //premium user who did not change his name or no premium user
                return CompletableFuture.completedFuture(new Session(res));
            }
        });
    }
}
