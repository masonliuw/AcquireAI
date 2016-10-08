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
import net.windward.Acquire.Units.HotelStock;
import net.windward.Acquire.Units.SpecialPowers;

import java.util.ArrayList;
import java.util.List;

/**
 * A turn by a player. Used both as a requested turn and a recorded previous action/turn.
 */
public class PlayerTurn extends PlayerPlayTile {

	private int card;

	private List<HotelStock> buy;

	private List<TradeStock> trade;

	public PlayerTurn() {
		card = SpecialPowers.CARD_NONE;
		buy = new ArrayList<HotelStock>();
		trade = new ArrayList<TradeStock>();
	}

	/**
	 * Which card to play this turn. This is one of the StockPurchase.CARD_* static ints.
	 */
	public int getCard() {
		return card;
	}

	/**
	 * Which card to play this turn. This is one of the StockPurchase.CARD_* static ints.
	 */
	public void setCard(int card) {
		this.card = card;
	}

	/**
	 * Stocks to purchase (may be free).
	 */
	public List<HotelStock> getBuy() {
		return buy;
	}

	/**
	 * Stocks sold or traded in 2:1. You can have up to 3 of these.
	 */
	public List<TradeStock> getTrade() {
		return trade;
	}

	public static class TradeStock {

		private HotelChain tradeIn2;

		private HotelChain get1;

		public TradeStock(HotelChain tradeIn2, HotelChain get1) {
			this.tradeIn2 = tradeIn2;
			this.get1 = get1;
		}

		/**
		 * Trade in 2 shares of this chain.
		 */
		public HotelChain getTradeIn2() {
			return tradeIn2;
		}

		/**
		 * To get 1 share of this chain.
		 */
		public HotelChain getGet1() {
			return get1;
		}

		@Override
		public String toString() {
			return tradeIn2 + "=> + get1";
		}
	}
}
