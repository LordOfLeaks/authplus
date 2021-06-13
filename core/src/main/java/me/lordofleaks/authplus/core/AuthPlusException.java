package me.lordofleaks.authplus.core;

/**
 * Parent exception class to all exceptions thrown by AuthPlus.
 */
public class AuthPlusException extends RuntimeException{

    public AuthPlusException(String message) {
        super(message);
    }

    public AuthPlusException(String message, Throwable cause) {
        super(message, cause);
    }
}