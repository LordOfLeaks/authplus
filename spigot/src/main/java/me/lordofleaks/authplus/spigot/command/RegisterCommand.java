package me.lordofleaks.authplus.spigot.command;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.session.Session;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class RegisterCommand implements CommandExecutor {

    @AllArgsConstructor
    private static class SaltAndHash {

        final byte[] salt;
        final byte[] hash;

    }

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
        if (session.getAccount().isRegistered()) {
            sender.sendMessage("You are already registered.");
            return true;
        }
        if (args.length == 2) {
            String password = args[0];
            if (!password.equals(args[1])) {
                sender.sendMessage("Passwords do not match.");
                return true;
            }
            core.getPasswordHasher().generateSalt().thenCompose(salt ->
                    core.getPasswordHasher()
                            .computeHash(salt, password)
                            .thenApply(hash -> new SaltAndHash(salt, hash))
            ).thenAccept(saltAndHash -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    session.getAccount().setSalt(saltAndHash.salt);
                    session.getAccount().setPassword(saltAndHash.hash);
                    session.getAccount().setRegistered(true);
                    session.setAuthorized(true);
                    Account account = new Account(player.getUniqueId());
                    account.setSalt(saltAndHash.salt);
                    account.setPassword(saltAndHash.hash);
                    core.getCommunicator().updateSessionAndAccount(session).thenRun(() ->
                        player.sendMessage("Registered successfully.")
                    ).exceptionally(e -> {
                        e.printStackTrace();
                        player.sendMessage("Session and account update failed");
                        return null;
                    });
                });
            });
            return true;
        }
        sender.sendMessage("/" + label + " <password> <repeat password>");
        return true;
    }
}