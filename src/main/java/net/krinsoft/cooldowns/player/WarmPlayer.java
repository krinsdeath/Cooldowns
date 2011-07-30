package net.krinsoft.cooldowns.player;

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

public class WarmPlayer implements Serializable {
	// version ID
	private final static long serialVersionUID = 119393L;

	// ------- //
	// STATICS //
	// ------- //

	/**
	 * A HashMap containing key:value pairs of players to their warmup info
	 */
	protected static HashMap<String, WarmPlayer> players = new HashMap<String, WarmPlayer>();
	/**
	 * A list of all currently warming players
	 */
	protected static List<String> warmers = new ArrayList<String>();

	/**
	 * Fetches a list of all players' warmup data.
	 * @return
	 * The list of players.
	 */
	public static List<WarmPlayer> getWarmingPlayers() {
		List<WarmPlayer> tmp = new ArrayList<WarmPlayer>();
		for (String key : warmers) {
			tmp.add(players.get(key));
		}
		if (tmp.size() < 1) { tmp = null; }
		return tmp;
	}

	/**
	 * Gets the warmup data for the specified player
	 * @param player
	 * The player to fetch
	 * @return
	 * the data
	 */
	public static WarmPlayer getPlayer(String player) {
		if (players.containsKey(player)) {
			return players.get(player);
		} else {
			return null;
		}
	}

	public static boolean addPlayer(String player) {
		if (players.containsKey(player)) {
			return false;
		} else {
			WarmPlayer guy = new WarmPlayer(player);
			players.put(player, guy);
			return true;
		}
	}

	private class WarmCommand implements ICommand {
		private String cmd;
		private String label;
		private String flag;
		private long warmup;

		public WarmCommand(String l, String f, long wu, String cmd) {
			label = l;
			flag = f;
			warmup = (wu * 1000) + System.currentTimeMillis();
		}

		@Override
		public String getHandle() {
			return ("/" + label + " " + flag).trim();
		}

		@Override
		public boolean getStatus() {
			if (warmup <= System.currentTimeMillis()) {
				return true;
			} else {
				return false;
			}
		}

		public String getCommand() {
			return cmd;
		}

		@Override
		public String toString() {
			return "WarmCommand{handle=" + getHandle() + "}";
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
	 * The player's warmup status
	 */
	private boolean done;
	/**
	 * A list of all currently warming commands on this player.
	 */
	private List<WarmCommand> commands = new ArrayList<WarmCommand>();

	public WarmPlayer(String player) {
		name = player;
		group = Cooldowns.getGroup(player);
	}

	/**
	 * Iterates through this player's active commands, and tries to warm them up
	 */
	public void update() {
		String loc = Cooldowns.getLocale(locale).getString("command.done", "'<cmd>' is done warming up.");
		String msg = "";
		for (WarmCommand wc : commands) {
			if (wc.getStatus()) {
				msg = loc;
				msg = msg.replaceAll("<cmd>|<command>", wc.getHandle());
				msg = msg.replaceAll("<label>", wc.label);
				msg = msg.replaceAll("<flag>", wc.flag);
				msg = msg.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
				Cooldowns.getPlayer(name).sendMessage(msg);
				commands.remove(wc);
			} else {
				continue;
			}
		}
		if (commands.size() < 1) {
			warmers.remove(name);
		}
	}

	public boolean addCommand(String msg) {
		if (!isDone()) {
			String label = msg.split(" ")[0].substring(1), flag = "";
			if (msg.split(" ").length > 1) {
				flag = msg.split(" ")[1];
			}
			String key = getCommandKey(label, flag);
			if (key == null) { return false; }
			ConfigurationNode node = Cooldowns.getCommandNode(group, "warmup");
			int warm = node.getInt(key, 0);
			if (warm > 0) {
				String loc = Cooldowns.getLocale(locale).getString("warmup.commands._generic_", "You are currently warming up '<cmd>'");
				String tmp = "";
				int wu = 0;
				for (WarmCommand wc : commands) {
					if (wc.getHandle().equalsIgnoreCase(("/" + label + " " + flag).trim())) {
						tmp = loc;
						wu = (int) ((wc.warmup - System.currentTimeMillis()) / 1000);
						tmp = tmp.replaceAll("<cmd>|<command>", wc.getHandle());
						tmp = tmp.replaceAll("<label>", wc.label);
						tmp = tmp.replaceAll("<flag>", wc.flag);
						tmp = tmp.replaceAll("<cd>|<cooldown>", "" + wu);
						tmp = tmp.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
						Cooldowns.getPlayer(name).sendMessage(tmp);
						return false;
					}
				}
				commands.add(new WarmCommand(label, flag, warm, msg));
				if (!warmers.contains(name)) {
					warmers.add(name);
				}
				return true;
			}
			return false;
		} else {
			done = false;
			return false;
		}
	}

	public void cancelCommand() {
		String loc = Cooldowns.getLocale(locale).getString("command.cancel", "You cancelled the command '<cmd>'");
		String tmp = "";
		int wu = 0;
		for (WarmCommand wc : commands) {
			wu = (int) ((wc.warmup - System.currentTimeMillis()) / 1000);
			tmp = tmp.replaceAll("<cmd>|<command>", wc.getHandle());
			tmp = tmp.replaceAll("<label>", wc.label);
			tmp = tmp.replaceAll("<flag>", wc.flag);
			tmp = tmp.replaceAll("<cd>|<cooldown>", "" + wu);
			tmp = tmp.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
			Cooldowns.getPlayer(name).sendMessage(tmp);
			commands.remove(wc);
			return;
		}
	}

	public boolean isWarming(String msg) {
		String label = msg.split(" ")[0].substring(1), flag = "";
		if (msg.split(" ").length > 1) {
			flag = msg.split(" ")[1];
		}
		for (WarmCommand wc : commands) {
			if (wc.getHandle().equalsIgnoreCase(("/" + label + " " + flag).trim())) {
				return true;
			}
		}
		return false;
	}

	public String getCommandKey(String label, String flag) {
		ConfigurationNode node = Cooldowns.getGroupNode(group, "commands.warmup");
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

	public boolean isDone() {
		return done;
	}
	
}
