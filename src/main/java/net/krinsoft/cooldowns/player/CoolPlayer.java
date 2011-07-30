package net.krinsoft.cooldowns.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.interfaces.ICommand;
import net.krinsoft.cooldowns.interfaces.IPlayer;
import net.krinsoft.cooldowns.util.Messages;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */

public class CoolPlayer implements Serializable, IPlayer {
	// version ID
	private final static long serialVersionUID = 11932L;
	protected static List<String> coolers = new ArrayList<String>();

	private class CoolCommand implements ICommand {
		private String command;
		private String label;
		private String flag;
		private long cooldown;

		public CoolCommand(String lb, String fl, long cd, String cmd) {
			this.command = cmd;
			this.label = lb;
			this.flag = fl;
			this.cooldown = (cd * 1000) + System.currentTimeMillis();
		}

		@Override
		public String getHandle() {
			return ("/" + label + " " + flag).trim();
		}

		@Override
		public boolean getStatus() {
			if (cooldown <= System.currentTimeMillis()) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String getCommand() {
			return command;
		}

		@Override
		public String toString() {
			return "CoolCommand{handle=" + getHandle() + "}";
		}

	}

	// -------- //
	// INSTANCE //
	// -------- //

	/**
	 * The player's name
	 */
	private String name;
	/**
	 * The player's group
	 */
	private String group;
	/**
	 * The player's localization setting
	 */
	private String locale;
	/**
	 * A list of this player's currently cooling commands
	 */
	private List<CoolCommand> commands = new ArrayList<CoolCommand>();

	private boolean global;
	private boolean cooling;

	/**
	 * Constructs a new instance of CoolPlayer
	 * @param player
	 * A string representing the player's name
	 */
	public CoolPlayer(String player) {
		name = player;
		group = Cooldowns.getGroup(player);
		global = Cooldowns.getGlobal(group, "cooldowns");
		locale = "en_US";
		System.out.println("CoolPlayer{name=" + name + ",group=" + group + "} created.");
	}

	/**
	 * Iterates through this player's active commands, and tries to cool them down
	 */
	public void update() {
		String loc = Cooldowns.getLocale(locale).getString("command.ready", "'<cmd>' is ready.");
		String msg = "";
		for (CoolCommand cc : commands) {
			if (cc.getStatus()) {
				msg = loc;
				msg = Messages.COMMAND.matcher(msg).replaceAll(cc.getHandle());
				msg = Messages.LABEL.matcher(msg).replaceAll(cc.label);
				msg = Messages.FLAG.matcher(msg).replaceAll(cc.flag);
				msg = Messages.COLOR.matcher(msg).replaceAll("\u00A7$1");
				Cooldowns.getPlayer(name).sendMessage(msg);
				commands.remove(cc);
			} else {
				continue;
			}
		}
		if (commands.size() < 1) {
			coolers.remove(name);
		}
	}

	/**
	 * Attempts to begin cooling down a command
	 * @param msg
	 * A string containing the command to be cooled
	 * @return
	 * true if the command is scheduled, false if it isn't
	 */
	public boolean addCommand(String msg) {
		String label = msg.split(" ")[0].substring(1), flag = "";
		if (msg.split(" ").length > 1) {
			flag = msg.split(" ")[1];
		}
		String key = getCommandKey(label, flag);
		if (key == null) { return false; }
		ConfigurationNode node = Cooldowns.getCommandNode(group, "cooldown");
		int cool = node.getInt(key, 0);
		if (cool > 0) {
			String loc = Cooldowns.getLocale(locale).getString("cooldown.commands._generic_", "'<cmd>' is currently cooling down. (<cd>)");
			String tmp = "";
			int cd = 0;
			for (CoolCommand cc : commands) {
				if (cc.getCommand().equalsIgnoreCase(msg)) {
					tmp = loc;
					cd = (int) ((cc.cooldown - System.currentTimeMillis()) / 1000);
					tmp = Messages.COMMAND.matcher(tmp).replaceAll(cc.getHandle());
					tmp = Messages.LABEL.matcher(tmp).replaceAll(cc.label);
					tmp = Messages.FLAG.matcher(tmp).replaceAll(cc.flag);
					tmp = Messages.COOLDOWN.matcher(tmp).replaceAll(""+cd);
					tmp = Messages.COLOR.matcher(tmp).replaceAll("\u00A7$1");
					Cooldowns.getPlayer(name).sendMessage(tmp);
					return false;
				}
			}
			commands.add(new CoolCommand(label, flag, cool, msg));
			if (!coolers.contains(name)) {
				coolers.add(name);
			}
			return true;
		}
		return false;
	}

	private String getCommandKey(String label, String flag) {
		ConfigurationNode node = Cooldowns.getGroupNode(group, "commands.cooldown");
		// check for the label normally
		if (node.getKeys().contains(label)) {
			// check for sub keys
			if (node.getKeys(label) != null) {
				// sub keys found
				if (node.getKeys(label).contains(flag)) {
					return label + "." + flag;
				}
				if (node.getKeys(label).contains("_self_") && flag.equalsIgnoreCase(name)) {
					return label + "._self_";
				}
				if (node.getKeys(label).contains("_all_")) {
					return label + "._all_";
				}
				else {
					// command isn't mapped for this user
					return null;
				}
			} else {
				// no sub keys
				return label;
			}
		}
		// check for the _all_ key
		if (node.getKeys().contains("_all_")) {
			return "_all_";
		}
		return null;
	}

	public boolean isCooling(String msg) {
		if (global && cooling) {
			return true;
		} else {
			for (CoolCommand cmd : commands) {
				if (cmd.getCommand().equalsIgnoreCase(msg)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public String toString() {
		return "CoolPlayer{name=" + name + "}";
	}

	@Override
	public WarmPlayer getOtherHalf() {
		return PlayerManager.getWarmPlayer(name);
	}

}
