package net.krinsoft.cooldowns.util;

import java.util.logging.Logger;
import net.krinsoft.cooldowns.Cooldowns;
import org.bukkit.ChatColor;

/**
 *
 * @author krinsdeath
 */

public class CoolLogger {
    private final Cooldowns plugin;
    private String prefix;
    private final Logger log;

    public CoolLogger(Cooldowns instance) {
        plugin = instance;
        log = plugin.getServer().getLogger();
    }

    public void info(String msg) {
        if (plugin.config != null) {
            prefix = plugin.config.getString("plugin.prefix", "[" + plugin.getDescription().getFullName() + "] ");
        } else {
            prefix = "[" + plugin.getDescription().getFullName() + "] ";
        }
        if (System.getProperty("os.name").contains("Windows")) {
            msg = prefix + msg;
        } else {
            if (plugin.config != null && plugin.config.getBoolean("plugin.colors", true)) {
                msg = prefix + ChatColor.GREEN + msg;
            } else {
                msg = prefix + msg;
            }
        }
        msg = msg.replaceAll("<fullname>", plugin.getDescription().getFullName());
        msg = msg.replaceAll("<shortname>", plugin.getDescription().getName());
        msg = msg.replaceAll("<version>", plugin.getDescription().getVersion());
        log.info(msg);
    }

    public void warn(String msg) {
        if (plugin.config != null) {
            prefix = plugin.config.getString("plugin.prefix", "[" + plugin.getDescription().getFullName() + "] ");
        } else {
            prefix = "[" + plugin.getDescription().getFullName() + "] ";
        }
        if (System.getProperty("os.name").contains("Windows")) {
            msg = prefix + msg;
        } else {
            if (plugin.config != null && plugin.config.getBoolean("plugin.colors", true)) {
                msg = prefix + ChatColor.RED + msg;
            } else {
                msg = prefix + msg;
            }
        }
        msg = msg.replaceAll("<fullname>", plugin.getDescription().getFullName());
        msg = msg.replaceAll("<shortname>", plugin.getDescription().getName());
        msg = msg.replaceAll("<version>", plugin.getDescription().getVersion());
        log.warning(msg);
    }
    
}
