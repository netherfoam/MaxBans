package org.maxgamer.maxbans.commands.bridge;

public interface Bridge{
	/**
	 * Exports all compatible data to another data source.
	 * E.g. Export MaxBans bans -> Vanilla bans
	 */
	public void export() throws Exception;
	/**
	 * Imports all compatible data into this data source.
	 * E.g. Imports Vanilla bans -> MaxBans bans.
	 */
	public void load() throws Exception;
}