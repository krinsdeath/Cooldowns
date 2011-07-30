package net.krinsoft.cooldowns.player;

import java.io.Serializable;
import java.util.ArrayList;
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

public class WarmPlayer implements Serializable, IPlayer {
	// version ID
	private final static long serialVersionUID = 119393L;
	public static List<String> warmers = new ArrayList<String>();

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
	private boolean global;
	private boolean warming;
	/**
	 * A list of all currently warming commands on this player.
	 */
	private List<WarmCommand> commands = new ArrayList<WarmCommand>();

	public WarmPlayer(String player) {
		name = player;
		group = Cooldowns.getGroup(player);
		global = Cooldowns.getGlobal(group, "warmups");
		locale = "en_US";
		System.out.println("WarmPlayer{name=" + name + ",group=" + group + "} created.");
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
				msg = Messages.COMMAND.matcher(msg).replaceAll(wc.getHandle());
				msg = Messages.LABEL.matcher(msg).replaceAll(wc.label);
				msg = Messages.FLAG.matcher(msg).replaceAll(wc.flag);
				msg = Messages.COLOR.matcher(msg).replaceAll("\u00A7$1");
				Cooldowns.getPlayer(name).sendMessage(msg);
				commands.remove(wc);
				Cooldowns.getPlayer(name).chat(wc.getCommand());
			} else {
				continue;
			}
		}
		if (commands.size() < 1) {
			warmers.remove(name);
		}
	}

	public boolean addCommand(String msg) {
		if (global && done) {
			done = false;
			return false;
		}
		// split the message up for searching
		String label = msg.split(" ")[0].substring(1), flag = "";
		if (msg.split(" ").length > 1) {
			flag = msg.split(" ")[1];
		}
		String key = getCommandKey(label, flag);
		if (key == null) { return false; }
		ConfigurationNode node = Cooldowns.getCommandNode(group, "warmup");
		int warm = node.getInt(key, 0);
		// check if the command has a warmup
		if (warm > 0) {
			// build a base string
			String loc = Cooldowns.getLocale(locale).getString("warmup.commands._generic_", "You are currently warming up '<cmd>'");
			String tmp = "";
			int wu = 0;
			for (WarmCommand wc : commands) {
				if (global) {
					cancelCommand(msg);
					return false;
				}
				if (wc.getCommand().equalsIgnoreCase(msg)) {
					tmp = loc;
					wu = (int) ((wc.warmup - System.currentTimeMillis()) / 1000);
					tmp = Messages.COMMAND.matcher(tmp).replaceAll(wc.getHandle());
					tmp = Messages.LABEL.matcher(tmp).replaceAll(wc.label);
					tmp = Messages.FLAG.matcher(tmp).replaceAll(wc.flag);
					tmp = Messages.WARMUP.matcher(tmp).replaceAll(""+wu);
					tmp = Messages.COLOR.matcher(tmp).replaceAll("\u00A7$1");
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
	}

	public void cancelCommand(String msg) {
		String loc = Cooldowns.getLocale(locale).getString("command.cancel", "You cancelled the command '<cmd>'");
		String tmp = "";
		int wu = 0;
		for (WarmCommand wc : commands) {
			if (wc.getCommand().equalsIgnoreCase(msg)) {
				wu = (int) ((wc.warmup - System.currentTimeMillis()) / 1000);
				tmp = Messages.COMMAND.matcher(tmp).replaceAll(wc.getHandle());
				tmp = Messages.LABEL.matcher(tmp).replaceAll(wc.label);
				tmp = Messages.FLAG.matcher(tmp).replaceAll(wc.flag);
				tmp = Messages.WARMUP.matcher(tmp).replaceAll(""+wu);
				tmp = Messages.COLOR.matcher(tmp).replaceAll("\u00A7$1");
				Cooldowns.getPlayer(name).sendMessage(tmp);
				if (global) {
					commands.clear();
				} else {
					commands.remove(wc);
				}
				return;
			}
		}
	}

	public boolean isWarming(String msg) {
		if (global && warming) {
			return true;
		} else {
			for (WarmCommand wc : commands) {
				if (wc.getCommand().equalsIgnoreCase(msg)) {
					return true;
				}
			}
			return false;
		}
	}

	private String getCommandKey(String label, String flag) {
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

	@Override
	public CoolPlayer getOtherHalf() {
		return PlayerManager.getCoolPlayer(name);
	}

}
