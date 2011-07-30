package net.krinsoft.cooldowns;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import net.krinsoft.cooldowns.interfaces.IPlayer;
import net.krinsoft.cooldowns.listeners.CommandListener;
import net.krinsoft.cooldowns.listeners.EntityListener;
import net.krinsoft.cooldowns.listeners.PlayerListener;
import net.krinsoft.cooldowns.util.CoolLogger;
import net.krinsoft.cooldowns.util.CoolTimer;
import net.krinsoft.cooldowns.util.Persister;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */

public class Cooldowns extends JavaPlugin {
	// logger
	protected static CoolLogger log = new CoolLogger();
	protected static HashMap<String, LinkedList<IPlayer>> players = new HashMap<String, LinkedList<IPlayer>>();

	public static CoolLogger getLogger() {
		return log;
	}

	// instance stuff
	protected PluginManager manager;
	protected PluginDescriptionFile info;
	private static Settings settings;

	// static stuff
	protected static Plugin plugin;
	protected static Configuration config;
	protected static Configuration users;
	protected static HashMap<String, Configuration> locales = new HashMap<String, Configuration>();

	// listeners
	protected static final PlayerListener pListener = new PlayerListener();
	protected static final EntityListener eListener = new EntityListener();
	protected static final CommandListener cListener = new CommandListener();

	@Override
	public void onEnable() {
		log.setParent(this);
		plugin = this;
		manager = getServer().getPluginManager();
		info = getDescription();

		// player events
		manager.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, pListener, Event.Priority.Lowest, this);
		manager.registerEvent(Event.Type.PLAYER_JOIN, pListener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.PLAYER_QUIT, pListener, Event.Priority.Normal, this);
		manager.registerEvent(Event.Type.PLAYER_KICK, pListener, Event.Priority.Normal, this);

		// entity events
		manager.registerEvent(Event.Type.ENTITY_DAMAGE, eListener, Event.Priority.Normal, this);
		
		// settings
		settings = new Settings(this);

		// load the players
		Persister.load();

		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new CoolTimer(), 20, 20);

		// we're done!
		log.info("Enabled.");
	}

	@Override
	public void onDisable() {
		Persister.save();
		getServer().getScheduler().cancelTasks(this);
		log.info("Disabled.");
	}

	/**
	 * Returns information from this plugin's plugin.yml
	 * @param field
	 * Which field from the YAML to fetch
	 * @return
	 * the associated data
	 */
	public String info(String field) {
		if (field.equalsIgnoreCase("fullname")) {
			return info.getFullName();
		} else if (field.equalsIgnoreCase("name")) {
			return info.getName();
		} else if (field.equalsIgnoreCase("version")) {
			return info.getVersion();
		} else if (field.equalsIgnoreCase("authors")) {
			return info.getAuthors().get(0);
		}
		return info.getName();
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		return cListener.onCommand(cs, cmd, label, args);
	}

	/**
	 * Determines the relevant group for the specified player
	 * @param name
	 * The name of the player with which to check permissions
	 * @return
	 * The group (if any), or 'default'
	 */
	public static String getGroup(String name) {
		Player player = plugin.getServer().getPlayer(name);
		if (player == null) {
			return null;
		} else {
			for (String key : config.getKeys("groups")) {
				if (player.hasPermission("cooldowns." + key)) {
					return key;
				}
			}
		}
		return "default";
	}

	/**
	 * Gets a (currently online) player
	 * @param name
	 * The name of the player to get
	 * @return
	 * the player
	 */
	public static Player getPlayer(String name) {
		return plugin.getServer().getPlayer(name);
	}

	/**
	 * Returns the configuration file for the localization specified
	 * @param loc
	 * A string denoting the file name before the file extension [en_US]
	 * @return
	 * the localization configuration
	 */
	public static Configuration getLocale(String loc) {
		return locales.get(loc);
	}

	public static void addLocale(String loc, File tmp) {
		Configuration conf = new Configuration(tmp);
		conf.load();
		locales.put(loc, conf);
	}

	public static ConfigurationNode getCommandNode(String group, String section) {
		return config.getNode("groups." + group + ".commands." + section);
	}

	public static ConfigurationNode getGroupNode(String group, String string) {
		return config.getNode("groups." + group + "." + string);
	}

	public static boolean getGlobal(String group, String key) {
		return config.getBoolean("groups." + group + ".globals." + key, true);
	}
}
