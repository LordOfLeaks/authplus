package me.lordofleaks.authplus.core.config;

import java.util.HashMap;
import java.util.Map;

//TODO in progress
public class AuthPlusMessageConfiguration {

    private Map<String, String> messages = new HashMap<>();

    public static class Param {

        private final String name;
        private final String value;

        private Param(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static Param param(String n, String v) {
            return new Param(n, v);
        }
    }

    public void register(String key, String def) {
        if(!messages.containsKey(key)) {
            messages.put(key, def);
        }
    }

    public String getMessage(String key, Param... params) {
        String msg = messages.get(key);
        for(Param p : params) {
            msg = msg.replace("%" + p.name + "%", p.value);
        }
        return msg;
    }
}