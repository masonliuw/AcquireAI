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
 * A share of stock in a hotel chain.
 */
public class HotelStock {

	private HotelChain chain;

	private int numShares;

	public HotelStock(HotelChain chain, int numShares)
	{
		this.chain = chain;
		this.numShares = numShares;
	}

	/**
	 * The chain this is a set of stock for.
	 */
	public HotelChain getChain() {
		return chain;
	}

	/**
	 * The number of shares in the chain.
	 */
	public int getNumShares() {
		return numShares;
	}

	/**
	 * The number of shares in the chain.
	 */
	public void setNumShares(int numShares) {
		this.numShares = numShares;
	}

	@Override
	public String toString() {
		return chain + ":" + numShares;
	}
}
