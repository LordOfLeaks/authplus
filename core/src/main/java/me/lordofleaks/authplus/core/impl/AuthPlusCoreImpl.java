package me.lordofleaks.authplus.core.impl;

import lombok.Getter;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.AuthPlusException;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.account.AccountValidator;
import me.lordofleaks.authplus.core.account.impl.AccountValidatorImpl;
import me.lordofleaks.authplus.core.account.impl.MysqlAccountRepository;
import me.lordofleaks.authplus.core.account.impl.SqliteAccountRepository;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import me.lordofleaks.authplus.core.hasher.PasswordHasher;
import me.lordofleaks.authplus.core.hasher.impl.PasswordHasherImpl;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.mojang.impl.MojangApiImpl;
import me.lordofleaks.authplus.core.session.SessionStorage;
import me.lordofleaks.authplus.core.session.impl.SessionStorageImpl;

@Getter
public class AuthPlusCoreImpl implements AuthPlusCore {

    private final AuthPlusConfiguration configuration;
    private final PasswordHasher passwordHasher;
    private final MojangApi mojangApi;
    private final SessionStorage sessionStorage;
    private final AccountValidator accountValidator;
    private final AccountRepository accountRepository;

    public AuthPlusCoreImpl(AuthPlusConfiguration configuration) {
        this.configuration = configuration;
        this.passwordHasher = new PasswordHasherImpl(configuration.getEncryption().getIterationCount());
        this.mojangApi = new MojangApiImpl();
        this.sessionStorage = new SessionStorageImpl();
        this.accountValidator = new AccountValidatorImpl();
        switch (configuration.getStorage().getType()) {
            case MYSQL:
                this.accountRepository = new MysqlAccountRepository(
                        configuration.getStorage().getHost(),
                        configuration.getStorage().getPort(),
                        configuration.getStorage().getDatabase(),
                        configuration.getStorage().getUsername(),
                        configuration.getStorage().getPassword()
                );
                break;
            case SQLITE:
                this.accountRepository = new SqliteAccountRepository(configuration.getStorage().getFile());
                break;
            default:
                throw new AuthPlusException("Unknown storage type");
        }
    }

    @Override
    public void close() {
        accountRepository.close();
    }
}