package me.lordofleaks.authplus.core.hasher;

import java.util.concurrent.CompletableFuture;

public interface PasswordHasher {

    CompletableFuture<byte[]> generateSalt();

    CompletableFuture<byte[]> computeHash(byte[] salt, String password);

}