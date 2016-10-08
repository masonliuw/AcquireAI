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
 * A single tile on the board.
 */
public class MapTile {

	public HotelChain hotel;
	public int type;

	/**
	 * Nothing on tile, ok to place a tile there.
	 */
	public static final int TYPE_UNDEVELOPED = 1;
	/**
	 * Hotel on this tile.
	 */
	public static final int TYPE_HOTEL = 2;
	/**
	 * Played tile, needs a second tile to become a chain.
	 */
	public static final int TYPE_SINGLE = 3;
	/**
	 * Can never play this tile, would merge 2 safe chains.
	 */
	public static final int TYPE_UNPLAYABLE_MERGE_SAFE = 4;
	/**
	 * Can not play this tile now as it would create a chain and there are no available chains.
	 */
	public static final int TYPE_UNPLAYABLE_NO_AVAIL_CHAINS = 5;

	public MapTile()
	{
		type = TYPE_UNDEVELOPED;
		hotel = null;
	}

	public MapTile(int type)
	{
		this.type = type;
		hotel = null;
	}

	public MapTile(int type, HotelChain hotel)
	{
		this.type = type;
		this.hotel = hotel;
	}

	/**
	 * The type of square.
	 */
	public int getType() {
		return type;
	}

	/**
	 * The type of square.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * If this is a hotel, the chain it is. Setting this will set Type too.
	 */
	public HotelChain getHotel() {
		return hotel;
	}

	/**
	 * If this is a hotel, the chain it is. Setting this will set Type too.
	 */
	public void setHotel(HotelChain hotel) {
		this.hotel = hotel;
		setType(TYPE_HOTEL);
	}
}
