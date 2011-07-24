package net.krinsoft.cooldowns.listeners;

import java.util.ArrayList;
import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.types.CoolPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author krinsdeath
 */

public class PlayerEvents extends PlayerListener {
    public Cooldowns plugin;

    public PlayerEvents(Cooldowns instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) { return; }
        Player player = event.getPlayer();
        if (plugin.getPlayer(player) == null) {
            plugin.addPlayer(player);
        }
        // make sure this world is handled
        if (!plugin.config.getStringList("plugin.worlds", new ArrayList<String>()).contains(event.getPlayer().getWorld().getName())) {
            // this world isn't handled
            return;
        }
        CoolPlayer data = plugin.getPlayer(player);
        if (data.isWarmed()) {
            // player has finished warming up and this is his command executing
            // start the player on cooldown
            data.startCooling();
            String cooler = plugin.getLocale(data.getLocale()).getString("command.cool", "You are now cooling down. (<cd>)");
            data.parse(cooler);
            plugin.updatePlayer(player, data);
            return;
        }
        if (data.isCooling()) {
            // player is cooling down, so tell him he can't do anything
            if (data.hasCommand("cooldown", event.getMessage())) {
                // player is cooling down and has this command mapped
                // tell him he's cooling, and cancel
                String cooling = plugin.getLocale(data.getLocale()).getString("cooldown.commands._generic_", "You are currently cooling down. (<cd>)");
                data.parse(cooling);
                event.setCancelled(true);
                return;
            } else {
                return;
            }
        }
        if (data.isWarming()) {
            if (data.hasCommand("warmup", event.getMessage())) {
                // player is warming up and has this command mapped
                // tell him he's warming, cancel the command, cancel this event
                String cancel = plugin.getLocale(data.getLocale()).getString("command.cancel", "You cancelled the command '<cmd>'");
                data.parse(cancel);
                data.cancel();
                event.setCancelled(true);
                plugin.updatePlayer(player, data);
                return;
            }
        }
        if (data.hasCommand("warmup", event.getMessage())) {
            // player has the command in his warmups, so we schedule it
            data.schedule(event.getMessage());
            // get the localization message and send it to the player
            String warmer = plugin.getLocale(data.getLocale()).getString("command.start", "You begin warming up '<cmd>'");
            data.parse(warmer);
            // update the player in the map
            plugin.updatePlayer(player, data);
            // cancel the event to be executed later
            event.setCancelled(true);
            return;
        }
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getPlayer(event.getPlayer()) == null) {
            plugin.addPlayer(event.getPlayer());
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getPlayer(event.getPlayer()) != null) {
            plugin.getPlayers().remove(event.getPlayer());
        }
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        if (plugin.getPlayer(event.getPlayer()) != null) {
            plugin.getPlayers().remove(event.getPlayer());
        }
    }

}
