package net.krinsoft.cooldowns.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.krinsoft.cooldowns.interfaces.IPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author krinsdeath
 */

public class PlayerManager {
	protected static HashMap<String, IPlayer> warm = new HashMap<String, IPlayer>();
	protected static HashMap<String, IPlayer> cool = new HashMap<String, IPlayer>();

	public static LinkedList<HashMap<String, IPlayer>> players = null;
	public static List<Player> online = new ArrayList<Player>();

	public static void init(LinkedList<HashMap<String, IPlayer>> list) {
		players = new LinkedList<HashMap<String, IPlayer>>();
		if (list == null) {
			players.add(warm);
			players.add(cool);
			return;
		}
		for (HashMap<String, IPlayer> item : list) {
			for (String key : item.keySet()) {
				if (item.get(key) instanceof WarmPlayer) {
					System.out.println("Loading WarmPlayers");
					warm.putAll(item);
					break;
				}
				if (item.get(key) instanceof CoolPlayer) {
					System.out.println("Loading CoolPlayers");
					cool.putAll(item);
					break;
				}
				System.out.println("Couldn't match class");
			}
		}
		players.add(warm);
		players.add(cool);
	}

	public static WarmPlayer getWarmPlayer(String name) {
		return (WarmPlayer) warm.get(name);
	}

	public static CoolPlayer getCoolPlayer(String name) {
		return (CoolPlayer) cool.get(name);
	}

	public static boolean getPlayer(String name) {
		if (cool.get(name) != null) {
			return true;
		} else {
			return false;
		}
	}

	public static void addPlayer(String name) {
		if (warm.get(name) == null) {
			warm.put(name, new WarmPlayer(name));
			cool.put(name, new CoolPlayer(name));
		}
	}

	public static void setOnline(Player player) {
		online.add(player);
	}
	
	public static void setOffline(Player player) {
		online.remove(player);
	}

	public static List<Player> online() {
		return online;
	}

}
