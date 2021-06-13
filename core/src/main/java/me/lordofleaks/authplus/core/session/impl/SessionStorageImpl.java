package me.lordofleaks.authplus.core.session.impl;

import me.lordofleaks.authplus.core.session.Session;
import me.lordofleaks.authplus.core.session.SessionStorage;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class SessionStorageImpl implements SessionStorage {

    private final ConcurrentHashMap<String, Session> internalStorage = new ConcurrentHashMap<>();

    @Override
    public Session getSessionByAccount(String accountName) {
        return internalStorage.get(accountName);
    }

    @Override
    public @NotNull Session createSession(String accountName) {
        return internalStorage.compute(accountName, (key, value) -> new Session(accountName));
    }

    @Override
    public void deleteSession(String accountName) {
        internalStorage.remove(accountName);
    }
}
