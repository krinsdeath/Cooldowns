package net.krinsoft.cooldowns.listeners;

import java.util.ArrayList;
import java.util.List;
import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.player.CoolPlayer;
import net.krinsoft.cooldowns.player.PlayerManager;
import net.krinsoft.cooldowns.player.WarmPlayer;
import net.krinsoft.cooldowns.util.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */

public class CommandListener implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("cooldowns")) {
				if (args.length == 0) {
					// show status of cooldowns
					showStatus(player);
					return true;
				} else {
					if (args[0].equalsIgnoreCase("loc") || args[0].equalsIgnoreCase("locale")) {
						// show locale menu
						if (args.length == 1) {
							showLocales(player);
							return true;
						} else if (args.length > 1) {
							if (Cooldowns.getLocale(args[1]) != null) {
								PlayerManager.getCoolPlayer(player.getName()).setLocale(args[1]);
								PlayerManager.getWarmPlayer(player.getName()).setLocale(args[1]);
								String loc = PlayerManager.getCoolPlayer(player.getName()).getLocale().getString("plugin.locale_changed");
								if (loc != null) {
									player.sendMessage(loc);
								}
							} else {
								String loc = PlayerManager.getCoolPlayer(player.getName()).getLocale().getString("plugin.locale_not_found");
								if (loc != null) {
									player.sendMessage(loc);
								}
							}
						}
					} else if (args[0].equalsIgnoreCase("settings") || args[0].equalsIgnoreCase("setup")) {
						showSetup(player);
					} else if (args[0].equalsIgnoreCase("help")) {
						showHelp(player, label);
					}
				}
				return true;
			}
		}
		return false;
	}

	private void showSetup(Player player) {
		List<String> loc = PlayerManager.getWarmPlayer(player.getName()).getLocale().getStringList("plugin.setup", new ArrayList<String>());
		if (loc.isEmpty()) {
			return;
		}
		for (String line : loc) {
			WarmPlayer w = PlayerManager.getWarmPlayer(player.getName());
			CoolPlayer c = PlayerManager.getCoolPlayer(player.getName());
			line = line.replaceAll("<movement>", ""+w.getInterrupt("movement"));
			line = line.replaceAll("<damage>", ""+w.getInterrupt("damage"));
			line = line.replaceAll("<commands>", ""+w.getInterrupt("command"));
			line = line.replaceAll("<global_cool>", ""+c.getGlobal());
			line = line.replaceAll("<global_warm>", ""+w.getGlobal());
			line = Messages.COLOR.matcher(line).replaceAll("\u00A7$1");
			player.sendMessage(line);
		}
	}

	private void showStatus(Player player) {
		CoolPlayer c = PlayerManager.getCoolPlayer(player.getName());
		c.showCooldowns();
	}

	private void showLocales(Player player) {
		List<String> loc = Cooldowns.getConfig().getStringList("plugin.available_locales", new ArrayList<String>());
		if (loc.isEmpty()) {
			return;
		}
		String l = "";
		for (String line : loc) {
			l = "&a" + line + "&f: " + Cooldowns.getLocale(line).getString("plugin.language");
			l = Messages.COLOR.matcher(l).replaceAll("\u00A7$1");
			player.sendMessage(l);
		}
	}

	private void showHelp(Player player, String label) {
		List<String> loc = PlayerManager.getCoolPlayer(player.getName()).getLocale().getStringList("plugin.help", new ArrayList<String>());
		if (loc.isEmpty()) {
			return;
		}
		for (String line : loc) {
			line = line.replaceAll("(<cmd>|<command>)", label);
			line = Messages.COLOR.matcher(line).replaceAll("\u00A7$1");
			player.sendMessage(line);
		}
	}
}
