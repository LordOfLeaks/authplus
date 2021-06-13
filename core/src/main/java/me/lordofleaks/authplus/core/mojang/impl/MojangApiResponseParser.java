package me.lordofleaks.authplus.core.mojang.impl;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MojangApiResponseParser {

    /**
     * Extracts UUID from response received from GET https://api.mojang.com/users/profiles/minecraft/ API.
     *
     * @param response Response.
     * @return Extracted UUID.
     */
    @NotNull
    UUID parseUuidResponse(String response);

}