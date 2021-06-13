package me.lordofleaks.authplus.core.mojang.impl;

import me.lordofleaks.authplus.core.mojang.MojangApiException;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MojangApiResponseParserImpl implements MojangApiResponseParser {

    private static final String ID_PREFIX = "\"id\":\"";
    private static final int ID_LENGTH = 32;

    @Override
    @NotNull
    public UUID parseUuidResponse(String response) {
        response = sanitizeResponse(response);
        String id = null;
        for (int i = 0; i < response.length() - ID_PREFIX.length() - ID_LENGTH; i++) {
            if (ID_PREFIX.equals(response.substring(i, i + ID_PREFIX.length()))) {
                id = response.substring(i + ID_PREFIX.length(), i + ID_PREFIX.length() + ID_LENGTH);
                break;
            }
        }
        if (id == null)
            throw new MojangApiException("Cannot find id in the response: " + response);
        long mostSigBits = Long.parseLong(id.substring(0, 8), 16);
        mostSigBits <<= 16;
        mostSigBits |= Long.parseLong(id.substring(8, 12), 16);
        mostSigBits <<= 16;
        mostSigBits |= Long.parseLong(id.substring(12, 16), 16);

        long leastSigBits = Long.parseLong(id.substring(16, 20), 16);
        leastSigBits <<= 48;
        leastSigBits |= Long.parseLong(id.substring(20, 32), 16);
        return new UUID(mostSigBits, leastSigBits);
    }

    private String sanitizeResponse(String response) {
        StringBuilder sanitizedResponse = new StringBuilder();
        for(int i = 0; i < response.length(); i++) {
            char c = response.charAt(i);
            if(c == '\n' || c == ' ')
                continue;
            sanitizedResponse.append(c);
        }
        return sanitizedResponse.toString();
    }
}
