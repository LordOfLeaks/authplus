package me.lordofleaks.authplus.core;

import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import org.jetbrains.annotations.NotNull;

public interface AuthPlusCoreFactory {

    @NotNull
    AuthPlusCore newCore(AuthPlusConfiguration config);

}