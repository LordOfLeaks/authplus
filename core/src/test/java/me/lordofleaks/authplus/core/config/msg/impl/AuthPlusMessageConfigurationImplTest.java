package me.lordofleaks.authplus.core.config.msg.impl;

import me.lordofleaks.authplus.core.config.AuthPlusConfigException;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import me.lordofleaks.authplus.core.config.msg.AuthPlusMessageConfiguration;
import me.lordofleaks.authplus.core.config.msg.MessageArg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AuthPlusMessageConfigurationImplTest {

    @TempDir
    Path tempDir;

    @Test
    void loadNotExistingTest() throws IOException {
        Path filePath = tempDir.resolve("messages.yml");
        assertFalse(Files.exists(filePath));

        AuthPlusMessageConfiguration cfg = new AuthPlusMessageConfigurationImpl();
        cfg.registerMessage("message", "&6Message content");
        cfg.registerMessage("message.second-val", "&8Second message %var% content");
        cfg.load(filePath);

        assertTrue(Files.exists(filePath));
        cfg = new AuthPlusMessageConfigurationImpl();
        cfg.load(filePath);
        assertEquals("§6Message content", cfg.getMessage("message"));
        assertEquals("§8Second message test content", cfg.getMessage("message.second-val", MessageArg.of("var", "test")));

        Files.delete(filePath);
    }

    @Test
    void loadExistingTest() throws URISyntaxException {
        Path filePath = Paths.get(getClass().getClassLoader().getResource("messages.yml").toURI());
        AuthPlusMessageConfiguration cfg = new AuthPlusMessageConfigurationImpl();
        cfg.load(filePath);

        assertEquals("§6Some fun message",
                cfg.getMessage("test-message",
                        MessageArg.of("test", "fun")
                )
        );
        assertEquals("§7§lSome very long and very fun message",
                cfg.getMessage("second-test-message",
                        MessageArg.of("test", "very fun"),
                        MessageArg.of("second", "very long")
                )
        );
    }

    @Test
    void getMessageUnregisteredTest() {
        AuthPlusMessageConfiguration cfg = new AuthPlusMessageConfigurationImpl();
        cfg.registerMessage("first", "ok");

        assertEquals("ok", cfg.getMessage("first"));
        assertThrows(AuthPlusConfigException.class, () -> cfg.getMessage("second"));
    }

    @Test
    void getMessageSubstituteVariablesTest() {
        AuthPlusMessageConfiguration cfg = new AuthPlusMessageConfigurationImpl();
        cfg.registerMessage("first", "Some message with variables %var1% %somevar% %var1% %var2%");
        cfg.registerMessage("second", "it works 100% yes %var1% %somevar% %var1% %var2%");

        assertEquals("Some message with variables value1 Some value value1 second val",
                cfg.getMessage("first",
                        MessageArg.of("var1", "value1"),
                        MessageArg.of("somevar", "Some value"),
                        MessageArg.of("var2", "second val")
                ));
        assertEquals("it works 100% yes value1 Some value value1 second val",
                cfg.getMessage("second",
                        MessageArg.of("var1", "value1"),
                        MessageArg.of("somevar", "Some value"),
                        MessageArg.of("var2", "second val")
                ));
    }

    @Test
    void getMessageColoredTest() {
        AuthPlusMessageConfiguration cfg = new AuthPlusMessageConfigurationImpl();
        cfg.registerMessage("first", "&5SomeColored&c&lMessage");
        cfg.registerMessage("second", "Some &1colored &2message &uwith &wunknown &bcolor &zcodes");

        assertEquals("§5SomeColored§c§lMessage", cfg.getMessage("first"));
        assertEquals("Some §1colored §2message &uwith &wunknown §bcolor &zcodes", cfg.getMessage("second"));
    }
}