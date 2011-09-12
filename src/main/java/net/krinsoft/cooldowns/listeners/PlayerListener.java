package net.krinsoft.cooldowns.listeners;

import net.krinsoft.cooldowns.player.CoolPlayer;
import net.krinsoft.cooldowns.player.PlayerManager;
import net.krinsoft.cooldowns.player.WarmPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author krinsdeath
 */

public class PlayerListener extends org.bukkit.event.player.PlayerListener {

	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!PlayerManager.online().contains(event.getPlayer())) {
			if (!PlayerManager.getPlayer(event.getPlayer().getName())) {
				PlayerManager.addPlayer(event.getPlayer().getName());
			}
			PlayerManager.setOnline(event.getPlayer());
		}
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		// tries to schedule the command for warmup
		// returns true if the command is scheduled, else
		// false if the player is done warming up
		// false if the player doesn't have this command mapped for warmup
		// false if this command is currently warming up
		WarmPlayer w = PlayerManager.getWarmPlayer(player.getName());
		CoolPlayer c = PlayerManager.getCoolPlayer(player.getName());
		if (c.isCooling(event.getMessage())) {
			// this command is cooling
			// player might have globals: true
			// for now, we assume true
			event.setCancelled(true);
			return;
		}
		if (w.isWarming(event.getMessage())) {
			// this command is warming
			// player might have globals: true
			// for now, we assume true
			event.setCancelled(true);
			return;
		}

		// player is not warming or cooling (for this command)
		// attempt to schedule the command
		// returns true if the command is scheduled
		// returns false otherwise (no matter the reason)
		if (w.addCommand(event.getMessage())) {
			w.sendMessage("start", event.getMessage());
			event.setCancelled(true);
		} else {
			if (c.addCommand(event.getMessage())) {
				c.sendMessage("start", event.getMessage());
			} else {
				return;
			}
		}
		PlayerManager.updatePlayer(w, c);
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) { return; }
		if (!PlayerManager.online().contains(event.getPlayer())) {
			if (!PlayerManager.getPlayer(event.getPlayer().getName())) {
				PlayerManager.addPlayer(event.getPlayer().getName());
			}
			PlayerManager.setOnline(event.getPlayer());
		}
		WarmPlayer w = PlayerManager.getWarmPlayer(event.getPlayer().getName());
		if (w.locationHasChanged(event.getTo())) {
			w.cancelCommand(true, "_movement_");
		}
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!PlayerManager.online().contains(event.getPlayer())) {
			if (!PlayerManager.getPlayer(event.getPlayer().getName())) {
				PlayerManager.addPlayer(event.getPlayer().getName());
			}
            PlayerManager.updateGroup(event.getPlayer());
			PlayerManager.setOnline(event.getPlayer());
		}
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		PlayerManager.setOffline(event.getPlayer());
	}

	@Override
	public void onPlayerKick(PlayerKickEvent event) {
		PlayerManager.setOffline(event.getPlayer());
	}
}
