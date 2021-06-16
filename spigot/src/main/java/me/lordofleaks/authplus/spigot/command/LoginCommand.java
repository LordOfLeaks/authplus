package me.lordofleaks.authplus.spigot.command;

import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.config.msg.MessageArg;
import me.lordofleaks.authplus.core.session.Session;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class LoginCommand extends SessionAwareCommand {

    private final JavaPlugin plugin;
    private final AuthPlusCore core;

    public LoginCommand(JavaPlugin plugin, AuthPlusCore core) {
        super(core);
        this.plugin = plugin;
        this.core = core;

        this.core.getMessageConfiguration().registerMessage("command-login-already-logged-in",
                "&cYou are already logged in.");
        this.core.getMessageConfiguration().registerMessage("command-login-not-registered",
                "&cYou have to register first.");
        this.core.getMessageConfiguration().registerMessage("command-login-invalid-password",
                "&cInvalid password.");
        this.core.getMessageConfiguration().registerMessage("command-login-success",
                "&aLogged successfully.");
        this.core.getMessageConfiguration().registerMessage("command-login-session-update-failed",
                "&cSession update failed.");
        this.core.getMessageConfiguration().registerMessage("command-login-usage",
                "&cUse: /%enteredCommand% <password>.");
    }

    @Override
    public boolean onCommand(Player sender, Session session, String label, String[] args) {
        if (session.isAuthorized()) {
            sender.sendMessage(core.getMessageConfiguration().getMessage("command-login-already-logged-in"));
            return true;
        }
        if (!session.getAccount().isRegistered()) {
            sender.sendMessage(core.getMessageConfiguration().getMessage("command-login-not-registered"));
            return true;
        }
        if (args.length == 1) {
            String password = args[0];
            final byte[] sessionPassword = session.getAccount().getPassword();
            core.getPasswordHasher().computeHash(session.getAccount().getSalt(), password).thenAccept(enteredPassword -> {
                if (!Arrays.equals(sessionPassword, enteredPassword)) {
                    sender.sendMessage(core.getMessageConfiguration().getMessage("command-login-invalid-password"));
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    //session's fields are not thread-safe - make sure we update them inside the primary thread
                    session.setAuthorized(true);
                    core.getCommunicator().updateSession(session).thenRun(() ->
                            sender.sendMessage(core.getMessageConfiguration().getMessage("command-login-success"))
                    ).exceptionally(ex -> {
                        ex.printStackTrace();
                        sender.sendMessage(core.getMessageConfiguration().getMessage("command-login-session-update-failed"));
                        return null;
                    });
                });
            });
            return true;
        }
        sender.sendMessage(core.getMessageConfiguration().getMessage("command-login-usage",
                MessageArg.of("enteredCommand", label))
        );
        return true;
    }
}