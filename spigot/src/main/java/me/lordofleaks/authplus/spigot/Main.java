package me.lordofleaks.authplus.spigot;

import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.AuthPlusLoader;
import me.lordofleaks.authplus.core.comm.AuthPlusCommunicationException;
import me.lordofleaks.authplus.core.comm.Communicator;
import me.lordofleaks.authplus.core.config.AuthPlusConfiguration;
import me.lordofleaks.authplus.spigot.command.LoginCommand;
import me.lordofleaks.authplus.spigot.command.RegisterCommand;
import me.lordofleaks.authplus.spigot.listener.AuthPlusPluginMessageListener;
import me.lordofleaks.authplus.spigot.listener.LoginListener;
import me.lordofleaks.authplus.spigot.listener.PreAuthListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import java.util.concurrent.CompletableFuture;

public class Main extends JavaPlugin {

    private AuthPlusCore core;

    @Override
    public void onEnable() {
        AuthPlusConfiguration configuration = AuthPlusConfiguration.load(getDataFolder().toPath().resolve("config.yml"));
        core = AuthPlusLoader.load(getDataFolder().toPath(), configuration);

        getCommand("login").setExecutor(new LoginCommand(this, core));
        getCommand("register").setExecutor(new RegisterCommand(this, core));

        getServer().getPluginManager().registerEvents(new LoginListener(this, core, !SpigotConfig.bungee), this);
        getServer().getPluginManager().registerEvents(new PreAuthListener(core), this);
        setupCommunicator(core);
    }

    private void setupCommunicator(AuthPlusCore core) {
        if(SpigotConfig.bungee) {
            //establish plugin message pubsub tunnel: server<->proxy
            getServer().getMessenger().registerIncomingPluginChannel(this, Communicator.CHANNEL_NAME, new AuthPlusPluginMessageListener(core.getCommunicator()));
            getServer().getMessenger().registerOutgoingPluginChannel(this, Communicator.CHANNEL_NAME);
            core.getCommunicator().initializeSender((accountId, data) -> {
                CompletableFuture<Void> future = new CompletableFuture<>();
                Bukkit.getScheduler().runTask(Main.this, () -> {
                    Player player = getServer().getPlayer(accountId);
                    if (player == null) {
                        future.completeExceptionally(new AuthPlusCommunicationException("Player with given UUID is offline"));
                    } else {
                        player.sendPluginMessage(Main.this, Communicator.CHANNEL_NAME, data);
                        future.complete(null);
                    }

                });
                return future;
            });
        } else {
            //establish local connection - all requests will be handled by this server
            core.getCommunicator().initializeSender((accountId, data) -> {
                core.getCommunicator().handleRead(accountId, data);
                return CompletableFuture.completedFuture(null);
            });
        }
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        core.close();
    }
}