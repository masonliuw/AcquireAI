/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward meet some day, and you think
 * this stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire.Units;

/**
 * Used to track who owns what shares. This is held by a HotelChain object.
 */
public class StockOwner {

	private Player owner;

	private int numShares;

	public StockOwner(Player owner, int numShares) {
		this.owner = owner;
		this.numShares = numShares;
	}

	/**
	 * The owner of these shares. null if they are available for purchased (unowned).
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * The number of shares.
	 */
	public int getNumShares() {
		return numShares;
	}

	@Override
	public String toString() {
		return owner.getName() + ':' + numShares;
	}
}
