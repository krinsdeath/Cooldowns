package net.krinsoft.cooldowns.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.interfaces.ICommand;
import net.krinsoft.cooldowns.interfaces.IPlayer;
import net.krinsoft.cooldowns.util.Messages;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */

public class CoolPlayer implements Serializable, IPlayer {
	// version ID
	private final static long serialVersionUID = 11932L;

	private class CoolCommand implements ICommand, Serializable {
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
	}

	public boolean getGlobal() {
		global = Cooldowns.getGlobal(group, "cooldowns");
		return global;
	}

	/**
	 * Iterates through this player's active commands, and tries to cool them down
	 */
	public void update() {
		List<CoolCommand> tmp = commands;
		CoolCommand cc = null;
		synchronized (tmp) {
			String def = Cooldowns.getLocale(locale).getString("cooldown.done._generic_", "generic cooldown message");
			String msg = "", loc = "", key = "";
			if (tmp.isEmpty()) { return; }
			for (int i = 0; i < tmp.size(); i++) {
				cc = tmp.get(i);
				if (cc.getStatus()) {
					key = getCommandKey(cc.label, cc.flag);
					loc = Cooldowns.getLocale(locale).getString("cooldown.done." + key, def);
					msg = Cooldowns.getConfig().getString("groups."+group+".prefix", "[" + group + "] ") + loc;
					msg = Messages.COMMAND.matcher(msg).replaceAll(cc.getHandle());
					msg = Messages.LABEL.matcher(msg).replaceAll(cc.label);
					msg = Messages.FLAG.matcher(msg).replaceAll(cc.flag);
					msg = Messages.COLOR.matcher(msg).replaceAll("\u00A7$1");
					Cooldowns.getPlayer(name).sendMessage(msg);
					commands.remove(cc);
					if (commands.isEmpty()) {
						cooling = false;
					}
				} else {
					continue;
				}
			}
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
		if (key.split("\\.").length > 1) {
			flag = key.split("\\.")[1];
		} else {
			flag = "";
		}
		String handle = ("/" + key.replaceAll("\\.", " ")).trim();
		ConfigurationNode node = Cooldowns.getCommandNode(group, "cooldown");
		int cool = node.getInt(key, 0);
		if (cool > 0) {
			String def = Cooldowns.getLocale(locale).getString("cooldown.status._generic_", "generic cooldown message");
			String loc = Cooldowns.getLocale(locale).getString("cooldown.status." + key, def);
			String tmp = "";
			int cd = 0;
			for (CoolCommand cc : commands) {
				if (cc.getHandle().equalsIgnoreCase(handle)) {
					tmp = Cooldowns.getConfig().getString("groups."+group+".prefix", "[" + group + "] ") + loc;
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
			cooling = true;
			commands.add(new CoolCommand(label, flag, cool, msg));
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
				if (flag.length() < 1) { flag = null; }
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
		String label = msg.split(" ")[0].substring(1), flag = "";
		if (msg.split(" ").length > 1) {
			flag = msg.split(" ")[1];
		}
		String key = getCommandKey(label, flag);
		if (key == null) { return false; }
		String handle = ("/" + key.replaceAll("\\.", " ")).trim();
		if (getGlobal() && cooling) {
			sendMessage("status", msg);
			return true;
		}
		for (CoolCommand cmd : commands) {
			if (cmd.getHandle().equalsIgnoreCase(handle)) {
				sendMessage("status", msg);
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "CoolPlayer{name=" + name + ",group=" + group + "}";
	}

	@Override
	public WarmPlayer getOtherHalf() {
		return PlayerManager.getWarmPlayer(name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public Configuration getLocale() {
		return Cooldowns.getLocale(locale);
	}

	@Override
	public void setLocale(String loc) {
		locale = loc;
	}

	public void sendMessage(String field, String msg) {
		Player p = Cooldowns.getPlayer(name);
		String label = msg.split(" ")[0].substring(1), flag = "";
		if (msg.split(" ").length > 1) {
			flag = msg.split(" ")[1];
		}
		String key = getCommandKey(label, flag);
		String handle = ("/" + key.replaceAll("\\.", " ")).trim();
		int cd = 0;
		for (CoolCommand cc : commands) {
			if (cc.getHandle().equalsIgnoreCase(handle)) {
				cd = (int) ((cc.cooldown - System.currentTimeMillis()) / 1000);
			}
		}
		if (cd == 0) { return; }
		String def = Cooldowns.getLocale(locale).getString("cooldown."+field+"._generic_");
		if (def == null) { return; }
		String loc = Cooldowns.getConfig().getString("groups."+group+".prefix", "[" + group + "] ") + Cooldowns.getLocale(locale).getString("cooldown."+field+"." + key, def);
		loc = Messages.COMMAND.matcher(loc).replaceAll(msg);
		loc = Messages.LABEL.matcher(loc).replaceAll(label);
		loc = Messages.FLAG.matcher(loc).replaceAll(flag);
		loc = Messages.COOLDOWN.matcher(loc).replaceAll(""+cd);
		loc = Messages.COLOR.matcher(loc).replaceAll("\u00A7$1");
		p.sendMessage(loc);
	}

	public void showCooldowns() {
		if (commands.isEmpty()) {
			return;
		}
		for (CoolCommand c : commands) {
			sendMessage("status", c.getCommand());
		}
	}
}
