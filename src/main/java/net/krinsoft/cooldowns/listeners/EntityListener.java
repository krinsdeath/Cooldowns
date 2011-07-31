package net.krinsoft.cooldowns.listeners;

import net.krinsoft.cooldowns.player.CoolPlayer;
import net.krinsoft.cooldowns.player.PlayerManager;
import net.krinsoft.cooldowns.player.WarmPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 *
 * @author krinsdeath
 */

public class EntityListener extends org.bukkit.event.entity.EntityListener {

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (!PlayerManager.online().contains(p)) {
				if (!PlayerManager.getPlayer(p.getName())) {
					PlayerManager.addPlayer(p.getName());
				}
				PlayerManager.setOnline(p);
			}
			WarmPlayer w = PlayerManager.getWarmPlayer(p.getName());
			CoolPlayer c = PlayerManager.getCoolPlayer(p.getName());
			if (w.getInterrupt("damage")) {
				w.cancelCommand(true, "_damage_");
			}
			PlayerManager.updatePlayer(w, c);
		}

	}

}
