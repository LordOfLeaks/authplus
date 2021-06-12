package me.lordofleaks.authplus.core.mojang.impl;

import me.lordofleaks.authplus.core.mojang.MojangApi;
import me.lordofleaks.authplus.core.mojang.MojangApiException;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MojangApiImpl implements MojangApi {

    @Override
    public CompletableFuture<UUID> getUniqueIdByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection conn = (HttpsURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);
                conn.setUseCaches(false);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.connect();

                String response;
                try (InputStream inputStream = conn.getInputStream()) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int length; (length = inputStream.read(buffer)) != -1; ) {
                        result.write(buffer, 0, length);
                    }
                    response = new String(result.toByteArray(), StandardCharsets.UTF_8);
                }
                return fastUuidDecode(response);
            } catch (Exception e) {
                throw new MojangApiException("Cannot get unique id by name", e);
            }
        });
    }

    private static final String ID_PREFIX = "\"id\":\"";
    private static final int ID_LENGTH = 32;

    private static UUID fastUuidDecode(String response) {
        String id = null;
        for (int i = 0; i < response.length() - ID_PREFIX.length() - ID_LENGTH; i++) {
            if (ID_PREFIX.equals(response.substring(i, i + ID_PREFIX.length()))) {
                id = response.substring(i + ID_PREFIX.length(), i + ID_PREFIX.length() + ID_LENGTH);
                break;
            }
        }
        if (id == null)
            throw new IllegalStateException("Cannot find id in the response.");
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
}