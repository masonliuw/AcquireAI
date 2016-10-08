/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward some day, and you think this
 * stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire.AI;

/**
 * Players stock transactions in the result of a merge.
 */
public class PlayerMerge {

	private int sell;

	private int keep;

	private int trade;

	public PlayerMerge(int sell, int keep, int trade) {
		this.sell = sell;
		this.keep = keep;
		this.trade = trade;
	}

	/**
	 * How many shares to sell.
	 */
	public int getSell() {
		return sell;
	}

	/**
	 * How many shares to keep.
	 */
	public int getKeep() {
		return keep;
	}

	/**
	 * How many shares to trade in 2:1. For any that can't be traded, they will be sold.
	 */
	public int getTrade() {
		return trade;
	}
}
