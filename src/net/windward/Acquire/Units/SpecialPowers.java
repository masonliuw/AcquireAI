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
 * Every player is dealt 5 cards, one of each special power at the start of the game. They can play one on any turn.
 */
public class SpecialPowers {

	private int card;

	/**
	 * This is not a special power. It is used to pass that you are playing no card.
	 */
	public static final int CARD_NONE = 0;
	/**
	 * Instead of purchasing stock, you can draw up to 3 shares of stock for free.
	 */
	public static final int CARD_FREE_3_STOCK = 1;
	/**
	 * You can purchase up to 5 shares of stock this turn instead of the normal 3.
	 */
	public static final int CARD_BUY_5_STOCK = 2;
	/**
	 * You can do up to 3 2:1 trades where you trade in 2 shares of stock and in return receive 1 share of stock. All
	 * shares involved must be for active chains. This is in addition to the 3 shares you can purchase.
	 */
	public static final int CARD_TRADE_2_STOCK = 3;
	/**
	 * Draw 5 tiles at the beginning of this turn. You do not draw any additional tiles until you've been reduced back
	 * down to 5 times.
	 */
	public static final int CARD_DRAW_5_TILES = 4;
	/**
	 * Place 4 tiles on the board this turn. Any merges due to a tile placement are resolved before the next tile is
	 * placed. 4 new tiles are drawn after all 4 are played.
	 */
	public static final int CARD_PLACE_4_TILES = 5;

	public static final String [] cardString = {"NONE", "FREE_3_STOCK", "BUY_5_STOCK", "TRADE_2_STOCK", "DRAW_5_TILES", "PLACE_4_TILES"};

	public SpecialPowers(int card) {
		this.card = card;
	}

	/**
	 * The specific power of this card.
	 */
	public int getCard() {
		return card;
	}

	public String toString() {
		return "" + cardString[card];
	}
}
