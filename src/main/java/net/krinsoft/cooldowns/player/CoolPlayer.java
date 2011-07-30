package net.krinsoft.cooldowns.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.interfaces.ICommand;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */

public class CoolPlayer implements Serializable {
	// version ID
	private final static long serialVersionUID = 11932L;

	// ------- //
	// STATICS //
	// ------- //

	/**
	 * A HashMap containing key:value pairs of player names to their cooldown data
	 */
	protected static HashMap<String, CoolPlayer> players = new HashMap<String, CoolPlayer>();
	/**
	 * A list of all currently cooling players
	 */
	protected static List<String> coolers = new ArrayList<String>();

	/**
	 * Retrieves a list of all cooling players
	 * @return
	 * A list of players, or null
	 */
	public static List<CoolPlayer> getCoolingPlayers() {
		List<CoolPlayer> tmp = new ArrayList<CoolPlayer>();
		for (String key : coolers) {
			tmp.add(players.get(key));
		}
		if (tmp.size() < 1) { tmp = null; }
		return tmp;
	}

	/**
	 * Retrieves the specified player
	 * @param player
	 * The player to fetch
	 * @return
	 * the player, or null
	 */
	public static CoolPlayer getPlayer(String player) {
		if (players.containsKey(player)) {
			return players.get(player);
		} else {
			return null;
		}
	}

	/**
	 * Adds a player to the currently cooling list
	 * @param player
	 * The player to add
	 * @return
	 * true if the player is added, false otherwise
	 */
	public static boolean addPlayer(String player) {
		if (players.containsKey(player)) {
			return false;
		} else {
			CoolPlayer guy = new CoolPlayer(player);
			players.put(player, guy);
			return true;
		}
	}

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

	/**
	 * Constructs a new instance of CoolPlayer
	 * @param player
	 * A string representing the player's name
	 */
	public CoolPlayer(String player) {
		this.name = player;
		this.group = Cooldowns.getGroup(player);
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
				msg = msg.replaceAll("<cmd>|<command>", cc.getHandle());
				msg = msg.replaceAll("<label>", cc.label);
				msg = msg.replaceAll("<flag>", cc.flag);
				msg = msg.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
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
				if (cc.getHandle().equalsIgnoreCase(("/" + label + " " + flag).trim())) {
					tmp = loc;
					cd = (int) ((cc.cooldown - System.currentTimeMillis()) / 1000);
					tmp = tmp.replaceAll("<cmd>|<command>", cc.getHandle());
					tmp = tmp.replaceAll("<label>", cc.label);
					tmp = tmp.replaceAll("<flag>", cc.flag);
					tmp = tmp.replaceAll("<cd>|<cooldown>", "" + cd);
					tmp = tmp.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
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

	public String getCommandKey(String label, String flag) {
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

	@Override
	public String toString() {
		return "CoolPlayer{name=" + name + "}";
	}

	/**
	 * Saves the Synced Chests to disk
	 */
	public static void save() {
		FileOutputStream file = null;
		ObjectOutputStream out = null;
		File tmp = new File("plugins/Cooldowns/users.dat");
		try {
			if (!tmp.exists()) {
				tmp.getParentFile().mkdirs();
				tmp.createNewFile();
			}
			if (tmp.length() > 0) {
				tmp.delete();
				tmp.createNewFile();
			}
			file = new FileOutputStream(tmp);
			out = new ObjectOutputStream(file);
			out.writeObject(players);
			out.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Loads the Synced Chests from disk
	 * @param plugin
	 * Sets up a static reference for the Synced Chests to use (for logging)
	 */
	public static void load() {
		HashMap<String, CoolPlayer> list = null;
		FileInputStream file = null;
		ObjectInputStream in = null;
		File tmp = new File("plugins/Cooldowns/users.dat");
		try {
			if (!tmp.exists()) {
				tmp.getParentFile().mkdirs();
				tmp.createNewFile();
			}
			file = new FileInputStream(tmp);
			in = new ObjectInputStream(file);
			list = (HashMap<String, CoolPlayer>) in.readObject();
			in.close();
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}
		players = list;
	}
}
