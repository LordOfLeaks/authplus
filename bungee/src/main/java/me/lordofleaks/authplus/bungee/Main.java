package me.lordofleaks.authplus.bungee;

import me.lordofleaks.authplus.bungee.listener.AuthPlusPluginMessageListener;
import me.lordofleaks.authplus.bungee.listener.LoginListener;
import me.lordofleaks.authplus.bungee.listener.PreAuthListener;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.AuthPlusLoader;
import me.lordofleaks.authplus.core.comm.Communicator;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class Main extends Plugin {

    private AuthPlusCore core;

    @Override
    public void onEnable() {
        AuthPlusConfiguration configuration = AuthPlusConfiguration.load(getDataFolder().toPath().resolve("config.yml"));
        core = AuthPlusLoader.load(getDataFolder().toPath(), configuration, true);

        getProxy().registerChannel(Communicator.CHANNEL_NAME);
        getProxy().getPluginManager().registerListener(this, new LoginListener(this, core));
        getProxy().getPluginManager().registerListener(this, new PreAuthListener(core));
        initCommunicator(core);
        core.getMessageConfiguration().load(getDataFolder().toPath().resolve("messages.yml"));
    }

    private void initCommunicator(AuthPlusCore core) {
        //establish proxy<->server pubsub tunnel
        getProxy().getPluginManager().registerListener(this, new AuthPlusPluginMessageListener(core));
        core.getCommunicator().initializeSender((uuid, data) -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            player.getServer().sendData(Communicator.CHANNEL_NAME, data);
            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public void onDisable() {
        getProxy().unregisterChannel(Communicator.CHANNEL_NAME);
        core.close();
    }
}