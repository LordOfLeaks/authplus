package me.lordofleaks.authplus.core.session.impl;

import me.lordofleaks.authplus.core.session.Session;
import me.lordofleaks.authplus.core.session.SessionStorage;
import org.jetbrains.annotations.NotNull;

public class SessionStorageImpl implements SessionStorage {

    @Override
    public Session getSessionByAccount(String accountName) {
        return null;
    }

    @Override
    public @NotNull Session createSession(String accountName) {
        return null;
    }

    @Override
    public void deleteSession(String accountName) {

    }
}
