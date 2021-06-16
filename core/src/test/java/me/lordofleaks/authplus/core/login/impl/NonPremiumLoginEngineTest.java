package me.lordofleaks.authplus.core.login.impl;

import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.session.Session;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NonPremiumLoginEngineTest {

    @Test
    void testUnregistered() {
        AccountRepository repository = mock(AccountRepository.class);
        LoginEngine engine = new NonPremiumLoginEngine(repository);
        Account expected = new Account(UUID.randomUUID());
        expected.setRegistered(false);
        expected.setRegisteredPremium(false);
        expected.setName("test");

        when(repository.getAccountByName(anyString())).thenReturn(CompletableFuture.completedFuture(null));

        Session session = engine.performLogin(expected.getName(), name -> expected.getUniqueId()).join();
        assertFalse(session.isAuthorized());
        assertEquals(expected, session.getAccount());

        verify(repository, never()).updateAccount(any());
    }

    @Test
    void testRegistered() {
        AccountRepository repository = mock(AccountRepository.class);
        LoginEngine engine = new NonPremiumLoginEngine(repository);
        Account expected = new Account(UUID.randomUUID());
        expected.setRegistered(true);
        expected.setRegisteredPremium(false);
        expected.setName("test");

        when(repository.getAccountByName(anyString())).thenReturn(CompletableFuture.completedFuture(expected));

        Session session = engine.performLogin(expected.getName(), name -> expected.getUniqueId()).join();
        assertFalse(session.isAuthorized());
        assertEquals(expected, session.getAccount());

        verify(repository, never()).updateAccount(any());
    }
}