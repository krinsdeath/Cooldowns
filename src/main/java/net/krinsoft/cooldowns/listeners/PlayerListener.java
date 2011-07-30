package net.krinsoft.cooldowns.listeners;

import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.player.CoolPlayer;
import net.krinsoft.cooldowns.player.WarmPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author krinsdeath
 */

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
	private Cooldowns plugin;

	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (CoolPlayer.getPlayer(player.getName()) != null) {
			CoolPlayer.addPlayer(player.getName());
		}
		if (WarmPlayer.getPlayer(player.getName()) != null) {
			WarmPlayer.addPlayer(player.getName());
		}
		// tries to schedule the command for warmup
		// returns true if the command is scheduled, else
		// false if the player is done warming up
		// false if the player doesn't have this command mapped for warmup
		// false if this command is currently warming up
		WarmPlayer w = WarmPlayer.getPlayer(player.getName());
		if (w.addCommand(event.getMessage())) {
			// command was successfully scheduled
			event.setCancelled(true);
			return;
		} else {
			CoolPlayer c = CoolPlayer.getPlayer(player.getName());
			if (w.isWarming(event.getMessage())) {
				// player was warming this command
				event.setCancelled(true);
				return;
			}
			// check if he's done warming up
			if (w.isDone()) {
				// player was done warming up
				if (c.addCommand(event.getMessage())) {
					// player finished warming up, and the command is now cooling down
					return;
				} else {
					// player had no cooldown mapped for this command
					return;
				}
			}
		}
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {

	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {

	}

	@Override
	public void onPlayerKick(PlayerKickEvent event) {

	}
}
