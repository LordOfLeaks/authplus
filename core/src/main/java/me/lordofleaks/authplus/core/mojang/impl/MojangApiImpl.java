package me.lordofleaks.authplus.core.mojang.impl;

import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class MojangApiImpl implements MojangApi {

    private final MojangApiResponseParser responseParser;

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
                if(conn.getResponseCode() == 204) //no content
                    return null;

                String response;
                try (InputStream inputStream = conn.getInputStream()) {
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int length; (length = inputStream.read(buffer)) != -1; ) {
                        result.write(buffer, 0, length);
                    }
                    response = new String(result.toByteArray(), StandardCharsets.UTF_8);
                }
                if(conn.getResponseCode() != 200)
                    throw new MojangApiException("Request failed! Response: " + response);
                return responseParser.parseUuidResponse(response);
            } catch (Exception e) {
                throw new MojangApiException("Cannot get unique id by name", e);
            }
        });
    }
}