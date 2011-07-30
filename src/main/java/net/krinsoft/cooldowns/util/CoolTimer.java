package net.krinsoft.cooldowns.util;

import net.krinsoft.cooldowns.player.PlayerManager;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */

public class CoolTimer implements Runnable {

	@Override
	public void run() {
		for (Player player : PlayerManager.online()) {
			PlayerManager.getWarmPlayer(player.getName()).update();
			PlayerManager.getCoolPlayer(player.getName()).update();
		}
	}

}
