package me.lordofleaks.authplus.core.login;

/**
 * Exception thrown by {@link LoginEngine} implementations in case of failure.
 */
public class AuthPlusLoginException extends RuntimeException {

    public AuthPlusLoginException(String message) {
        super(message);
    }

    public AuthPlusLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}