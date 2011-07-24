package net.krinsoft.cooldowns.util;

import java.util.ArrayList;
import java.util.List;
import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.types.CoolPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */

public class CoolTimer implements Runnable {
    private Cooldowns plugin;

    public CoolTimer(Cooldowns instance) {
        plugin = instance;
    }

    public void run() {
        doWarmups();
        doCooldowns();
    }

    public void doWarmups() {
        List<String> worlds = plugin.config.getStringList("plugin.worlds", new ArrayList<String>());
        // Iterate through the worlds
        for (World world : plugin.getServer().getWorlds()) {
            // check if this world is handled by the plugin
            if (!worlds.contains(world.getName())) {
                // this world wasn't found in the handler, continuing to next world
                continue;
            }
            // Iterate through players
            for (Player player : world.getPlayers()) {
                // make sure the player exists before performing operations
                if (plugin.getPlayer(player) == null) {
                    plugin.addPlayer(player);
                }
                // set a temp variable containing the player
                CoolPlayer data = plugin.getPlayer(player);
                if (data.isWarming()) {
                    // player is warming up, nothing we can do
                    continue;
                }
                if (data.isWarmed()) {
                    try {
                        data.execute();
                    } catch (NullPointerException e) {
                        data.cancel();
                    }
                }
                plugin.updatePlayer(player, data);
            }
        }
    }

    public void doCooldowns() {
        List<String> worlds = plugin.config.getStringList("plugin.worlds", new ArrayList<String>());
        for (World world : plugin.getServer().getWorlds()) {
            if (!worlds.contains(world.getName())) {
                continue;
            }

            for (Player player : world.getPlayers()) {
                if (plugin.getPlayer(player) == null) {
                    plugin.addPlayer(player);
                }

                CoolPlayer data = plugin.getPlayer(player);
                if (data.isCooling()) {
                    // will return true if the player is cooling, or false if not
                    continue;
                }
                if (data.isCooled()) {
                    String loc = plugin.getLocale(data.getLocale()).getString("command.ready", "'<cmd>' is ready");
                    // check if the server is sending this message
                    if (loc != null) {
                        data.parse(loc);
                    }
                    data.finishCooldown();
                    continue;
                }
            }
        }
    }
}
