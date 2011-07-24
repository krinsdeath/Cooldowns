package net.krinsoft.cooldowns.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.types.CoolPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */

public class CommandListener implements CommandExecutor {
    private Cooldowns plugin;

    public CommandListener(Cooldowns instance) {
        plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) { return true; }
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("cooldowns")) {
            // make sure of the command that got us here
            if (args.length >= 1) {
                // we need at least one argument for the following
                if (args[0].equalsIgnoreCase("loc") || args[0].equalsIgnoreCase("locale")) {
                    // player is asking about locales
                    Set<String> locs = plugin.locales.keySet();
                    if (locs != null) {
                        if (args.length >= 2) {
                            // the player gave a second argument, let's try to match it to a key
                            if (locs.contains(args[1])) {
                                // success! let's set the player's localization to this
                                plugin.getPlayer(player).setLocale(args[1]);
                                String msg = plugin.getLocale(plugin.getPlayer(player).getLocale()).getString("plugin.locale_changed", "");
                                if (msg == null) { return true; }
                                plugin.getPlayer(player).parse(msg);
                                return true;
                            } else {
                                // no luck, that localization doesn't exist
                                String msg = plugin.getLocale(plugin.getPlayer(player).getLocale()).getString("plugin.locale_not_found", "");
                                if (msg == null) { return true; }
                                plugin.getPlayer(player).parse(msg);
                                return true;
                            }
                        } else {
                            // show the player a list of localization options
                            String msg = "";
                            for (String key : locs) {
                                msg = plugin.getLocale(key).getString("plugin.language", "");
                                plugin.getPlayer(player).parse(key + ": " + msg);
                            }
                        }
                    } else {
                        plugin.getLogger().warn("You have no localizations. Players will get no messages.");
                        return true;
                    }
                } else {
                    // TODO more arguments
                }
            } else {
                // we got no arguments
                List<String> help = plugin.getLocale(plugin.getPlayer((Player)sender).getLocale()).getStringList("plugin.help", new ArrayList<String>());
                CoolPlayer data = plugin.getPlayer((Player)sender);
                for (String line : help) {
                    data.help(cmd.getName(), line);
                }
                return true;
            }
        }
        return true;
    }


}
