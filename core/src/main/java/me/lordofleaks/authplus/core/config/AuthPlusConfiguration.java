package me.lordofleaks.authplus.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthPlusConfiguration {

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Encryption {

        /**
         * Iteration count used to create PBEKey.
         */
        @JsonProperty("iteration-count")
        private int iterationCount = 5000;

        private void validate() {
            must(iterationCount > 0, "Iteration count must be positive.");
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Storage {

        public enum Type {
            SQLITE,
            MYSQL
        }

        private Type type = Type.SQLITE;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String file = "authplus.db";

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String host;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Integer port;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String username;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String password;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String database;

        private void validate() {
            must(type != null, "Storage type be defined");
            if(type == Type.SQLITE) {
                must(file != null, "File must be defined for SQLite");
            } else if(type == Type.MYSQL) {
                must(host != null, "Host must be defined for MySQL");
                must(port != null, "Port must be defined for MySQL");
                must(username != null, "Username must be defined for MySQL");
                must(database != null, "Database must be defined for MySQL");
            }
        }
    }

    private Encryption encryption = new Encryption();
    private Storage storage = new Storage();

    @JsonProperty("bungee-cord")
    private boolean bungeeCord = false;

    @JsonProperty("deop-on-join")
    private boolean deOpOnJoin = false;

    @JsonProperty("allowed-login-commands")
    private List<String> allowedLoginCommands = Arrays.asList("/somecommand", "/someothercommand");

    public static AuthPlusConfiguration load(Path path) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        factory.enable(YAMLGenerator.Feature.SPLIT_LINES);
        factory.enable(YAMLGenerator.Feature.INDENT_ARRAYS);
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        AuthPlusConfiguration cfg;
        if (Files.exists(path)) {
            try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
                cfg = mapper.readValue(is, AuthPlusConfiguration.class);
            }
        } else {
            cfg = new AuthPlusConfiguration();
            Files.createDirectories(path.getParent());
            try (OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                mapper.writeValue(os, cfg);
            }
        }
        cfg.validate();
        return cfg;
    }

    private void validate() {
        must(encryption != null, "Encryption section must be defined");
        encryption.validate();

        must(storage != null, "Storage section must be defined");
        storage.validate();
    }

    private static void must(boolean b, String failMsg) {
        if (!b) throw new IllegalStateException(failMsg);
    }
}