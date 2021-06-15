package me.lordofleaks.authplus.core.login;

import me.lordofleaks.authplus.core.session.Session;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface LoginEngine {

    /**
     * Performs login of the player with given name.
     * It does not insert session to the storage - only creates it.
     *
     * @param name Name of the player to log in.
     * @param nameToUuid Name to uuid function for non premium.
     * @return Future containing logged player session.
     */
    CompletableFuture<Session> performLogin(String name, Function<String, UUID> nameToUuid);

}