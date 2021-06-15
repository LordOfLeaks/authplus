package me.lordofleaks.authplus.core;

import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.comm.Communicator;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import me.lordofleaks.authplus.core.hasher.PasswordHasher;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.account.AccountValidator;
import me.lordofleaks.authplus.core.session.SessionStorage;
import org.jetbrains.annotations.NotNull;

public interface AuthPlusCore {

    @NotNull
    PasswordHasher getPasswordHasher();

    @NotNull
    MojangApi getMojangApi();

    @NotNull
    AuthPlusConfiguration getConfiguration();

    @NotNull
    SessionStorage getSessionStorage();

    @NotNull
    AccountValidator getAccountValidator();

    @NotNull
    AccountRepository getAccountRepository();

    @NotNull
    Communicator getCommunicator();

    @NotNull
    LoginEngine getLoginEngine();

    /**
     * Closes all underlying resources used by AuthPlus.
     */
    void close();

}