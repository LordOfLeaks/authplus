package me.lordofleaks.authplus.spigot.command;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.session.Session;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

@RequiredArgsConstructor
public class LoginCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final AuthPlusCore core;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be executed by a player");
            return false;
        }
        Player player = (Player) sender;
        Session session = core.getSessionStorage().getSessionByAccount(player.getUniqueId());
        if (session == null) {
            sender.sendMessage("Hang on! We are fetching your login data...");
            return true;
        }
        if (session.isAuthorized()) {
            sender.sendMessage("You are already logged in.");
            return true;
        }
        if (!session.getAccount().isRegistered()) {
            sender.sendMessage("You are not registered.");
            return true;
        }
        if (args.length == 1) {
            String password = args[0];
            final byte[] sessionPassword = session.getAccount().getPassword();
            core.getPasswordHasher().computeHash(session.getAccount().getSalt(), password).thenAccept(enteredPassword -> {
                if (!Arrays.equals(sessionPassword, enteredPassword)) {
                    sender.sendMessage("Invalid password");
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    //session's fields are not thread-safe - make sure we update them inside the primary thread
                    session.setAuthorized(true);
                    core.getCommunicator().updateSession(session).thenRun(() ->
                        player.sendMessage("Logged successfully.")
                    ).exceptionally(ex -> {
                        ex.printStackTrace();
                        player.sendMessage("Session update failed.");
                        return null;
                    });
                });
            });
            return true;
        }
        sender.sendMessage("/" + label + " <password>");
        return true;
    }
}