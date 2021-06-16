package me.lordofleaks.authplus.core.login.impl;

import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.login.LoginEngine;
import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.session.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PremiumAwareLoginEngineTest {

    @Test
    void testLoginPremiumNotRegistered() {
        AccountRepository repository = mock(AccountRepository.class);
        MojangApi mojangApi = mock(MojangApi.class);
        LoginEngine engine = new PremiumAwareLoginEngine(repository, mojangApi);

        Account expected = new Account(UUID.randomUUID());
        expected.setName("test");
        expected.setRegistered(true);
        expected.setRegisteredPremium(true);

        when(repository.getAccountByName(anyString())).thenReturn(CompletableFuture.completedFuture(null));
        when(repository.getAccountByUniqueId(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(repository.updateAccount(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(mojangApi.getUniqueIdByName(anyString())).thenReturn(CompletableFuture.completedFuture(expected.getUniqueId()));

        Session result = engine.performLogin(expected.getName(), (name) -> null /*unused*/).join();
        assertTrue(result.isAuthorized());
        assertEquals(expected, result.getAccount());

        verify(repository).updateAccount(eq(expected));
    }

    @Test
    void testLoginNoPremiumNotRegistered() {
        AccountRepository repository = mock(AccountRepository.class);
        MojangApi mojangApi = mock(MojangApi.class);
        LoginEngine engine = new PremiumAwareLoginEngine(repository, mojangApi);

        when(repository.getAccountByName(anyString())).thenReturn(CompletableFuture.completedFuture(null));
        when(repository.getAccountByUniqueId(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(repository.updateAccount(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(mojangApi.getUniqueIdByName(anyString())).thenReturn(CompletableFuture.completedFuture(null));

        Account expected = new Account(UUID.randomUUID());
        expected.setName("test");
        expected.setRegistered(false);
        expected.setRegisteredPremium(false);

        Session result = engine.performLogin(expected.getName(), (name) -> expected.getUniqueId()).join();
        assertFalse(result.isAuthorized());
        assertEquals(expected, result.getAccount());
        verify(repository, never()).updateAccount(any());
    }

    @Test
    void testLoginNoPremiumRegistered() {
        AccountRepository repository = mock(AccountRepository.class);
        MojangApi mojangApi = mock(MojangApi.class);
        LoginEngine engine = new PremiumAwareLoginEngine(repository, mojangApi);

        Account expected = new Account(UUID.randomUUID());
        expected.setName("test");
        expected.setRegistered(true);
        expected.setRegisteredPremium(false);

        when(repository.getAccountByName(anyString())).thenReturn(CompletableFuture.completedFuture(expected));
        when(repository.getAccountByUniqueId(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(repository.updateAccount(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(mojangApi.getUniqueIdByName(anyString())).thenReturn(CompletableFuture.completedFuture(null));

        Session result = engine.performLogin(expected.getName(), (name) -> expected.getUniqueId()).join();
        assertFalse(result.isAuthorized());
        assertEquals(expected, result.getAccount());
        verify(repository, never()).updateAccount(any());
    }

    @Test
    void testLoginPremiumRegistered() {
        AccountRepository repository = mock(AccountRepository.class);
        MojangApi mojangApi = mock(MojangApi.class);
        LoginEngine engine = new PremiumAwareLoginEngine(repository, mojangApi);

        Account expected = new Account(UUID.randomUUID());
        expected.setName("test");
        expected.setRegistered(true);
        expected.setRegisteredPremium(true);

        when(repository.getAccountByName(anyString())).thenReturn(CompletableFuture.completedFuture(expected));
        when(repository.getAccountByUniqueId(any())).thenReturn(CompletableFuture.completedFuture(expected));
        when(repository.updateAccount(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(mojangApi.getUniqueIdByName(anyString())).thenReturn(CompletableFuture.completedFuture(null));

        Session result = engine.performLogin(expected.getName(), (name) -> expected.getUniqueId()).join();
        assertTrue(result.isAuthorized());
        assertEquals(expected, result.getAccount());
        verify(repository, never()).updateAccount(any());
        verify(mojangApi, never()).getUniqueIdByName(anyString());
    }

    @Test
    void testLoginPremiumRegisteredChangedName() {
        AccountRepository repository = mock(AccountRepository.class);
        MojangApi mojangApi = mock(MojangApi.class);
        LoginEngine engine = new PremiumAwareLoginEngine(repository, mojangApi);

        Account expected = new Account(UUID.randomUUID());
        expected.setName("test");
        expected.setRegistered(true);
        expected.setRegisteredPremium(true);

        when(repository.getAccountByName(anyString())).thenReturn(CompletableFuture.completedFuture(null));
        when(repository.getAccountByUniqueId(any())).thenReturn(CompletableFuture.completedFuture(expected));
        when(repository.updateAccount(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(mojangApi.getUniqueIdByName(anyString())).thenReturn(CompletableFuture.completedFuture(expected.getUniqueId()));

        Session result = engine.performLogin(expected.getName(), (name) -> expected.getUniqueId()).join();
        assertTrue(result.isAuthorized());
        assertEquals(expected, result.getAccount());
        verify(repository).updateAccount(any());
        verify(mojangApi).getUniqueIdByName(anyString());
    }
}