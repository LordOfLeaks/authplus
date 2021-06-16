package me.lordofleaks.authplus.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AuthPlusConfigurationTest {

    @TempDir
    Path tempDir;

    @Test
    void testConfigOkSqlite() throws Exception {
        AuthPlusConfiguration cfg = AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-ok-sqlite.yml").toURI()));
        assertArrayEquals(new String[] {"/somecommand", "/someothercommand"}, cfg.getAllowedLoginCommands().toArray(new String[0]));
        assertEquals(4000, cfg.getEncryption().getIterationCount());
        assertEquals(AuthPlusConfiguration.Storage.Type.SQLITE, cfg.getStorage().getType());
        assertEquals("authplus.db", cfg.getStorage().getFile());
        assertNull(cfg.getStorage().getUsername());
        assertNull(cfg.getStorage().getPassword());
        assertNull(cfg.getStorage().getHost());
        assertNull(cfg.getStorage().getPort());
        assertNull(cfg.getStorage().getDatabase());
        assertEquals(4, cfg.getPassword().getPasswordMinimumLength());
        assertTrue(cfg.getPassword().isShouldContainSpecialCharacters());
        assertFalse(cfg.getPassword().isShouldContainUpperAndLowerCase());
        assertFalse(cfg.isDeOpOnJoin());
    }

    @Test
    void testConfigOkMysql() throws Exception {
        AuthPlusConfiguration cfg = AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-ok-mysql.yml").toURI()));
        assertArrayEquals(new String[] {"/somecommand", "/someothercommand"}, cfg.getAllowedLoginCommands().toArray(new String[0]));
        assertEquals(4000, cfg.getEncryption().getIterationCount());
        assertEquals(AuthPlusConfiguration.Storage.Type.MYSQL, cfg.getStorage().getType());
        assertNull(cfg.getStorage().getFile());
        assertEquals("test", cfg.getStorage().getUsername());
        assertEquals("qwerty", cfg.getStorage().getPassword());
        assertEquals("localhost", cfg.getStorage().getHost());
        assertEquals(3333, cfg.getStorage().getPort());
        assertEquals("testdb", cfg.getStorage().getDatabase());
        assertEquals(4, cfg.getPassword().getPasswordMinimumLength());
        assertTrue(cfg.getPassword().isShouldContainSpecialCharacters());
        assertFalse(cfg.getPassword().isShouldContainUpperAndLowerCase());
        assertFalse(cfg.isDeOpOnJoin());
    }

    @Test
    void testConfigOkCommandsEmpty() throws Exception {
        AuthPlusConfiguration cfg = AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-ok-commands-empty.yml").toURI()));
        assertArrayEquals(new String[0], cfg.getAllowedLoginCommands().toArray(new String[0]));
        assertEquals(4000, cfg.getEncryption().getIterationCount());
        assertEquals(AuthPlusConfiguration.Storage.Type.MYSQL, cfg.getStorage().getType());
        assertNull(cfg.getStorage().getFile());
        assertEquals("test", cfg.getStorage().getUsername());
        assertEquals("qwerty", cfg.getStorage().getPassword());
        assertEquals("localhost", cfg.getStorage().getHost());
        assertEquals(3333, cfg.getStorage().getPort());
        assertEquals("testdb", cfg.getStorage().getDatabase());
        assertEquals(4, cfg.getPassword().getPasswordMinimumLength());
        assertTrue(cfg.getPassword().isShouldContainSpecialCharacters());
        assertFalse(cfg.getPassword().isShouldContainUpperAndLowerCase());
        assertFalse(cfg.isDeOpOnJoin());
    }

    @Test
    void testConfigOkMysqlNoPassword() throws Exception {
        AuthPlusConfiguration cfg = AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-ok-mysql-no-password.yml").toURI()));
        assertArrayEquals(new String[] {"/somecommand", "/someothercommand"}, cfg.getAllowedLoginCommands().toArray(new String[0]));
        assertEquals(4000, cfg.getEncryption().getIterationCount());
        assertEquals(AuthPlusConfiguration.Storage.Type.MYSQL, cfg.getStorage().getType());
        assertNull(cfg.getStorage().getFile());
        assertEquals("test", cfg.getStorage().getUsername());
        assertNull(cfg.getStorage().getPassword());
        assertEquals("localhost", cfg.getStorage().getHost());
        assertEquals(3333, cfg.getStorage().getPort());
        assertEquals("testdb", cfg.getStorage().getDatabase());
        assertEquals(4, cfg.getPassword().getPasswordMinimumLength());
        assertTrue(cfg.getPassword().isShouldContainSpecialCharacters());
        assertFalse(cfg.getPassword().isShouldContainUpperAndLowerCase());
        assertFalse(cfg.isDeOpOnJoin());
    }

    @Test
    void testConfigBadIterationCount() {
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-bad-iteration-count.yml").toURI())));
    }

    @Test
    void testConfigBadStorageType() {
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-bad-storage-type.yml").toURI())));
    }

    @Test
    void testConfigBadPasswordMinLength() {
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-bad-password-min-length.yml").toURI())));
    }

    @Test
    void testConfigIncomplete() {
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-incomplete-mysql-database.yml").toURI())));
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-incomplete-mysql-port.yml").toURI())));
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-incomplete-mysql-host.yml").toURI())));
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-incomplete-mysql-username.yml").toURI())));
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-incomplete-commands.yml").toURI())));
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-incomplete-sqlite-file.yml").toURI())));
        assertThrows(AuthPlusConfigException.class, () -> AuthPlusConfiguration.load(Paths.get(getClass().getClassLoader().getResource("config-incomplete-password.yml").toURI())));
    }

    @Test
    void testConfigCreateDefault() {
        Path temp = tempConfig();
        assertFalse(Files.exists(temp));
        AuthPlusConfiguration.load(temp);
        assertTrue(Files.exists(temp));
        AuthPlusConfiguration cfg = AuthPlusConfiguration.load(temp);
        AuthPlusConfiguration def = new AuthPlusConfiguration();
        def.fillDefaults();
        assertEquals(def, cfg);
    }

    private Path tempConfig() {
        return tempDir.resolve("temp" + System.nanoTime() + ".yml");
    }

}