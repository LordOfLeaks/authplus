package me.lordofleaks.authplus.core.comm;

import me.lordofleaks.authplus.core.AuthPlusException;

/**
 * Exception thrown in case of failure related to communication.
 */
public class AuthPlusCommunicationException extends AuthPlusException {

    public AuthPlusCommunicationException(String message) {
        super(message);
    }

    public AuthPlusCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}