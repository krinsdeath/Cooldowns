package net.krinsoft.cooldowns.util;

import java.util.logging.Logger;
import net.krinsoft.cooldowns.Cooldowns;

/**
 *
 * @author krinsdeath
 */

public class CoolLogger {
	private enum Level {
		INFO(0),
		WARN(1),
		SEVERE(2);

		private final int level;
		Level(int lv) {
			level = lv;
		}
	}
	
	private Cooldowns plugin;
	private final static Logger LOGGER = Logger.getLogger("Cooldowns");
	private static String PREFIX;

	public CoolLogger() {
	}

	public void setParent(Cooldowns aThis) {
		plugin = aThis;
	}

	/**
	 * Logs a standard message [INFO]
	 * @param msg
	 * the message
	 */
	public void info(String msg) {
		PREFIX = validate(Level.INFO);
		PREFIX = PREFIX.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
		msg = PREFIX + msg;
		LOGGER.info(msg);
	}

	/**
	 * Logs a warning [WARNING]
	 * @param msg
	 * the message
	 */
	public void warn(String msg) {
		PREFIX = validate(Level.WARN);
		PREFIX = PREFIX.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
		msg = PREFIX + msg;
		LOGGER.warning(msg);
	}

	/**
	 * Logs a critical error [SEVERE]
	 * @param msg
	 * the message
	 */
	public void severe(String msg) {
		PREFIX = validate(Level.SEVERE);
		PREFIX = PREFIX.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
		msg = PREFIX + msg;
		LOGGER.severe(msg);
	}

	/**
	 * Returns a string to use as the log prefix
	 * @param lv
	 * The level of the message (INFO, WARN, SEVERE)
	 * @return
	 * the string
	 */
	private String validate(Level lv) {
		String os = "";
		try {
			os = System.getProperty("os.name");
		} catch (SecurityException e) {
		} catch (NullPointerException e) {
		} catch (IllegalArgumentException e) {
		}
		if (os.contains("Windows")) {
			return "[" + plugin.info("name") + "] ";
		} else {
			switch (lv) {
				case INFO:		return "&A[" + plugin.info("name") + "] &F";
				case WARN:		return "&3[" + plugin.info("name") + "] &F";
				case SEVERE:	return "&C[" + plugin.info("name") + "] &F";
			}
		}
		return "[" + plugin.info("name") + "] ";
	}
}
