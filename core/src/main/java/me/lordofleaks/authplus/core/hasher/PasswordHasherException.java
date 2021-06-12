package me.lordofleaks.authplus.core.hasher;

import me.lordofleaks.authplus.core.AuthPlusException;

public class PasswordHasherException extends AuthPlusException {

    public PasswordHasherException(String message) {
        super(message);
    }

    public PasswordHasherException(String message, Throwable cause) {
        super(message, cause);
    }
}