package net.krinsoft.cooldowns.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import net.krinsoft.cooldowns.interfaces.IPlayer;
import net.krinsoft.cooldowns.player.PlayerManager;

/**
 *
 * @author krinsdeath
 */

public class Persister {
	/**
	 * Saves the CoolPlayer to disk
	 */
	public static void save() {
		LinkedList<HashMap<String, IPlayer>> list = PlayerManager.players;
		FileOutputStream file = null;
		ObjectOutputStream out = null;
		File tmp = new File("plugins/Cooldowns/users/users.dat");
		try {
			if (!tmp.exists()) {
				tmp.getParentFile().mkdirs();
				tmp.createNewFile();
			}
			if (tmp.length() > 0) {
				tmp.delete();
				tmp.createNewFile();
			}
			file = new FileOutputStream(tmp);
			out = new ObjectOutputStream(file);
			out.writeObject(list);
			out.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Loads the CoolPlayers from the datafile
	 * @param plugin
	 * Sets up a static reference for the Cool Player
	 */
	public static void load() {
		LinkedList<HashMap<String, IPlayer>> list = PlayerManager.players;
		FileInputStream file = null;
		ObjectInputStream in = null;
		File tmp = new File("plugins/Cooldowns/users/users.dat");
		try {
			if (!tmp.exists()) {
				tmp.getParentFile().mkdirs();
				tmp.createNewFile();
			}
			file = new FileInputStream(tmp);
			in = new ObjectInputStream(file);
			list = (LinkedList<HashMap<String, IPlayer>>) in.readObject();
			in.close();
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}
		PlayerManager.init(list);
	}
}
