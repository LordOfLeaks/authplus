package me.lordofleaks.authplus.core.account;

import me.lordofleaks.authplus.core.AuthPlusException;

public class AccountRepositoryException extends AuthPlusException {

    public AccountRepositoryException(String message) {
        super(message);
    }

    public AccountRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}