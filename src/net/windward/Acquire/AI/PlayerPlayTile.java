/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward some day, and you think this
 * stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire.AI;

import net.windward.Acquire.Units.HotelChain;
import net.windward.Acquire.Units.PlayerTile;

/**
 * A tile play by a player. Base class for PlayerTurn and used for CARD.PLACE_4_TILES for the placement of the first 3 tiles.
 */
public class PlayerPlayTile {

	/**
	 * The tile to play.
	 */
	public PlayerTile tile;

	/**
	 * If the tile played creates a chain, this is the chain.
	 */
	public HotelChain createdHotel;

	/**
	 * If the tile played merges two equally sized chains, this is the surviving chain.
	 */
	public HotelChain mergeSurvivor;
}
