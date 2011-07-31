package net.krinsoft.cooldowns.util;

import java.util.List;
import java.util.TimerTask;
import net.krinsoft.cooldowns.player.CoolPlayer;
import net.krinsoft.cooldowns.player.PlayerManager;
import net.krinsoft.cooldowns.player.WarmPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */

public class CoolTimer extends TimerTask {

	public void run() {
		List<Player> list = PlayerManager.online();
		for (Player p : list) {
			WarmPlayer w = PlayerManager.getWarmPlayer(p.getName());
			CoolPlayer c = PlayerManager.getCoolPlayer(p.getName());
			w.update();
			c.update();
			PlayerManager.updatePlayer(w, c);
		}
	}

}
