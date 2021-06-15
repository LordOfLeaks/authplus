package me.lordofleaks.authplus.core.session;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

public interface SessionStorage {

    /**
     * Gets session by account name from storage.
     * @param accountId Account id of the session.
     * @return Session or {@code null} if not found.
     */
    @Nullable
    Session getSessionByAccount(UUID accountId);

    /**
     * Registers an additional source for retrieving
     * session information e.g. temporary cache.
     *
     * @param hook Hook to register.
     */
    //void registerAdditionalSource(Function<UUID, Session> hook);

    /**
     * Inserts provided session into the storage.
     * If session with the same account name is already present - replaces it with new one.
     *
     * @param session Session to be inserted.
     */
    void insertSession(Session session);

    /**
     * Replaces existing session with the same account name with new one.
     * If session is not present - does nothing.
     *
     * @param session New session.
     */
    void replaceSession(Session session);

    /**
     * Deletes session with given account name from the storage.
     * If there is no session present - does nothing.
     *
     * @param accountId Account id of the session to delete.
     */
    void deleteSession(UUID accountId);

}