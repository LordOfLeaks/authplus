package me.lordofleaks.authplus.core;

import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;

import java.util.Iterator;
import java.util.ServiceLoader;

public class AuthPlusLoader {

    private static final ServiceLoader<AuthPlusCoreFactory> coreFactories = ServiceLoader.load(AuthPlusCoreFactory.class);

    private AuthPlusLoader() {
        throw new AssertionError();
    }

    /**
     * Loads instance of AuthPlusCore using Java SPI.
     * @param config Config of loaded AuthPlusCore.
     * @return Loaded AuthPlusCore.
     */
    public static AuthPlusCore load(AuthPlusConfiguration config) {
        Iterator<AuthPlusCoreFactory> factories = coreFactories.iterator();
        if(!factories.hasNext())
            throw new AuthPlusException("Cannot find any core factories.");
        return factories.next().newCore(config);
    }
}