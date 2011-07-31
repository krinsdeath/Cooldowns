package net.krinsoft.cooldowns.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.interfaces.ICommand;
import net.krinsoft.cooldowns.interfaces.IPlayer;
import net.krinsoft.cooldowns.util.Messages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */

public class WarmPlayer implements Serializable, IPlayer {
	// version ID
	private final static long serialVersionUID = 119393L;

	private class WarmCommand implements ICommand {
		private String command;
		private String label;
		private String flag;
		private long warmup;

		public WarmCommand(String l, String f, long wu, String cmd) {
			label = l;
			flag = f;
			warmup = (wu * 1000) + System.currentTimeMillis();
			command = cmd;
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
			return command;
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
	 * interruption booleans
	 */
	private boolean movement;
	private boolean command;
	private boolean damage;

	/**
	 * location variables
	 */
	private double x;
	private double z;

	/**
	 * A list of all currently warming commands on this player.
	 */
	private List<WarmCommand> commands = new ArrayList<WarmCommand>();

	public WarmPlayer(String player) {
		name = player;
		group = Cooldowns.getGroup(player);
		global = Cooldowns.getGlobal(group, "warmups");
		setInterrupts();
		locale = Cooldowns.getConfig().getString("plugin.default_locale");
	}

	private void setInterrupts() {
		ConfigurationNode node = Cooldowns.getGroupNode(group, "interrupts");
		movement = node.getBoolean("movement", false);
		command = node.getBoolean("command", true);
		damage = node.getBoolean("damage", true);
	}

	public boolean getInterrupt(String t) {
		setInterrupts();
		if (t.equalsIgnoreCase("movement")) {
			return movement;
		} else if (t.equalsIgnoreCase("command")) {
			return command;
		} else if (t.equalsIgnoreCase("damage")) {
			return damage;
		} else {
			return true;
		}
	}

	public void setLocation(Location location) {
		x = location.getX();
		z = location.getZ();
	}

	public boolean locationHasChanged(Location location) {
		if (movement) {
			if ((int) location.getX() != (int) x) {
				return true;
			}
			if ((int) location.getZ() != (int) z) {
				return true;
			}
		}
		return false;
	}

	public boolean getGlobal() {
		return global;
	}

	/**
	 * Iterates through this player's active commands, and tries to warm them up
	 */
	public void update() {
		List<WarmCommand> tmp = commands;
		WarmCommand wc = null;
		synchronized (tmp) {
			String def = Cooldowns.getLocale(locale).getString("warmup.done._generic_", "generic warmup message");
			String msg = "", key = "", loc = "";
			if (tmp.isEmpty()) { return; }
			for (int i = 0; i < tmp.size(); i++) {
				wc = tmp.get(i);
				if (wc.getStatus()) {
					key = getCommandKey(wc.label, wc.flag);
					loc = Cooldowns.getLocale(locale).getString("warmup.done." + key, def);
					msg = loc;
					msg = Messages.COMMAND.matcher(msg).replaceAll(wc.getHandle());
					msg = Messages.LABEL.matcher(msg).replaceAll(wc.label);
					msg = Messages.FLAG.matcher(msg).replaceAll(wc.flag);
					msg = Messages.COLOR.matcher(msg).replaceAll("\u00A7$1");
					Cooldowns.getPlayer(name).sendMessage(msg);
					String cmd = wc.getCommand();
					commands.remove(wc);
					if (commands.isEmpty()) {
						warming = false;
					}
					done = true;
					Cooldowns.getPlayer(name).chat(cmd);
				} else {
					continue;
				}
			}
		}
	}

	public boolean addCommand(String msg) {
		if (done) {
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
		if (key.split("\\.").length > 1) {
			flag = key.split("\\.")[1];
		} else {
			flag = "";
		}
		String handle = ("/" + key.replaceAll("\\.", " ")).trim();
		ConfigurationNode node = Cooldowns.getCommandNode(group, "warmup");
		int warm = node.getInt(key, 0);
		if (warm > 0) {
			String def = Cooldowns.getLocale(locale).getString("warmup.status._generic_", "You are currently warming up '<cmd>'");
			String loc = Cooldowns.getLocale(locale).getString("warmup.status." + key, def);
			String tmp = "";
			int wu = 0;
			for (WarmCommand wc : commands) {
				if (global && command) {
					cancelCommand(msg);
					return false;
				}
				if (wc.getHandle().equalsIgnoreCase(handle) && command) {
					tmp = loc;
					wu = (int) ((wc.warmup - System.currentTimeMillis()) / 1000);
					tmp = Messages.COMMAND.matcher(tmp).replaceAll(wc.getHandle());
					tmp = Messages.LABEL.matcher(tmp).replaceAll(wc.label);
					tmp = Messages.FLAG.matcher(tmp).replaceAll(wc.flag);
					tmp = Messages.WARMUP.matcher(tmp).replaceAll(""+wu);
					tmp = Messages.COLOR.matcher(tmp).replaceAll("\u00A7$1");
					Cooldowns.getPlayer(name).sendMessage(tmp);
					cancelCommand(msg);
					return false;
				}
			}
			warming = true;
			commands.add(new WarmCommand(label, flag, warm, msg));
			if (movement) {
				setLocation(Cooldowns.getPlayer(name).getLocation());
			}
			return true;
		}
		return false;
	}

	public void cancelCommand(boolean flag, String field) {
		if (flag) {
			boolean f = false;
			if (field.equals("_damage_")) {
				f = damage;
			} else if (field.equals("_movement_")) {
				f = movement;
			}
			if (f) {
				String def = getLocale().getString("warmup.cancel." + field);
				String tmp = "", key = "";
				int wu = 0;
				WarmCommand w = null;
				for (int i = 0; i < commands.size(); i++) {
					w = commands.get(i);
					if (def != null) {
						if (w.flag.length() > 0) {
							key = w.label + "." + w.flag;
						} else {
							key = w.label;
						}
						tmp = def;
						wu = (int) ((w.warmup - System.currentTimeMillis()) / 1000);
						tmp = Messages.COMMAND.matcher(tmp).replaceAll(w.getHandle());
						tmp = Messages.LABEL.matcher(tmp).replaceAll(w.label);
						tmp = Messages.FLAG.matcher(tmp).replaceAll(w.flag);
						tmp = Messages.WARMUP.matcher(tmp).replaceAll(""+wu);
						tmp = Messages.COLOR.matcher(tmp).replaceAll("\u00A7$1");
						Cooldowns.getPlayer(name).sendMessage(tmp);
					}
				}
				commands.clear();
				return;
			}
		}
	}

	public void cancelCommand(String msg) {
		String def = Cooldowns.getLocale(locale).getString("warmup.cancel._generic_");
		String tmp = "";
		int wu = 0;
		if (commands.isEmpty()) {
			warming = false;
			return;
		}
		String label = msg.split(" ")[0].substring(1), flag = "";
		if (msg.split(" ").length > 1) {
			flag = msg.split(" ")[1];
		}
		String key = getCommandKey(label, flag);
		if (key == null) { return; }
		String handle = ("/" + key.replaceAll("\\.", " ")).trim();
		for (int i = 0; i < commands.size(); i++) {
			WarmCommand wc = commands.get(i);
			if (wc.getHandle().equalsIgnoreCase(handle)) {
				if (def != null) {
					tmp = Cooldowns.getLocale(locale).getString("warmup.cancel." + key, def);
					wu = (int) ((wc.warmup - System.currentTimeMillis()) / 1000);
					tmp = Messages.COMMAND.matcher(tmp).replaceAll(wc.getHandle());
					tmp = Messages.LABEL.matcher(tmp).replaceAll(wc.label);
					tmp = Messages.FLAG.matcher(tmp).replaceAll(wc.flag);
					tmp = Messages.WARMUP.matcher(tmp).replaceAll(""+wu);
					tmp = Messages.COLOR.matcher(tmp).replaceAll("\u00A7$1");
					Cooldowns.getPlayer(name).sendMessage(tmp);
				}
				if (global) {
					commands.clear();
					warming = false;
				} else {
					commands.remove(wc);
				}
				return;
			}
		}
	}

	public boolean isWarming(String msg) {
		String label = msg.split(" ")[0].substring(1), flag = "";
		if (msg.split(" ").length > 1) {
			flag = msg.split(" ")[1];
		}
		String key = getCommandKey(label, flag);
		if (key == null) { return false; }
		String handle = ("/" + key.replaceAll("\\.", " ")).trim();
		if (done) {
			return false;
		}
		if (global && warming) {
			sendMessage("status", msg);
			if (command) {
				cancelCommand(msg);
			}
			return true;
		} else {
			for (WarmCommand cmd : commands) {
				if (cmd.getHandle().equalsIgnoreCase(handle)) {
					sendMessage("status", msg);
					if (command) {
						cancelCommand(cmd.getCommand());
					}
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

	public boolean isDone() {
		return done;
	}

	@Override
	public CoolPlayer getOtherHalf() {
		return PlayerManager.getCoolPlayer(name);
	}

	@Override
	public String toString() {
		return "WarmPlayer{name=" + name + ",group=" + group + "}";
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
		int wu = 0;
		for (WarmCommand wc : commands) {
			if (wc.getHandle().equalsIgnoreCase(handle)) {
				wu = (int) ((wc.warmup - System.currentTimeMillis()) / 1000);
			}
		}
		if (wu == 0) { return; }
		String def = Cooldowns.getLocale(locale).getString("warmup."+field+"._generic_");
		if (def == null) { return; }
		String loc = Cooldowns.getLocale(locale).getString("warmup."+field+"." + key, def);
		loc = Messages.COMMAND.matcher(loc).replaceAll(msg);
		loc = Messages.LABEL.matcher(loc).replaceAll(label);
		loc = Messages.FLAG.matcher(loc).replaceAll(flag);
		loc = Messages.WARMUP.matcher(loc).replaceAll(""+wu);
		loc = Messages.COLOR.matcher(loc).replaceAll("\u00A7$1");
		p.sendMessage(loc);
	}
}
