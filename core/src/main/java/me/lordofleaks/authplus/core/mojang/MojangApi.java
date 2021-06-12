package me.lordofleaks.authplus.core.mojang;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MojangApi {

    /**
     * Returns unique id of the account using given name.
     *
     * @param name Name to check.
     * @return Unique id associated to given name.
     */
    CompletableFuture<UUID> getUniqueIdByName(String name);

}