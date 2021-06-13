package me.lordofleaks.authplus.core.config;

import me.lordofleaks.authplus.core.AuthPlusException;

/**
 * Exception class thrown in relation to AuthPlusConfiguration.
 */
public class AuthPlusConfigException extends AuthPlusException {

    public AuthPlusConfigException(String message) {
        super(message);
    }

    public AuthPlusConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}