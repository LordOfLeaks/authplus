package me.lordofleaks.authplus.core.account;

import me.lordofleaks.authplus.core.AuthPlusException;

/**
 * Exception class thrown by AccountRepository implementations in case of failure.
 */
public class AccountRepositoryException extends AuthPlusException {

    public AccountRepositoryException(String message) {
        super(message);
    }

    public AccountRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}