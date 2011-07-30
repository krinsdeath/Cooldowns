package net.krinsoft.cooldowns.listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author krinsdeath
 */

public class CommandListener implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("cooldowns")) {
			sender.sendMessage("All's good so far o-o");
			return true;
		}
		return false;
	}
}
