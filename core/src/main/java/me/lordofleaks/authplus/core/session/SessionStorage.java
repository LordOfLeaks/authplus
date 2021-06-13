package me.lordofleaks.authplus.core.session;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SessionStorage {

    /**
     * Gets session by account name from storage.
     * @param accountName Account name of the session.
     * @return Session or {@code null} if not found.
     */
    @Nullable
    Session getSessionByAccount(String accountName);

    /**
     * Creates new session with given account name
     * and returns its instance.
     *
     * @param accountName Account name of created session.
     * @return Created session.
     */
    @NotNull
    Session createSession(String accountName);

    /**
     * Deletes session with given account name from the storage.
     * If there is no session present - does nothing.
     *
     * @param accountName Account name of the session to delete.
     */
    void deleteSession(String accountName);

}