/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward meet some day, and you think
 * this stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire.Units;

import net.windward.Acquire.TRAP;
import org.dom4j.Element;
import sun.util.locale.StringTokenIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A player in the game.
 */
public class Player {

	private List<PlayerTile> tiles;

	private List<SpecialPowers> powers;

	private int cash;

	private String guid;

	private String name;

	private List<HotelStock> stock;

	private int score;

	private List<Integer> scoreboard;

	public Player() {
		scoreboard = new ArrayList<Integer>();
		tiles = new ArrayList<PlayerTile>();
		powers = new ArrayList<SpecialPowers>();
		stock = new ArrayList<HotelStock>();
	}

	public double netWorth(){
	    int nw = 0;
	    nw += cash*1.25;
	    for(HotelStock s: stock){
	        nw+= s.getNumShares() * s.getChain().getStockPrice();
	    }
	    return nw;
	}
	
	/**
	 * The tiles this player is holding. You only see your own, this will be empty for other players.
	 */
	public List<PlayerTile> getTiles() {
		return tiles;
	}

	/**
	 * The special powers this player is holding. This is just the unplayed cards. This includes cards other players
	 * are holding.
	 */
	public List<SpecialPowers> getPowers() {
		return powers;
	}

	/**
	 * Your cash balance.
	 */
	public int getCash() {
		return cash;
	}

	/**
	 * The unique identifier for this player. This will remain constant for the length of the game (while the Player
	 * objects passed will change on every call).
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * The name of the player.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The stock this player holds.
	 */
	public List<HotelStock> getStock() {
		return stock;
	}

	/**
	 * The score for this player - this game
	 */
	public int getScore() {
		return score;
	}

	/**
	 * The score for this player - previous games.
	 */
	public List<Integer> getScoreboard() {
		return scoreboard;
	}

	@Override
	public String toString() {
		return name + ", Score: " + score;
	}

	/**
	 * Reads all of the data passed in the XML except the Stock property.
	 */
	public static ArrayList<Player> fromXml(Element elemPlayers) {

		ArrayList<Player> players = new ArrayList<Player>();
		for (Object elem : elemPlayers.elements("player"))
		{
			Element elemPlyrOn = (Element) elem;

			Player plyr = new Player();
			plyr.cash = Integer.parseInt(elemPlyrOn.attribute("cash").getValue());
			plyr.guid = elemPlyrOn.attribute("guid").getValue();
			plyr.name = elemPlyrOn.attribute("name").getValue();
			plyr.score = Integer.parseInt(elemPlyrOn.attribute("score").getValue());

			Element elemTiles = elemPlyrOn.element("tiles");
			if (elemTiles != null)
			{
				StringTokenizer tokTiles = new StringTokenizer(elemTiles.getStringValue(), ";");
				while (tokTiles.hasMoreElements())
				{
					String tileOn = tokTiles.nextToken();
					StringTokenizer tokTileOn = new StringTokenizer(tileOn, ":");
					int x = Integer.parseInt(tokTileOn.nextToken());
					int y = Integer.parseInt(tokTileOn.nextToken());
					PlayerTile tile = new PlayerTile(x, y);
					plyr.tiles.add(tile);
				}
			}

			Element elemPowers = elemPlyrOn.element("powers");
			if (elemPowers != null)
			{
				StringTokenizer tokPowers = new StringTokenizer(elemPowers.getStringValue(), ";");
				while (tokPowers.hasMoreElements())
				{
					String pwrOn = tokPowers.nextToken();
					SpecialPowers power = new SpecialPowers(Integer.parseInt(pwrOn));
					plyr.powers.add(power);
				}
			}

			// we can't do stock yet - need the chains for their names

			Element elemScores = elemPlyrOn.element("scoreboard");
			if (elemScores != null)
			{
				StringTokenizer tokScores = new StringTokenizer(elemScores.getStringValue(), ";");
				while (tokScores.hasMoreElements())
					plyr.scoreboard.add(Integer.parseInt(tokScores.nextToken()));
			}

			players.add(plyr);
		}

		return players;
	}

	/**
	 * Reads in the stock info for the players
	 */
	public static void readStockForPlayers(Element elemAllPlayers, ArrayList<Player> players, ArrayList<HotelChain> hotels) {
		for (Player plyrOn : players)
			plyrOn.ReadStockFromXml(elemAllPlayers, hotels);
	}

	private void ReadStockFromXml(Element elemAllPlayers, ArrayList<HotelChain> hotels) {
		Element elemPlayer = null;
		for (Object elem : elemAllPlayers.elements("player"))
			if (((Element) elem).attribute("guid").getValue().equals(guid)) {
				elemPlayer = (Element) elem;
				break;
			}
		Element elemStock = elemPlayer.element("stock");
		if (elemStock == null)
			return;

		StringTokenizer tokStock = new StringTokenizer(elemStock.getStringValue(), ";");
		while (tokStock.hasMoreElements()) {
			String stockOn = tokStock.nextToken().trim();
			if (stockOn.length() == 0)
				continue;
			StringTokenizer tokParts = new StringTokenizer(stockOn, ":");
			String chainName = tokParts.nextToken().trim();
			int numShares = Integer.parseInt(tokParts.nextToken());
			HotelChain chain = null;
			for (HotelChain h : hotels)
				if (h.getName().equals(chainName)) {
					chain = h;
					break;
				}
			HotelStock hotelStock = new HotelStock(chain, numShares);
			stock.add(hotelStock);
		}
	}
}
