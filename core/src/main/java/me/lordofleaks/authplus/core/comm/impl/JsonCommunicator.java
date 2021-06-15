package me.lordofleaks.authplus.core.comm.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.comm.*;
import me.lordofleaks.authplus.core.comm.impl.model.GetSessionRequest;
import me.lordofleaks.authplus.core.comm.impl.model.GetSessionResponse;
import me.lordofleaks.authplus.core.comm.impl.model.UpdateSessionRequest;
import me.lordofleaks.authplus.core.session.Session;

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
    private final AuthPlusCore core;
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

    public JsonCommunicator(AuthPlusCore core, int timeoutSeconds) {
        this.core = core;
        this.timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);
        this.callbackEvictExecutor = Executors.newSingleThreadScheduledExecutor();
        this.callbackEvictExecutor.scheduleAtFixedRate(this::evictSessionCallbacks, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<Void> handleRead(UUID accountId, byte[] data) {
        return CompletableFuture.runAsync(() -> {
            ByteBuffer buf = ByteBuffer.wrap(data);
            int id = buf.getInt();
            byte[] jsonRaw = new byte[buf.remaining()];
            buf.get(jsonRaw);
            String json = new String(jsonRaw, StandardCharsets.UTF_8);
            System.out.println("Received message #" + id + " with data: " + json);
            try {
                switch (id) {
                    case GetSessionRequest.ID: {
                        handleGetSessionRequest(mapper.readValue(json, GetSessionRequest.class));
                        return;
                    }
                    case GetSessionResponse.ID:
                        handleGetSessionResponse(mapper.readValue(json, GetSessionResponse.class));
                        return;
                    case UpdateSessionRequest.ID:
                        handleUpdateSessionRequest(mapper.readValue(json, UpdateSessionRequest.class));
                        return;
                }
            } catch (Exception e) {
                throw new AuthPlusCommunicationException("Read failed", e);
            }
            throw new AuthPlusCommunicationException("Unsupported message id");
        });
    }

    private void handleGetSessionRequest(GetSessionRequest req) throws JsonProcessingException {
        Session session = core.getSessionStorage().getSessionByAccount(req.getAccountId());
        GetSessionResponse response = new GetSessionResponse();
        response.setReqId(req.getReqId());
        response.setSession(session);
        sender.sendAsync(req.getAccountId(), writeOut(GetSessionResponse.ID, mapper.writeValueAsString(response)));
    }

    private void handleGetSessionResponse(GetSessionResponse req) {
        evictSessionCallbacks();
        SessionCallback callback = sessionCallbacks.remove(req.getReqId());
        if (callback != null)
            callback.cb.complete(req.getSession());
    }

    private void handleUpdateSessionRequest(UpdateSessionRequest req) {
        if (req.getSession() == null) {
            throw new IllegalArgumentException("Session must be provided");
        }
        core.getSessionStorage().insertSession(req.getSession());
        if (req.isUpdateAccount()) {
            core.getAccountRepository().updateAccount(req.getSession().getAccount());
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
            try {
                String json = mapper.writeValueAsString(request);
                sender.sendAsync(accountId, writeOut(GetSessionRequest.ID, json)).exceptionally((ex) -> {
                    future.completeExceptionally(ex);
                    return null;
                });
            } catch (Exception e) {
                future.completeExceptionally(new AuthPlusCommunicationException("Get session failed", e));
            }
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
        return CompletableFuture.runAsync(() -> {
            try {
                String json = mapper.writeValueAsString(request);
                sender.sendAsync(session.getAccount().getUniqueId(), writeOut(UpdateSessionRequest.ID, json));
            } catch (Exception e) {
                throw new AuthPlusCommunicationException("Update session failed.", e);
            }
        });
    }

    private byte[] writeOut(int id, String data) {
        System.out.println("Writing: #" + id + " " + data);
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(4 + bytes.length);
        buf.putInt(id);
        buf.put(bytes);
        return buf.array();
    }

    @Override
    public void close() {
        callbackEvictExecutor.shutdown();
    }
}
