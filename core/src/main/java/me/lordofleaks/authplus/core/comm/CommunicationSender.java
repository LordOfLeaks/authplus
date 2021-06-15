package me.lordofleaks.authplus.core.comm;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CommunicationSender {

    CompletableFuture<Void> sendAsync(UUID accountId, byte[] data);

}