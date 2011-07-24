package net.krinsoft.cooldowns;

import net.krinsoft.cooldowns.util.CoolTimer;
import net.krinsoft.cooldowns.listeners.BlockEvents;
import net.krinsoft.cooldowns.listeners.PlayerEvents;
import java.io.File;
import net.krinsoft.cooldowns.util.CoolLogger;
import java.util.HashMap;
import net.krinsoft.cooldowns.listeners.CommandListener;
import net.krinsoft.cooldowns.types.CoolPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

/**
 * Hello world!
 *
 */
public class Cooldowns extends JavaPlugin {
    // private instance stuff
    private CoolLogger log;
    private Settings settings;
    private PluginManager manager;

    // public configuration stuff
    public Configuration config;
    public Configuration users;
    public HashMap<Player, CoolPlayer> players = new HashMap<Player, CoolPlayer>();
    public HashMap<String, Configuration> locales = new HashMap<String, Configuration>();

    // public instance stuff
    public final PlayerEvents pListener = new PlayerEvents(this);
    public final BlockEvents bListener = new BlockEvents(this);
    public final CommandListener cListener = new CommandListener(this);

    public void onEnable() {
        // get the logger and put it in my logger
        log = new CoolLogger(this);
        // set up some settings!
        settings = new Settings(this);
        // set up the manager and register the events
        manager = this.getServer().getPluginManager();
        // players
        manager.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, pListener, Event.Priority.Lowest, this);
        manager.registerEvent(Event.Type.PLAYER_JOIN, pListener, Event.Priority.Lowest, this);
        manager.registerEvent(Event.Type.PLAYER_QUIT, pListener, Event.Priority.Lowest, this);
        manager.registerEvent(Event.Type.PLAYER_KICK, pListener, Event.Priority.Lowest, this);
        // blocks
        manager.registerEvent(Event.Type.BLOCK_PLACE, bListener, Event.Priority.Normal, this);
        // schedule the cooldown listener
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new CoolTimer(this), 20, 20);
        // set up the command 'cooldowns'
        getCommand("cooldowns").setExecutor(cListener);
        // we're done
        log.info(locales.get(config.getString("plugin.default_locale", "en_US")).getString("plugin.enabled", "Enabled."));
    }

    public void onDisable() {
        users.save();
        getServer().getScheduler().cancelTasks(this);
        log.info(locales.get(config.getString("plugin.default_locale", "en_US")).getString("plugin.disabled", "Disabled."));
    }


    /**
     * attempts to add the player to the active player list
     *
     * @param player
     */
    public void addPlayer(Player player) {
        String group = "";
        for (String key : config.getKeys("groups")) {
            if (player.hasPermission("cooldowns." + key)) {
                group = key;
                break;
            }
        }
        if (group.equals("")) {
            group = "default";
        }
        players.put(player, new CoolPlayer(this, player, group));
    }

    /**
     * Attempts to find the data associated with the player
     *
     * @param player
     * the player to fetch
     * @return CoolPlayer or null
     * the CoolPlayer data
     */
    public CoolPlayer getPlayer(Player player) {
        return players.get(player);
    }

    /**
     * attempts to fetch configuration data for a locale
     *
     * @param loc
     * the key
     * @return
     * the configuration associated with the parameter
     */
    public Configuration getLocale(String loc) {
        return locales.get(loc);
    }

    /**
     * Gets the logger associated with this plugin instance
     *
     * @return
     */
    public CoolLogger getLogger() {
        return log;
    }

    public void addLocale(String loc, File tmp) {
        Configuration conf = new Configuration(tmp);
        conf.load();
        locales.put(loc, conf);
    }

    public void updatePlayer(Player player, CoolPlayer data) {
        players.put(player, data);
    }

    public HashMap<Player, CoolPlayer> getPlayers() {
        return players;
    }

}
