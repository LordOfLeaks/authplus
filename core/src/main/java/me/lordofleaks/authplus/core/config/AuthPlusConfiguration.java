package me.lordofleaks.authplus.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

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
            must(type != null, "Storage type be defined.");
            switch (type) {
                case SQLITE:
                    must(file != null, "File must be defined for SQLite.");
                    break;
                case MYSQL:
                    must(host != null, "Host must be defined for MySQL.");
                    must(port != null, "Port must be defined for MySQL.");
                    must(username != null, "Username must be defined for MySQL.");
                    must(database != null, "Database must be defined for MySQL.");
                    break;
                default:
                    throw new AuthPlusConfigException("Unknown storage type.");
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

    /**
     * Loads AuthPlusConfiguration or it creates if not exists from given file in YAML format.
     * Then validates loaded configuration.
     *
     * @param path Path to the file.
     * @return Loaded AuthPlusConfiguration.
     */
    @NotNull
    public static AuthPlusConfiguration load(Path path) {
        path = path.toAbsolutePath();
        YAMLFactory factory = new YAMLFactory();
        factory.enable(YAMLGenerator.Feature.SPLIT_LINES);
        factory.enable(YAMLGenerator.Feature.INDENT_ARRAYS);
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        AuthPlusConfiguration cfg;
        if (Files.exists(path)) {
            try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
                cfg = mapper.treeToValue(mapper.readTree(is).get("authplus"), AuthPlusConfiguration.class);
            } catch (Exception e) {
                throw new AuthPlusConfigException("Config load failed.", e);
            }
        } else {
            try {
                cfg = new AuthPlusConfiguration();
                if (path.getParent() != null)
                    Files.createDirectories(path.getParent());
                try (OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                    ObjectNode root = mapper.createObjectNode();
                    root.set("authplus", mapper.valueToTree(cfg));
                    mapper.writeTree(mapper.createGenerator(os), root);
                }
            }  catch (Exception e) {
                throw new AuthPlusConfigException("Config creation failed.", e);
            }
        }
        cfg.validate();
        return cfg;
    }

    private void validate() {
        must(encryption != null, "Encryption section must be defined.");
        encryption.validate();

        must(storage != null, "Storage section must be defined.");
        storage.validate();
    }

    private static void must(boolean b, String failMsg) {
        if (!b) throw new AuthPlusConfigException(failMsg);
    }
}