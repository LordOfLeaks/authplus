package me.lordofleaks.authplus.core.hasher;

import java.util.concurrent.CompletableFuture;

public interface PasswordHasher {

    /**
     * Generates random salt to be used by this PasswordHasher.
     *
     * @return Future returning generated salt.
     */
    CompletableFuture<byte[]> generateSalt();

    /**
     * Computes the hash using provided salt and password string.
     *
     * @param salt Salt of the hash.
     * @param password Password string to create hash for.
     * @return Future containing computed hash.
     */
    CompletableFuture<byte[]> computeHash(byte[] salt, String password);

}