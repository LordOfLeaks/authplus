package me.lordofleaks.authplus.core.impl;

import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.AuthPlusCoreFactory;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class AuthPlusCoreImplFactory implements AuthPlusCoreFactory {

    @Override
    public @NotNull AuthPlusCore newCore(Path workdir, AuthPlusConfiguration config) {
        return new AuthPlusCoreImpl(workdir, config);
    }
}