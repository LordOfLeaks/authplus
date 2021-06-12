package me.lordofleaks.authplus.core.mojang;

import me.lordofleaks.authplus.core.AuthPlusException;

public class MojangApiException extends AuthPlusException {

    public MojangApiException(String message) {
        super(message);
    }

    public MojangApiException(String message, Throwable cause) {
        super(message, cause);
    }
}