package me.lordofleaks.authplus.core.hasher.impl;

import me.lordofleaks.authplus.core.hasher.PasswordHasher;
import me.lordofleaks.authplus.core.hasher.PasswordHasherException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

public class PasswordHasherImpl implements PasswordHasher {

    private final int iterationCount;

    public PasswordHasherImpl(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    @Override
    public CompletableFuture<byte[]> generateSalt() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SecureRandom sr = new SecureRandom();
                byte[] salt = new byte[16];
                sr.nextBytes(salt);
                return salt;
            } catch (Exception e) {
                throw new PasswordHasherException("Cannot generate salt.", e);
            }
        });
    }

    @Override
    public CompletableFuture<byte[]> computeHash(byte[] salt, String input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                char[] chars = input.toCharArray();

                PBEKeySpec spec = new PBEKeySpec(chars, salt, iterationCount, 512);
                SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
                byte[] res = skf.generateSecret(spec).getEncoded();
                spec.clearPassword();
                return res;
            } catch (Exception e) {
                throw new PasswordHasherException("Cannot compute hash.", e);
            }
        });
    }
}