package me.lordofleaks.authplus.core;

public class AuthPlusException extends RuntimeException{

    public AuthPlusException(String message) {
        super(message);
    }

    public AuthPlusException(String message, Throwable cause) {
        super(message, cause);
    }
}