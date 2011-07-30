package net.krinsoft.cooldowns.util;

import java.util.regex.Pattern;

/**
 *
 * @author krinsdeath
 */

public class Messages {
	public final static Pattern COLOR		= Pattern.compile("&([a-fA-F0-9])");
	public final static Pattern COMMAND		= Pattern.compile("(<cmd>|%c)");
	public final static Pattern LABEL		= Pattern.compile("(<label>|%l)");
	public final static Pattern FLAG		= Pattern.compile("(<flag>|%f)");
	public final static Pattern WARMUP		= Pattern.compile("(<wu>|%wu|<warmup>)");
	public final static Pattern COOLDOWN	= Pattern.compile("(<cd>|%cd|<cooldown>)");
}