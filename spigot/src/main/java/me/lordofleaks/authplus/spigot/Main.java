package me.lordofleaks.authplus.spigot;

import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.AuthPlusLoader;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private AuthPlusCore core;

    @Override
    public void onEnable() {
        AuthPlusConfiguration configuration = AuthPlusConfiguration.load(getDataFolder().toPath().resolve("config.yml"));
        core = AuthPlusLoader.load(configuration);
    }

    @Override
    public void onDisable() {
        core.close();
    }
}