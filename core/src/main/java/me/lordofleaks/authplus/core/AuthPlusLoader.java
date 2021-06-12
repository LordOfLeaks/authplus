package me.lordofleaks.authplus.core;

import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;

import java.util.function.Function;

public class AuthPlusLoader {

    public static Function<AuthPlusConfiguration, AuthPlusCore> loadFun;

    private AuthPlusLoader() {
        throw new AssertionError();
    }

    public static AuthPlusCore load(AuthPlusConfiguration config) {
        assert loadFun != null;
        return loadFun.apply(config);
    }

    public static synchronized void initialize(Function<AuthPlusConfiguration, AuthPlusCore> f) {
        if(loadFun != null) throw new IllegalStateException("Already initialised");
        loadFun = f;
    }
}