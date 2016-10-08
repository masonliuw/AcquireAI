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

import java.util.ArrayList;
import java.util.List;

/**
 * A hotel chain. Each chain has 0 or 1 instance on the board.
 */
public class HotelChain {

	private String name;

	private List<StockOwner> owners;

	private int startPrice;

	private int numTiles;

	private boolean isActive;

	private boolean isSafe;

	private List<StockOwner> firstMajorityOwners;

	private List<StockOwner> secondMajorityOwners;

	private int stockPrice;

	private int firstMajorityBonus;

	private int secondMajorityBonus;

	private int numAvailableShares;

	public HotelChain() {

		owners = new ArrayList<StockOwner>();
		firstMajorityOwners = new ArrayList<StockOwner>();
		secondMajorityOwners = new ArrayList<StockOwner>();
	}
	
	public int getStockAt(int tiles){
	    int tier = 0;
	    if(tiles == 2){
	        tier = 1;
	    } else if(tiles == 3){
	        tier = 2;
	    } else if(tiles == 4){
            tier = 3;
        } else if(tiles == 5){
            tier = 4;
        } else if(tiles < 11){
            tier = 5;
        } else if(tiles < 21){
            tier = 6;
        } else if (tiles < 31){
            tier = 7;
        } else  if (tiles < 41){
            tier = 8;
        } else {
            tier = 9;
        }
	    
	    tier += (startPrice == 400)? 2 : (startPrice == 300)? 1:0;
	    return 100*tier + 100;
	}

	/**
	 * The name of the hotel chain.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The owners of shares in this chain. Each player (Owner) will occur 0 or 1 time in this list. Shares not in this
	 * list are available.
	 */
	public List<StockOwner> getOwners() {
		return owners;
	}

	public int getNumShares(Player p){
	    for(StockOwner so: owners){
	        if(so.getOwner() == p){
	            return so.getNumShares();
	        }
	    }
	    return 0;
	}
	/**
	 * The start price for this chain. Should be 200, 300, or 400.
	 */
	public int getStartPrice() {
		return startPrice;
	}

	/**
	 * The number of tiles this hotel has.
	 */
	public int getNumTiles() {
		return numTiles;
	}

	/**
	 * True if this chain is active on the board.
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * True if this chain is safe and cannot be merged out of existence.
	 */
	public boolean isSafe() {
		return isSafe;
	}

	/**
	 * All stockholders who get the first majority bonus.
	 */
	public List<StockOwner> getFirstMajorityOwners() {
		return firstMajorityOwners;
	}

	/**
	 * All stockholders who get the second majority bonus. This is the same list as the FirstMajorityOwners if there is
	 * more than 1 FirstMajorityOwner.
	 */
	public List<StockOwner> getSecondMajorityOwners() {
		return secondMajorityOwners;
	}

	/**
	 * The price for a share of this stock.
	 */
	public int getStockPrice() {
		return stockPrice;
	}

	/**
	 * The price for the first majority's bonus.
	 */
	public int getFirstMajorityBonus() {
		return firstMajorityBonus;
	}

	/**
	 * The price for the second majority's bonus.
	 */
	public int getSecondMajorityBonus() {
		return secondMajorityBonus;
	}

	/**
	 * The number of shares still available for sale.
	 */
	public int getNumAvailableShares() {
		return numAvailableShares;
	}

	@Override
	public String toString() {
		return name;
	}

	public static ArrayList<HotelChain> fromXml(Element elemHotels) {

		ArrayList<HotelChain> hotels = new ArrayList<HotelChain>();
		for (Object elemOn : elemHotels.elements("hotel"))
		{
			Element elemHotelOn = (Element) elemOn;
			HotelChain hotel = new HotelChain();
			hotel.name = elemHotelOn.attribute("name").getValue();
			hotel.startPrice = Integer.parseInt(elemHotelOn.attribute("start-price").getValue());
			hotel.numTiles = Integer.parseInt(elemHotelOn.attribute("num-tiles").getValue());
			hotel.isActive = elemHotelOn.attribute("is-active").getValue().toLowerCase().equals("true");
			hotel.isSafe = elemHotelOn.attribute("is-safe").getValue().toLowerCase().equals("true");
			hotel.stockPrice = Integer.parseInt(elemHotelOn.attribute("stock-price").getValue());
			hotel.firstMajorityBonus = Integer.parseInt(elemHotelOn.attribute("first-majority").getValue());
			hotel.secondMajorityBonus = Integer.parseInt(elemHotelOn.attribute("second-majority").getValue());
			hotel.numAvailableShares = Integer.parseInt(elemHotelOn.attribute("num-avail-shares").getValue());
			hotels.add(hotel);
		}

		// can't do owners yet. Need players

		return hotels;
	}

	public static void readOwnersForChains(Element elemAllChains, ArrayList<HotelChain> hotels, ArrayList<Player> players) {
		for (HotelChain chainOn : hotels)
			chainOn.ReadOwnersFromXml(elemAllChains, players);
	}

	private void ReadOwnersFromXml(Element elemAllChains, List<Player> players)
	{
		Element elemChain = null;
		for (Object child : elemAllChains.elements("hotel"))
			if (((Element)child).attribute("name").getValue().equals(name)) {
				elemChain = (Element) child;
				break;
			}
		GetOwners(players, elemChain, "owners", owners);
		GetOwners(players, elemChain, "first-majority", firstMajorityOwners);
		GetOwners(players, elemChain, "second-majority", secondMajorityOwners);
	}

	private void GetOwners(List<Player> players, Element elemChain, String nodeName, List<StockOwner> owners)
	{
		Element elemOwners = elemChain.element(nodeName);
		if (elemOwners != null) {
			for (Object elem : elemOwners.elements("owner")) {
				Element elemOwner = (Element) elem;
				Player ownerPlayer = null;
				for (Player p : players)
				if (p.getGuid().equals(elemOwner.attribute("guid").getValue())) {
					ownerPlayer = p;
					break;
				}
				StockOwner owner = new StockOwner(ownerPlayer, Integer.parseInt(elemOwner.attribute("num-shares").getValue()));
				owners.add(owner);
			}
		}
	}
}
