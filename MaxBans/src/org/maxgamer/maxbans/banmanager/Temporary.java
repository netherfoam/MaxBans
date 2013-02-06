package org.maxgamer.maxbans.banmanager;

public interface Temporary{
	/**
	 * The time in milliseconds that this will expire. Use System.currentMillis()
	 * @return The time in milliseconds that this will expire. Use System.currentMillis()
	 */
	public long getExpires();
	/**
	 * Convenience method for checking if this has expired.
	 * @return true if it has expired, false if it is still valid.
	 */
	public boolean hasExpired();
}