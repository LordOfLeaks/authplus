package me.lordofleaks.authplus.core.comm.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.account.AccountRepository;
import me.lordofleaks.authplus.core.comm.AuthPlusCommunicationException;
import me.lordofleaks.authplus.core.comm.CommunicationSender;
import me.lordofleaks.authplus.core.comm.Communicator;
import me.lordofleaks.authplus.core.comm.impl.model.GetSessionRequest;
import me.lordofleaks.authplus.core.comm.impl.model.GetSessionResponse;
import me.lordofleaks.authplus.core.comm.impl.model.UpdateSessionRequest;
import me.lordofleaks.authplus.core.session.Session;
import me.lordofleaks.authplus.core.session.SessionStorage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Communicator implementation that serializes the messages using JSON.
 * To properly receive messages they have to be sent by the same implementation.
 */
public class JsonCommunicator implements Communicator {

    private static final AtomicLong requestNextId = new AtomicLong();
    private final ScheduledExecutorService callbackEvictExecutor;
    private final ConcurrentHashMap<String, SessionCallback> sessionCallbacks = new ConcurrentHashMap<>();
    private final SessionStorage sessionStorage;
    private final AccountRepository accountRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final long timeoutMillis;
    private CommunicationSender sender;

    private static class SessionCallback {

        private final CompletableFuture<Session> cb;
        private final long expireTime;

        public SessionCallback(CompletableFuture<Session> cb, long timeoutMillis) {
            this.cb = cb;
            this.expireTime = System.currentTimeMillis() + timeoutMillis;
        }
    }

    @RequiredArgsConstructor
    private static class IdAndJson {

        private final int id;
        private final String json;

    }

    public JsonCommunicator(SessionStorage sessionStorage, AccountRepository accountRepository, int timeoutSeconds) {
        this.sessionStorage = sessionStorage;
        this.accountRepository = accountRepository;
        this.timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);
        this.callbackEvictExecutor = Executors.newSingleThreadScheduledExecutor();
        this.callbackEvictExecutor.scheduleAtFixedRate(this::evictSessionCallbacks, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<Void> handleRead(UUID accountId, byte[] data) {
        return CompletableFuture.supplyAsync(() -> {
            ByteBuffer buf = ByteBuffer.wrap(data);
            int id = buf.getInt();
            byte[] jsonRaw = new byte[buf.remaining()];
            buf.get(jsonRaw);
            String json = new String(jsonRaw, StandardCharsets.UTF_8);
            System.out.println("Received message #" + id + " with data: " + json);
            return new IdAndJson(id, json);
        }).thenCompose(idAndJson -> {
            int id = idAndJson.id;
            String json = idAndJson.json;
            try {
                switch (id) {
                    case GetSessionRequest.ID:
                        return handleGetSessionRequest(mapper.readValue(json, GetSessionRequest.class));
                    case GetSessionResponse.ID:
                        return handleGetSessionResponse(mapper.readValue(json, GetSessionResponse.class));
                    case UpdateSessionRequest.ID:
                        return handleUpdateSessionRequest(mapper.readValue(json, UpdateSessionRequest.class));
                }
            } catch (Exception e) {
                throw new AuthPlusCommunicationException("Read failed", e);
            }
            throw new AuthPlusCommunicationException("Unsupported message id");
        });
    }

    private CompletableFuture<Void> handleGetSessionRequest(GetSessionRequest req) {
        Session session = sessionStorage.getSessionByAccount(req.getAccountId());
        GetSessionResponse response = new GetSessionResponse();
        response.setReqId(req.getReqId());
        response.setSession(session);
        return writeOut(req.getAccountId(), GetSessionResponse.ID, response);
    }

    private CompletableFuture<Void> handleGetSessionResponse(GetSessionResponse req) {
        evictSessionCallbacks();
        SessionCallback callback = sessionCallbacks.remove(req.getReqId());
        if (callback != null)
            callback.cb.complete(req.getSession());
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handleUpdateSessionRequest(UpdateSessionRequest req) {
        if (req.getSession() == null) {
            throw new AuthPlusCommunicationException("Session must be provided");
        }
        sessionStorage.replaceSession(req.getSession());
        if (req.isUpdateAccount()) {
            if (accountRepository == null) {
                throw new AuthPlusCommunicationException("Account repository not initialized");
            }
            return accountRepository.updateAccount(req.getSession().getAccount());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void initializeSender(CommunicationSender sender) {
        if (this.sender != null)
            throw new AuthPlusCommunicationException("Sender already initialized");
        this.sender = sender;
    }

    @Override
    public CompletableFuture<Session> getSession(UUID accountId) {
        CompletableFuture<Session> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            GetSessionRequest request = new GetSessionRequest();
            request.setReqId(generateRequestId(accountId));
            request.setAccountId(accountId);
            evictSessionCallbacks();
            sessionCallbacks.put(request.getReqId(), new SessionCallback(future, timeoutMillis));
            writeOut(accountId, GetSessionRequest.ID, request).exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        });
        return future;
    }

    /**
     * Generates new request id with astronomically low chance of repeating even across multiple servers.
     *
     * @param accountId Id of the account to generate request id for.
     * @return Generated request id.
     */
    private String generateRequestId(UUID accountId) {
        return requestNextId.incrementAndGet() + "-" + accountId.getLeastSignificantBits() + "-" + accountId.getLeastSignificantBits() + "-" + System.nanoTime();
    }

    private void evictSessionCallbacks() {
        final long now = System.currentTimeMillis();
        Iterator<SessionCallback> cb = sessionCallbacks.values().iterator();
        while (cb.hasNext()) {
            SessionCallback next = cb.next();
            if (now >= next.expireTime) {
                cb.remove();
                next.cb.completeExceptionally(new AuthPlusCommunicationException("Timed out."));
            }
        }
    }

    @Override
    public CompletableFuture<Void> updateSessionAndAccount(Session session) {
        return updateSession(session, true);
    }

    @Override
    public CompletableFuture<Void> updateSession(Session session) {
        return updateSession(session, false);
    }

    private CompletableFuture<Void> updateSession(Session session, boolean updateAccount) {
        UpdateSessionRequest request = new UpdateSessionRequest();
        request.setSession(session);
        request.setUpdateAccount(updateAccount);
        return writeOut(session.getAccount().getUniqueId(), UpdateSessionRequest.ID, request);
    }

    private CompletableFuture<Void> writeOut(UUID uuid, int id, Object obj) {
        return CompletableFuture.supplyAsync(() -> {
            String data;
            try {
                data = mapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new AuthPlusCommunicationException("Cannot serialize output", e);
            }
            System.out.println("Writing: #" + id + " " + data);
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buf = ByteBuffer.allocate(4 + bytes.length);
            buf.putInt(id);
            buf.put(bytes);
            return buf.array();
        }).thenCompose(res -> sender.sendAsync(uuid, res));
    }

    @Override
    public void close() {
        callbackEvictExecutor.shutdown();
    }
}
