package me.lordofleaks.authplus.core.config.msg.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import me.lordofleaks.authplus.core.config.AuthPlusConfigException;
import me.lordofleaks.authplus.core.config.msg.AuthPlusMessageConfiguration;
import me.lordofleaks.authplus.core.config.msg.MessageArg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class AuthPlusMessageConfigurationImpl implements AuthPlusMessageConfiguration {

    private final Map<String, Message> messages = new HashMap<>();

    private static class Message {

        private final String raw;
        private final String colored;

        public Message(String raw) {
            this.raw = raw;
            this.colored = translateColors(raw);
        }

        private static String translateColors(String s) {
            char[] b = s.toCharArray();

            for(int i = 0; i < b.length - 1; ++i) {
                if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                    b[i] = 167;
                    b[i + 1] = Character.toLowerCase(b[i + 1]);
                }
            }
            return new String(b);
        }
    }

    @Override
    public void load(Path path) {
        YAMLFactory factory = new YAMLFactory();
        factory.enable(YAMLGenerator.Feature.SPLIT_LINES);
        factory.enable(YAMLGenerator.Feature.INDENT_ARRAYS);
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        if(Files.exists(path)) {
            Map<String, String> rawMessages;
            try(InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
                rawMessages = mapper.readValue(in, mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));
            } catch (IOException e) {
                throw new AuthPlusConfigException("Cannot open messages file", e);
            }
            for(Map.Entry<String, String> entry : rawMessages.entrySet()) {
                messages.put(entry.getKey(), new Message(entry.getValue()));
            }
        } else {
            Map<String, String> rawMessages = new HashMap<>();
            for(Map.Entry<String, Message> entry : messages.entrySet()) {
                rawMessages.put(entry.getKey(), entry.getValue().raw);
            }
            try(OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                mapper.writeValue(os, rawMessages);
            } catch (IOException e) {
                throw new AuthPlusConfigException("Cannot create default messages file", e);
            }
        }
    }

    @Override
    public void registerMessage(String key, String def) {
        if(!messages.containsKey(key)) {
            messages.put(key, new Message(def));
        }
    }

    @Override
    public String getMessage(String key, MessageArg... params) {
        Message msg = messages.get(key);
        if(msg == null) {
            throw new AuthPlusConfigException("Message with key " + key + " is not registered.");
        }
        String text = msg.colored;
        Map<String, String> paramsMap = new HashMap<>();
        for(MessageArg param : params) {
            paramsMap.put(param.getName(), param.getValue());
        }

        int startIdx = -1;
        StringBuilder res = new StringBuilder();
        StringBuilder varBld = new StringBuilder();
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == '%') {
                if(startIdx == -1) {
                    startIdx = i;
                } else {
                    String var = varBld.toString();
                    varBld.setLength(0);
                    if(paramsMap.containsKey(var)) {
                        res.append(paramsMap.get(var));
                        startIdx = -1;
                    } else {
                        res.append('%');
                        res.append(var);
                        startIdx = i;
                    }
                }
            } else if(startIdx != -1) {
                varBld.append(c);
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }
}