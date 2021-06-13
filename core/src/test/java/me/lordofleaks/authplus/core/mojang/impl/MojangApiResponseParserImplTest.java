package me.lordofleaks.authplus.core.mojang.impl;

import me.lordofleaks.authplus.core.mojang.MojangApiException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MojangApiResponseParserImplTest {

    @Test
    void testResponseNormal() {
        MojangApiResponseParser responseParser = new MojangApiResponseParserImpl();
        assertEquals(
                UUID.fromString("0e5d368b-ce6b-4a25-b570-5d930d74cef2"),
                responseParser.parseUuidResponse("{\"name\":\"test\",\"id\":\"0e5d368bce6b4a25b5705d930d74cef2\"}")
        );
    }

    @Test
    void testResponsePrettyPrinted() {
        MojangApiResponseParser responseParser = new MojangApiResponseParserImpl();
        assertEquals(
                UUID.fromString("0e5d368b-ce6b-4a25-b570-5d930d74cef2"),
                responseParser.parseUuidResponse("{\n  \"name\": \"test\",\n  \"id\":\"0e5d368bce6b4a25b5705d930d74cef2\"\n}")
        );
    }

    @Test
    void testResponseError() {
        MojangApiResponseParser responseParser = new MojangApiResponseParserImpl();
        assertThrows(
                MojangApiException.class,
                () -> responseParser.parseUuidResponse(
                        "{\"error\": \"IllegalArgumentException\",\"errorMessage\":\"Invalid timestamp.\"}"
                )
        );
    }
}