package me.lordofleaks.authplus.core.login.impl;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.session.Session;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * LoginEngine that automatically registers premium users.
 */
@RequiredArgsConstructor
public class PremiumAwareLoginEngine implements LoginEngine {

    @NotNull
    private final AccountRepository accountRepository;
    private final MojangApi mojangApi;

    @Override
    public CompletableFuture<Session> performLogin(String name, Function<String, UUID> nameToUuid) {
        return accountRepository.getAccountByName(name).thenCompose(res -> {
            if (res == null) {
                return mojangApi.getUniqueIdByName(name).thenCompose(uuid -> {
                    if (uuid == null) {
                        //no premium user without an account
                        Account account = new Account(nameToUuid.apply(name));
                        account.setName(name);
                        return CompletableFuture.completedFuture(new Session(account));
                    } else {
                        return accountRepository.getAccountByUniqueId(uuid).thenCompose(account -> {
                            if (account == null) {
                                //premium user without an account
                                Account acc = new Account(uuid);
                                acc.setName(name);
                                acc.setRegisteredPremium(true);
                                acc.setRegistered(true);
                                Session session = new Session(acc);
                                session.setAuthorized(true);
                                return accountRepository.updateAccount(acc).thenApply(ignored -> session);
                            } else {
                                //premium user with account who changed their name
                                account.setName(name);
                                Session session = new Session(account);
                                session.setAuthorized(true);
                                return accountRepository.updateAccount(account).thenApply(ignored -> session);
                            }
                        });
                    }
                });
            } else {
                //registered premium or no premium user
                Session session = new Session(res);
                if(res.isRegisteredPremium()) {
                    session.setAuthorized(true);
                }

                return CompletableFuture.completedFuture(session);
            }
        });
    }
}
