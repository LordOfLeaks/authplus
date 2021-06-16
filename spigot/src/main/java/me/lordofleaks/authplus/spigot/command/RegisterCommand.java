package me.lordofleaks.authplus.spigot.command;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.account.Account;
import me.lordofleaks.authplus.core.config.msg.MessageArg;
import me.lordofleaks.authplus.core.session.Session;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RegisterCommand extends SessionAwareCommand {

    @AllArgsConstructor
    private static class SaltAndHash {

        final byte[] salt;
        final byte[] hash;

    }

    private final JavaPlugin plugin;
    private final AuthPlusCore core;

    public RegisterCommand(JavaPlugin plugin, AuthPlusCore core) {
        super(core);
        this.plugin = plugin;
        this.core = core;
        this.core.getMessageConfiguration().registerMessage("command-register-already-registered",
                "&cYou are already registered.");
        this.core.getMessageConfiguration().registerMessage("command-register-passwords-dont-match",
                "&cPasswords do not match.");
        this.core.getMessageConfiguration().registerMessage("command-register-success",
                "&aRegistered successfully.");
        this.core.getMessageConfiguration().registerMessage("command-register-session-update-failed",
                "&cSession update failed.");
        this.core.getMessageConfiguration().registerMessage("command-register-usage",
                "&cUse: /%enteredCommand% <password> <repeat password>.");
        this.core.getMessageConfiguration().registerMessage("command-register-password-too-short",
                "&cPassword has to have a minimum of 6 characters.");
    }

    @Override
    public boolean onCommand(Player sender, Session session, String label, String[] args) {
        if (session.getAccount().isRegistered()) {
            sender.sendMessage(core.getMessageConfiguration().getMessage("command-register-already-registered"));
            return true;
        }
        if (args.length == 2) {
            String password = args[0];
            if (!password.equals(args[1])) {
                sender.sendMessage(core.getMessageConfiguration().getMessage("command-register-passwords-dont-match"));
                return true;
            }
            if(password.length() < 6) {
                sender.sendMessage(core.getMessageConfiguration().getMessage("command-register-password-too-short"));
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
                    core.getCommunicator().updateSessionAndAccount(session).thenRun(() ->
                        sender.sendMessage(core.getMessageConfiguration().getMessage("command-register-success"))
                    ).exceptionally(e -> {
                        e.printStackTrace();
                        sender.sendMessage(core.getMessageConfiguration().getMessage("command-register-session-update-failed"));
                        return null;
                    });
                });
            });
            return true;
        }
        sender.sendMessage(core.getMessageConfiguration().getMessage("command-register-usage",
                MessageArg.of("enteredCommand", label)));
        return true;
    }
}