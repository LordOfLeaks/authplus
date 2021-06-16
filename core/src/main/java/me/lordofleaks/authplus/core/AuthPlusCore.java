package me.lordofleaks.authplus.core;

import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.comm.Communicator;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import me.lordofleaks.authplus.core.config.msg.AuthPlusMessageConfiguration;
import me.lordofleaks.authplus.core.hasher.PasswordHasher;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.account.AccountValidator;
import me.lordofleaks.authplus.core.session.SessionStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    AccountRepository getAccountRepository();

    @NotNull
    Communicator getCommunicator();

    @Nullable
    LoginEngine getLoginEngine();

    /**
     * Returns message configuration.
     * Responsibility to load it lies at the plugin-side since it may register new messages.
     *
     * @return Message configuration.
     */
    @NotNull
    AuthPlusMessageConfiguration getMessageConfiguration();

    /**
     * Closes all underlying resources used by AuthPlus.
     */
    void close();

}