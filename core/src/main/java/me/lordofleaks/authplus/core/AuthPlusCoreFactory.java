package me.lordofleaks.authplus.core;

import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface AuthPlusCoreFactory {

    @NotNull
    AuthPlusCore newCore(Path workdir, AuthPlusConfiguration config);

}