package net.krinsoft.cooldowns.types;

import net.krinsoft.cooldowns.Cooldowns;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */

public class CoolPlayer {
    private Cooldowns plugin;
    private Player player;

    private String name;
    private String group;
    private String locale;

    private boolean blocked;
    private long blockdown;

    private boolean warming;
    private long warmup;
    private boolean warmed;

    private boolean cooling;
    private long cooldown;
    private boolean cooled;

    private String cmd;
    private String flag;
    private String label;

    public CoolPlayer(Cooldowns instance, Player plyr, String grp) {
        plugin = instance;
        player = plyr;
        name = plyr.getName();
        group = grp;
        locale = plugin.users.getString(name + ".locale", plugin.config.getString("plugin.default_locale", "en_US"));
    }

    @Override
    public String toString() {
        return "CoolPlayer{name=" + name + "}";
    }

    public boolean isWarming() {
        if (warming) {
            if (getWarmup() <= System.currentTimeMillis()) {
                warming = false;
                warmed = true;
            }
        }
        return warming;
    }

    public void setWarmup(int secs) {
        warmup = (secs * 1000) + System.currentTimeMillis();
        warming = true;
    }

    public long getWarmup() {
        return warmup;
    }

    public boolean isWarmed() {
        return warmed;
    }

    /**
     * attempts to schedule the command
     * 
     * @return
     * true if the command is scheduled, false if not
     */
    public boolean schedule(String cmd) {
        this.cmd = cmd;
        String label = cmd.split(" ")[0].substring(1).toLowerCase(), flag = "";
        if (cmd.split(" ").length > 1) {
            flag = cmd.split(" ")[1].toLowerCase();
        }
        if (flag.equalsIgnoreCase(name)) { flag = "_self_"; }
        ConfigurationNode node = plugin.config.getNode("groups." + group + ".commands.warmup");
        if (node == null) { return false; }
        if (node.getKeys(label) != null) {
            // node has subkeys
            if (node.getKeys(label).contains(flag)) {
                // node has this command + flag registered
                this.label = label;
                this.flag = flag;
                setWarmup(node.getInt(label + "." + flag, 0));
                return true;
            } else if (node.getKeys(label).contains("_all_")) {
                // node has the _all_ subkey
                this.label = label;
                this.flag = "_all_";
                setWarmup(node.getInt(label + "._all_", 0));
                return true;
            } else {
                this.label = "";
                this.flag = "";
                // the command + key is not managed
                return false;
            }
        } else {
            // node has no subkeys
            if (node.getKeys().contains(label)) {
                // node has this command registered
                this.label = label;
                this.flag = null;
                setWarmup(node.getInt(label, 0));
                return true;
            } else if (node.getKeys().contains("_all_")) {
                // node has the _all_ key
                this.label = "_all_";
                this.flag = null;
                setWarmup(node.getInt("_all_", 0));
                return true;
            } else {
                // the command is not managed
                this.label = null;
                this.flag = null;
                return false;
            }
        }
    }

    public void execute() {
        player.chat(cmd);
    }

    public boolean isCooling() {
        if (cooling) {
            if (getCooldown() <= System.currentTimeMillis()) {
                cooling = false;
                cooled = true;
            }
        }
        return cooling;
    }

    public void setCooldown(int secs) {
        cooldown = (secs * 1000) + System.currentTimeMillis();
        cooling = true;
    }

    private long getCooldown() {
        return cooldown;
    }

    public void startCooling() {
        warmed = false;
        if (label != null && flag != null) {
            // label and flag are not null
            setCooldown(plugin.config.getInt("groups." + group + ".commands.cooldown." + label + "." + flag, 0));
        } else if (label != null && flag == null) {
            // label is not null, flag is null
            setCooldown(plugin.config.getInt("groups." + group + ".commands.cooldown." + label, 0));
        }
    }

    public boolean hasCommand(String field, String cmd) {
        String label = cmd.split(" ")[0].substring(1).toLowerCase(), flag = "";
        if (cmd.split(" ").length > 1) {
            flag = cmd.split(" ")[1].toLowerCase();
        }
        if (flag.equalsIgnoreCase(name)) { flag = "_self_"; }
        ConfigurationNode node = plugin.config.getNode("groups." + group + ".commands." + field);
        if (node == null) { return false; }
        if (node.getKeys(label) != null) {
            // node has subkeys
            if (node.getKeys(label).contains(flag)) {
                // node has this command + flag registered
                return true;
            } else if (node.getKeys(label).contains("_all_")) {
                // node has the _all_ subkey
                return true;
            } else {
                // the command + key is not managed
                return false;
            }
        } else {
            // node has no subkeys
            if (node.getKeys().contains(label)) {
                // node has this command registered
                return true;
            } else if (node.getKeys().contains("_all_")) {
                // node has the _all_ key
                return true;
            } else {
                // the command is not managed
                return false;
            }
        }
    }

    public void cancel() {
        this.cmd = null;
        this.label = null;
        this.flag = null;
        this.warming = false;
        this.warmed = false;
    }

    private void play(String msg) {
        player.sendMessage(msg);
    }

    public void setLocale(String loc) {
        plugin.users.setProperty(name + ".locale", loc);
        this.locale = loc;
    }

    public String getLocale() {
        return locale;
    }

    public boolean isCooled() {
        return cooled;
    }

    public void parse(String loc) {
        loc = plugin.config.getString("groups." + group + ".prefix", "[" + group + "] ") + loc;
        if (cmd != null) { loc = loc.replaceAll("<cmd>", cmd); }
        if (label != null) { loc = loc.replaceAll("<label>", label); }
        if (flag != null) { loc = loc.replaceAll("<flag>", flag); }
        int wu = (int) ((getWarmup() - System.currentTimeMillis()) / 1000);
        int cd = (int) ((getCooldown() - System.currentTimeMillis()) / 1000);
        int bd = (int) ((getBlockdown() - System.currentTimeMillis()) / 1000);
        loc = loc.replaceAll("<wu>|<warmup>", "" + wu);
        loc = loc.replaceAll("<cd>|<cooldown>", "" + cd);
        loc = loc.replaceAll("<bd>|<blockdown>", "" + bd);
		loc = loc.replaceAll("&([a-f0-9A-F])", "\u00A7$1");
        play(loc);
    }

    public void finishCooldown() {
        this.cooling = false;
        this.cooled = false;
        this.cmd = null;
        this.label = null;
        this.flag = null;
    }

    public String getGroup() {
        return group;
    }


    public boolean isBlocked() {
        if (blocked) {
            if (getBlockdown() <= System.currentTimeMillis()) {
                blocked = false;
            }
        }
        return blocked;
    }

    private long getBlockdown() {
        return blockdown;
    }

    public void setBlockdown(int secs) {
        blockdown = (secs * 1000) + System.currentTimeMillis();
        blocked = true;
    }

    public void help(String name, String line) {
        line = line.replaceAll("<cmd>", name);
        parse(line);
    }
}
