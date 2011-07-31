package net.krinsoft.cooldowns.interfaces;

import org.bukkit.util.config.Configuration;

/**
 *
 * @author krinsdeath
 */
public interface IPlayer {

	public IPlayer getOtherHalf();

	public String getName();

	public String getGroup();
	
	public Configuration getLocale();

	public void setLocale(String loc);

}