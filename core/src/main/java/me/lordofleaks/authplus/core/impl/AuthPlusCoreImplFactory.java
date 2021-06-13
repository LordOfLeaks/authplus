package me.lordofleaks.authplus.core.impl;

import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.AuthPlusCoreFactory;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import org.jetbrains.annotations.NotNull;

public class AuthPlusCoreImplFactory implements AuthPlusCoreFactory {

    @Override
    public @NotNull AuthPlusCore newCore(AuthPlusConfiguration config) {
        return new AuthPlusCoreImpl(config);
    }
}