package me.lordofleaks.authplus.core.login.impl;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.account.AccountValidator;
import me.lordofleaks.authplus.core.login.AuthPlusLoginException;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.session.Session;
import me.lordofleaks.authplus.core.session.SessionStorage;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * LoginEngine that automatically registers premium users.
 */
@RequiredArgsConstructor
public class PremiumAwareLoginEngine implements LoginEngine {

    private final AccountValidator accountValidator;
    private final AccountRepository accountRepository;
    private final MojangApi mojangApi;

    @Override
    public CompletableFuture<Session> performLogin(String name, Function<String, UUID> nameToUuid) {
        if(!accountValidator.isAccountNameValid(name)) {
            throw new AuthPlusLoginException("Provided account name is not valid.");
        }
        return accountRepository.getAccountByName(name).thenCompose(res -> {
            if (res == null) {
                return mojangApi.getUniqueIdByName(name).thenCompose(uuid -> {
                    if (uuid == null) {
                        //no premium user without an account
                        Account account = new Account(nameToUuid.apply(name));
                        return CompletableFuture.completedFuture(new Session(account));
                    } else {
                        return accountRepository.getAccountByUniqueId(uuid).thenCompose(account -> {
                            if (account == null) {
                                //premium user without an account
                                Account acc = new Account(uuid);
                                acc.setName(name);
                                acc.setRegisteredPremium(true);
                                acc.setRegistered(true);
                                return accountRepository.updateAccount(acc).thenApply(ignored -> new Session(acc));
                            } else {
                                //premium user with account who changed their name
                                account.setName(name);
                                return accountRepository.updateAccount(account).thenApply(ignored -> new Session(account));
                            }
                        });
                    }
                });
            } else {
                //premium user who did not change his name or no premium user
                return CompletableFuture.completedFuture(new Session(res));
            }
        });
    }
}
