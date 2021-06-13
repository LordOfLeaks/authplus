package me.lordofleaks.authplus.core.hasher.impl;

import me.lordofleaks.authplus.core.hasher.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherImplTest {

    @Test
    void testPasswordHashDifferentSaltsSamePassword() {
        PasswordHasher hasher = new PasswordHasherImpl(5000);
        final String password = "test123";
        byte[] salt1 = hasher.generateSalt().join();
        byte[] salt2 = hasher.generateSalt().join();
        assertNotEquals(hasher.computeHash(salt1, password).join(), hasher.computeHash(salt2, password).join());
    }

    @Test
    void testPasswordHashSameSaltsDifferentPassword() {
        PasswordHasher hasher = new PasswordHasherImpl(5000);
        final byte[] salt = hasher.generateSalt().join();
        assertNotEquals(hasher.computeHash(salt, "test123").join(), hasher.computeHash(salt, "test321").join());
    }

    @Test
    void testPasswordHashSameSaltsSamePassword() {
        PasswordHasher hasher = new PasswordHasherImpl(5000);
        final byte[] salt = hasher.generateSalt().join();
        final String password = "test123";
        assertNotEquals(hasher.computeHash(salt, password).join(), hasher.computeHash(salt, password).join());
    }
}