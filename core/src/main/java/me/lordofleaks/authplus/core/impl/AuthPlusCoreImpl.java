package me.lordofleaks.authplus.core.impl;

import lombok.Getter;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.AuthPlusException;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.account.AccountValidator;
import me.lordofleaks.authplus.core.account.impl.AccountValidatorImpl;
import me.lordofleaks.authplus.core.account.impl.MysqlAccountRepository;
import me.lordofleaks.authplus.core.account.impl.SqliteAccountRepository;
import me.lordofleaks.authplus.core.comm.Communicator;
import me.lordofleaks.authplus.core.comm.impl.JsonCommunicator;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import me.lordofleaks.authplus.core.config.msg.AuthPlusMessageConfiguration;
import me.lordofleaks.authplus.core.config.msg.impl.AuthPlusMessageConfigurationImpl;
import me.lordofleaks.authplus.core.hasher.PasswordHasher;
import me.lordofleaks.authplus.core.hasher.impl.PasswordHasherImpl;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.login.impl.PremiumAwareLoginEngine;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.mojang.impl.MojangApiImpl;
import me.lordofleaks.authplus.core.mojang.impl.MojangApiResponseParserImpl;
import me.lordofleaks.authplus.core.session.SessionStorage;
import me.lordofleaks.authplus.core.session.impl.SessionStorageImpl;

import java.nio.file.Path;

@Getter
public class AuthPlusCoreImpl implements AuthPlusCore {

    private final AuthPlusConfiguration configuration;
    private final PasswordHasher passwordHasher;
    private final MojangApi mojangApi;
    private final SessionStorage sessionStorage;
    private final AccountValidator accountValidator;
    private final AccountRepository accountRepository;
    private final Communicator communicator;
    private final LoginEngine loginEngine;
    private final AuthPlusMessageConfiguration messageConfiguration;

    public AuthPlusCoreImpl(Path workdir, AuthPlusConfiguration configuration, boolean database) {
        this.configuration = configuration;
        this.messageConfiguration = new AuthPlusMessageConfigurationImpl();
        this.passwordHasher = new PasswordHasherImpl(configuration.getEncryption().getIterationCount());
        this.mojangApi = new MojangApiImpl(new MojangApiResponseParserImpl());
        this.sessionStorage = new SessionStorageImpl();
        this.accountValidator = new AccountValidatorImpl();
        if(database) {
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
                    this.accountRepository = new SqliteAccountRepository(workdir.resolve(configuration.getStorage().getFile()).toString());
                    break;
                default:
                    throw new AuthPlusException("Unknown storage type");
            }
            this.loginEngine = new PremiumAwareLoginEngine(accountRepository, mojangApi);
        } else {
            this.accountRepository = null;
            this.loginEngine = null;
        }
        this.communicator = new JsonCommunicator(sessionStorage, accountRepository, 5);
    }

    @Override
    public void close() {
        accountRepository.close();
    }
}