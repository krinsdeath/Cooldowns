package net.krinsoft.cooldowns.interfaces;
/**
 *
 * @author krinsdeath
 */

public interface ICommand {

	/**
	 * Gets the handle of this command
	 * @return
	 * The command label and first argument '/label arg'
	 */
	public String getHandle();

	/**
	 * Checks the status of this command
	 * @return
	 * true if it's ready, false if it's not
	 */
	public boolean getStatus();

	/**
	 * Gets the full command line for this command
	 * @return
	 * the full command
	 */
	public String getCommand();

}
