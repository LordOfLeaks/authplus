package me.lordofleaks.authplus.core.session;

public interface SessionStorage {

    Session getSessionByAccount(String accountName);

    Session createSession(String accountName);

    void deleteSession(String accountName);

}