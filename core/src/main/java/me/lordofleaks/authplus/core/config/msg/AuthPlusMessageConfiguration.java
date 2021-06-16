package me.lordofleaks.authplus.core.config.msg;

import java.nio.file.Path;

public interface AuthPlusMessageConfiguration {

    /**
     * Loads messages from file.
     * If the file does not exist - it creates new one.
     *
     * @param path Path to the messages file.
     */
    void load(Path path);

    /**
     * Gets the colored message and substitutes its arguments with those passed.
     *
     * @param msg Message key.
     * @param args Arguments of the message.
     * @return Colored and substituted message.
     */
    String getMessage(String msg, MessageArg... args);

    /**
     * Registers new message with default value.
     *
     * @param msg Message key.
     * @param def Default value.
     */
    void registerMessage(String msg, String def);

}