package me.lordofleaks.authplus.core;

import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import me.lordofleaks.authplus.core.hasher.PasswordHasher;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.account.AccountValidator;
import me.lordofleaks.authplus.core.session.SessionStorage;

public interface AuthPlusCore {

    PasswordHasher getPasswordHasher();

    MojangApi getMojangApi();

    AuthPlusConfiguration getConfiguration();

    SessionStorage getSessionStorage();

    AccountValidator getAccountValidator();

    AccountRepository getAccountRepository();

}