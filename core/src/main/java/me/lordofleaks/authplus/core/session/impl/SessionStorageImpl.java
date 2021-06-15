package me.lordofleaks.authplus.core.session.impl;

import me.lordofleaks.authplus.core.session.Session;
import me.lordofleaks.authplus.core.session.SessionStorage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStorageImpl implements SessionStorage {

    private final ConcurrentHashMap<UUID, Session> internalStorage = new ConcurrentHashMap<>();

    @Override
    public Session getSessionByAccount(UUID accountId) {
        return internalStorage.get(accountId);
    }

    @Override
    public void insertSession(Session session) {
        internalStorage.put(session.getAccount().getUniqueId(), session);
    }

    @Override
    public void replaceSession(Session session) {
        internalStorage.computeIfPresent(session.getAccount().getUniqueId(), (key, old) -> session);
    }

    @Override
    public void deleteSession(UUID accountId) {
        internalStorage.remove(accountId);
    }
}
