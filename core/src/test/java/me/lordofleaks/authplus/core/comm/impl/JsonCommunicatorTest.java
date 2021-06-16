package me.lordofleaks.authplus.core.comm.impl;

import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.comm.AuthPlusCommunicationException;
import me.lordofleaks.authplus.core.comm.Communicator;
import me.lordofleaks.authplus.core.session.Session;
import me.lordofleaks.authplus.core.session.SessionStorage;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JsonCommunicatorTest {

    @Test
    void testCommunicatorSetSenderTwice() {
        SessionStorage sessionStorage = mock(SessionStorage.class);
        AccountRepository repo = mock(AccountRepository.class);
        Communicator comm = new JsonCommunicator(sessionStorage, repo, 1);
        comm.initializeSender((id, data) -> CompletableFuture.completedFuture(null));
        assertThrows(AuthPlusCommunicationException.class, () -> comm.initializeSender((id, data) -> CompletableFuture.completedFuture(null)));
        comm.close();
    }

    @Test
    void testCommunicatorSendError() {
        SessionStorage sessionStorage = mock(SessionStorage.class);
        AccountRepository repo = mock(AccountRepository.class);
        Communicator comm = new JsonCommunicator(sessionStorage, repo, 1);
        comm.initializeSender((id, data) -> {
            throw new RuntimeException("error");
        });

        assertThrows(CompletionException.class, () -> comm.getSession(UUID.randomUUID()).join());
        comm.close();
    }

    @Test
    void testCommunicatorUpdateSession() {
        SessionStorage storage = mock(SessionStorage.class);
        AccountRepository repo = mock(AccountRepository.class);

        Session testSession = new Session(new Account(UUID.fromString("7e97798a-d700-4928-a510-0608b4c2e7cd")));
        Session otherSession = new Session(new Account(UUID.fromString("9cd125ba-3277-48e9-8ddd-6936fa0b4b81")));

        when(repo.updateAccount(any())).thenReturn(CompletableFuture.completedFuture(null));

        Communicator client = new JsonCommunicator(storage, repo, 1);
        Communicator server = new JsonCommunicator(storage, repo, 1);
        server.initializeSender(client::handleRead);
        client.initializeSender(server::handleRead);

        client.updateSession(testSession).join();
        verify(storage).replaceSession(eq(testSession));
        client.updateSessionAndAccount(otherSession).join();
        verify(storage).replaceSession(eq(otherSession));
        verify(repo).updateAccount(eq(otherSession.getAccount()));

        client.close();
        server.close();
    }

    @Test
    void testCommunicatorGetSession() {
        SessionStorage storage = mock(SessionStorage.class);
        AccountRepository repo = mock(AccountRepository.class);

        Session testSession = new Session(new Account(UUID.fromString("7e97798a-d700-4928-a510-0608b4c2e7cd")));
        Session otherSession = new Session(new Account(UUID.fromString("9cd125ba-3277-48e9-8ddd-6936fa0b4b81")));

        when(storage.getSessionByAccount(any(UUID.class))).thenReturn(null);
        when(storage.getSessionByAccount(eq(UUID.fromString("7e97798a-d700-4928-a510-0608b4c2e7cd")))).thenReturn(testSession);
        when(storage.getSessionByAccount(eq(UUID.fromString("9cd125ba-3277-48e9-8ddd-6936fa0b4b81")))).thenReturn(otherSession);

        Communicator client = new JsonCommunicator(storage, repo, 1);
        Communicator server = new JsonCommunicator(storage, repo, 1);
        server.initializeSender(client::handleRead);
        client.initializeSender(server::handleRead);

        assertEquals(testSession, client.getSession(UUID.fromString("7e97798a-d700-4928-a510-0608b4c2e7cd")).join());
        assertEquals(otherSession, client.getSession(UUID.fromString("9cd125ba-3277-48e9-8ddd-6936fa0b4b81")).join());
        assertNull(client.getSession(UUID.fromString("85d33d82-d8d8-437c-9f81-99e73725889d")).join());
        client.close();
        server.close();
    }

    @Test
    void testCommunicatorTimeoutOnReceive() {
        SessionStorage storage = mock(SessionStorage.class);
        AccountRepository repository = mock(AccountRepository.class);

        when(storage.getSessionByAccount(any(UUID.class))).thenReturn(null);

        Communicator client = new JsonCommunicator(storage, repository, 0);
        Communicator server = new JsonCommunicator(storage, repository, 1);
        server.initializeSender(client::handleRead);
        client.initializeSender(server::handleRead);

        assertThrows(CompletionException.class, () -> client.getSession(UUID.randomUUID()).join());
        client.close();
        server.close();
    }

    @Test
    void testCommunicatorTimeoutNeverReceive() {
        SessionStorage storage = mock(SessionStorage.class);
        AccountRepository repository = mock(AccountRepository.class);

        Communicator client = new JsonCommunicator(storage, repository, 0);
        client.initializeSender((acc, data) -> CompletableFuture.completedFuture(null));

        assertThrows(CompletionException.class, () -> client.getSession(UUID.randomUUID()).join());
        client.close();
    }
}