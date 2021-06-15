package me.lordofleaks.authplus.core.comm;

import me.lordofleaks.authplus.core.session.Session;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provides pub-sub communication functionality.
 */
public interface Communicator {

    String CHANNEL_NAME = "authplus:comm";

    /**
     * Method called by external reader to notify this Communicator about incoming data.
     *
     * @param accountId Account id related to incoming data.
     * @param data Incoming data.
     * @return Data read future.
     */
    CompletableFuture<Void> handleRead(UUID accountId, byte[] data);

    /**
     * Initializes the sender used to transport data from this Communicator.
     *
     * @param sender Sender to be used by this Communicator.
     */
    void initializeSender(CommunicationSender sender);

    /**
     * Sends get session request via the sender and returns the response as the future.
     * Note that future may fail for several reasons:
     * - an error occurred during sending,
     * - an error occurred during reading,
     * - response took too long to arrive.
     *
     * Note that the account name should be name of the online player.
     *
     * @param accountId Account id of the session.
     * @return Future containing the response.
     */
    CompletableFuture<Session> getSession(UUID accountId);

    /**
     * Sends update request to the peers updating their local instance of session.
     * This method also requests to update the account in the repository.
     * After the future completes there is no guarantee that update was already performed.
     *
     * Note that the account name in the session should be name of the online player.
     *
     * @param session Updated session.
     * @return Future containing the response.
     */
    CompletableFuture<Void> updateSessionAndAccount(Session session);

    /**
     * Sends update request to the peers updating their local instance of session.
     * After the future completes there is no guarantee that update was already performed.
     *
     * Note that the account name in the session should be name of the online player.
     *
     * @param session Updated session.
     * @return Future containing the response.
     */
    CompletableFuture<Void> updateSession(Session session);

    /**
     * Close all underlying resources used by this Communicator.
     */
    void close();

}