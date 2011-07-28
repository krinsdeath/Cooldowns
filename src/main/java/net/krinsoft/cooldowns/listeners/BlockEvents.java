package net.krinsoft.cooldowns.listeners;

import net.krinsoft.cooldowns.Cooldowns;
import net.krinsoft.cooldowns.types.CoolPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */

public class BlockEvents extends BlockListener {
    private Cooldowns plugin;

    public BlockEvents(Cooldowns instance) {
        plugin = instance;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.config.getBoolean("plugin.block_limiter", false)) {
            return;
        }
        Player player = event.getPlayer();
        if (plugin.getPlayer(player) == null) {
            plugin.addPlayer(player);
        }

        CoolPlayer data = plugin.getPlayer(player);
        // get the node for this player's group
        ConfigurationNode blockList = plugin.config.getNode("groups." + data.getGroup() + ".blocks");
        // cancel this if it's null
        String mat = event.getBlock().getType().toString().toLowerCase();
        plugin.getLogger().info("mat: " + mat);
        if (blockList.getKeys() == null) { return; }
        if (blockList.getKeys().contains(mat)) {
            if (data.isBlocked()) {
                String blocker = plugin.getLocale(data.getLocale()).getString("cooldown.blocks._generic_", "");
                if (blocker != null) {
                    data.parse(blocker);
                }
                event.setCancelled(true);
                return;
            } else {
                data.setBlockdown(plugin.config.getInt("groups." + data.getGroup() + ".blocks." + mat, 0));
                return;
            }
        }
    }
}
