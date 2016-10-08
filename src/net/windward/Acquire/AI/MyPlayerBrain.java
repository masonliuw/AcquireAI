
package net.windward.Acquire.AI;

import net.windward.Acquire.Units.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The sample C# AI. Start with this project but write your own code as this is a very simplistic implementation of the AI.
 */
public class MyPlayerBrain {
    // bugbug - put your team name here.
    private static String NAME = "Mason Liu";

    // bugbug - put your school name here. Must be 11 letters or less (ie use MIT, not Massachussets Institute of Technology).
    public static String SCHOOL = "GaTech";

    private static Logger log = Logger.getLogger(MyPlayerBrain.class);

    /**
     * The name of the player.
     */
    private String privateName;

    public final String getName() {
        return privateName;
    }

    private void setName(String value) {
        privateName = value;
    }

    private static final java.util.Random rand = new java.util.Random();

    public MyPlayerBrain(String name) {
        setName(!net.windward.Acquire.DotNetToJavaStringHelper.isNullOrEmpty(name) ? name : NAME);
    }

    /**
     * The avatar of the player. Must be 32 x 32.
     */
    public final byte[] getAvatar() {
        try {
            // open image
            InputStream stream = getClass().getResourceAsStream("/net/windward/Acquire/res/MyAvatar.png");

            byte[] avatar = new byte[stream.available()];
            stream.read(avatar, 0, avatar.length);
            return avatar;

        } catch (IOException e) {
            System.out.println("error reading image");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Called when the game starts, providing all info.
     * @param map The game map.
     * @param me The player being setup.
     * @param hotelChains All hotel chains.
     * @param players All the players.
     */
    public void Setup(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
        // get your AI initialized here.
    }

    /**
     * Asks if you want to play the CARD.DRAW_5_TILES or CARD.PLACE_4_TILES special power. This call will not be made
     * if you have already played these cards.
     * @param map The game map.
     * @param me The player being setup.
     * @param hotelChains All hotel chains.
     * @param players All the players.
     * @return CARD.NONE, CARD.PLACE_4_TILES, or CARD.DRAW_5_TILES.
     */
    public int QuerySpecialPowerBeforeTurn(GameMap map, Player me, List<HotelChain> hotelChains,
            List<Player> players) {
        // we randomly decide if we want to play a card.
        // We don't worry if we still have the card as the server will ignore trying to use a card twice.
        for(SpecialPowers p : me.getPowers()){
            if(p.getCard() == SpecialPowers.CARD_DRAW_5_TILES){
                return SpecialPowers.CARD_DRAW_5_TILES;
            }
        }
        /*if (me.getPowers().contains(SpecialPowers.CARD_DRAW_5_TILES)){
            return SpecialPowers.CARD_DRAW_5_TILES;
        }*/
        return SpecialPowers.CARD_PLACE_4_TILES;
    }

    /**
     * Return what tile to play when using the CARD.PLACE_4_TILES. This will be called for the first 3 tiles and is for
     * placement only. Any merges due to this will be resolved before the next card is played. For the 4th tile,
     * QueryTileAndPurchase will be called.
     * @param map The game map.
     * @param me The player being setup.
     * @param hotelChains All hotel chains.
     * @param players All the players.
     * @return The tile(s) to play and the stock to purchase (and trade if CARD.TRADE_2_STOCK is played).
     */
    public PlayerPlayTile QueryTileOnly(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
        Player leader = me;
        PlayerPlayTile playTile = new PlayerPlayTile();
        for (Player p : players){
            if((p.getScore() > leader.getScore() && p != me) || leader == me){
                leader = p;
            }
        }

        List<PlayerTile> potential =  me.getTiles();
        List<PlayerTile> toRemove =  new ArrayList<>();
        for (PlayerTile t: potential){
            if(map.IsTileUnplayable(t)){
                toRemove.add(t);
            }
        }
        for (PlayerTile t: toRemove){
            potential.remove(t);
        }

        double[] weights = new double[potential.size()];
        int maxIndex = 0;
        for(int i=0; i<weights.length; i++){
            weights[i] = evalTilePlacement(potential.get(i), map, me, leader, hotelChains, players, playTile);
            if(weights[i] > weights[maxIndex]){
                maxIndex = i;
            }            
        }
        evalTilePlacement(potential.get(maxIndex), map, me, leader, hotelChains, players, playTile);
        playTile.tile = me.getTiles().size() == 0 ? null : potential.get(maxIndex);

        // we grab a random available hotel as the created hotel in case this tile creates a hotel
        for (HotelChain hotel : hotelChains)
            if (!hotel.isActive() && (playTile.createdHotel == null || playTile.createdHotel.getStartPrice() < hotel.getStartPrice())) {//TODO
                playTile.createdHotel = hotel;
                break;
            }

        return playTile;
    }

    private double evalTilePlacement(PlayerTile target, GameMap map, Player me, Player leader, List<HotelChain> hotelChains, List<Player> players, PlayerPlayTile playTile){
        MapTile[] neighbors = new MapTile[4];
        double baseWorth = me.netWorth();
        double enemyWorth = leader.netWorth();
        double newWorth = baseWorth;
        neighbors[0]= map.getTiles(Math.min(11,target.x + 1), target.y);
        neighbors[1]= map.getTiles(Math.max(0,target.x - 1), target.y);
        neighbors[2]= map.getTiles(target.x, Math.min(8,target.y + 1));
        neighbors[3]= map.getTiles(target.x, Math.max(0,target.y - 1));
        ArrayList<HotelChain> nearby = new ArrayList<>();
        int numSingles=0;
        int numCompanies=0;
        double h = 0.0; //larger is more desirable
        for(MapTile neighbor: neighbors){
            if(neighbor.type == MapTile.TYPE_SINGLE){
                numSingles++;
            } else if (neighbor.type == MapTile.TYPE_HOTEL){
                numCompanies++;
                nearby.add(neighbor.hotel);
            }
        }
        if(numSingles == 0 && numCompanies == 0){ //no impact move
            h = 100 - target.dist;
        } else if (numCompanies > 2){ //merge
            h = 400;
            ArrayList<HotelChain> mergedChains = new ArrayList<>();
            playTile.mergeSurvivor = null;
            for (HotelChain hc: nearby){
                if(playTile.mergeSurvivor != null && hc.getNumTiles() > playTile.mergeSurvivor.getNumTiles()){ //TODO determine survivor in tie
                }
                if(playTile.mergeSurvivor == null || hc.getNumTiles() > playTile.mergeSurvivor.getNumTiles()){
                    playTile.mergeSurvivor = hc;
                }
            }
            for(HotelChain hc: nearby){
                if(hc != playTile.mergeSurvivor){
                    mergedChains.add(hc);
                }
            }

            double[] desperation = new double[players.size()];
            double totalCash = 0.0;
            for(Player p: players){
                totalCash += p.getCash();
            }
            for(int i=0; i<desperation.length; i++){
                desperation[i] = 1 - Math.pow(players.get(i).getCash()/totalCash,.5);
            }
            //TODO bonus considerations
            for (HotelChain hc: mergedChains){
                if(hc.getFirstMajorityOwners().contains(me)){
                    h+= hc.getFirstMajorityBonus()/20;
                } else if(hc.getSecondMajorityOwners().contains(me)){
                    h+= hc.getSecondMajorityBonus()/20;
                } else {
                    h-= 350;
                }
            }

            //TODO net worth considerations

        } else if (numCompanies == 1){ //add
            h = 200;
            List<StockOwner> owners = nearby.get(0).getOwners();
            for (StockOwner owner: owners){
                if(owner.getOwner() == me){
                    h+= .3 * owner.getNumShares() * (nearby.get(0).getStockAt(nearby.get(0).getNumTiles()+1) - nearby.get(0).getStockPrice()) + 50;
                } else if (owner.getOwner() == leader){
                    h-= .2 * owner.getNumShares() * (nearby.get(0).getStockAt(nearby.get(0).getNumTiles()+1) - nearby.get(0).getStockPrice()) - 40;
                } else {
                    h-= .05 * owner.getNumShares() * (nearby.get(0).getStockAt(nearby.get(0).getNumTiles()+1) - nearby.get(0).getStockPrice()) - 10;
                }
            }
        } else{ //found
            h = 300 - target.dist;
        }

        return h;
    }

    public void buyStock(PlayerTurn turn, GameMap map, Player me, int cash, int[] bought, List<HotelChain> hotelChains, List<Player> players, int numRem){
        if(numRem == 0){
            return;
        }
        HashSet<Player> competitors = new HashSet<>();

        double[] weights = new double[hotelChains.size()];
        for(int i = 0; i< hotelChains.size(); i++){
            HotelChain hc = hotelChains.get(i);
            if (!hc.isActive() || hc.getStockPrice() > cash || hc.getNumAvailableShares() <= bought[i]){
                weights[i] = -1000;
            } else {
                List<StockOwner> owners = hc.getOwners();
                int majShares = hc.getFirstMajorityOwners().get(0).getNumShares();
                int minShares = hc.getSecondMajorityOwners().get(0).getNumShares();
                int myShares = bought[i];
                for (StockOwner so : hc.getOwners()){
                    if(so.getOwner() == me && (hc.getFirstMajorityOwners().contains(me) || hc.getSecondMajorityOwners().contains(me))){
                        for(StockOwner so2 : hotelChains.get(i).getOwners()){
                            competitors.add(so2.getOwner());
                        }
                        myShares = so.getNumShares() + bought[i];
                    } else if (so.getOwner() == me){
                        myShares = so.getNumShares() + bought[i];
                    }
                }
                competitors.remove(me);
                int competitorCash = 0;
                for(Player p: competitors){
                    competitorCash += p.getCash();
                }
                double aggressiveness = 2.5*(-.5 + ((double)(cash))/(cash + competitorCash));
                int newCompetitorCash = competitorCash;

                for(StockOwner so: hc.getOwners()){
                    if(!competitors.contains(so.getOwner())){
                        newCompetitorCash += so.getOwner().getCash();
                    }
                }
                double newAggressiveness = 2.5*(-.5 + ((double)(cash))/(cash + newCompetitorCash));

                if(majShares >= myShares && majShares-myShares <= numRem){
                    weights[i] = 200 + 75/Math.min(0.01,Math.abs(newAggressiveness)) + hc.getFirstMajorityBonus()/30 + 3*myShares - 20 * (hc.getStockPrice()/(hc.getStartPrice()+ 800));

                } else if(minShares >= myShares && minShares - myShares <= numRem){
                    weights[i] = 100 + 75/Math.min(0.01,Math.abs(newAggressiveness)) + hc.getSecondMajorityBonus()/30 + 3*myShares - 20 * (hc.getStockPrice()/(hc.getStartPrice()+ 800));
                } else {
                    weights[i] = 100 - 100 * (hc.getStockPrice()/(hc.getStartPrice()+ 800)); 
                }
            }
        }


        int maxIndex= 0;
        for(int i=0; i< weights.length; i++){
            if(weights[i] > weights[maxIndex]){
                maxIndex = i;
            }
        }
        HotelChain best = hotelChains.get(maxIndex);

        turn.getBuy().add(new HotelStock(best,1));
        bought[maxIndex] +=1;
        buyStock(turn, map, me, cash - best.getStockPrice(), bought, hotelChains, players, numRem-1);
    }
    /**
     * Return what tile(s) to play and what stock(s) to purchase. At this point merges have not yet been processed.
     * @param map The game map.
     * @param me The player being setup.
     * @param hotelChains All hotel chains.
     * @param players All the players.
     * @return The tile(s) to play and the stock to purchase (and trade if CARD.TRADE_2_STOCK is played).
     */
    public PlayerTurn QueryTileAndPurchase(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
        //System.out.println(me.getCash());
        PlayerPlayTile template = QueryTileOnly(map, me, hotelChains, players);
        PlayerTurn turn = new PlayerTurn();
        turn.tile = template.tile;
        turn.createdHotel = template.createdHotel;
        turn.mergeSurvivor = template.mergeSurvivor;
        int[] buys = new int[hotelChains.size()];
        int cash = me.getCash();
        if(cash > 2000){
            for(SpecialPowers p : me.getPowers()){
                if(p.getCard() == SpecialPowers.CARD_BUY_5_STOCK){
                    turn.setCard(SpecialPowers.CARD_BUY_5_STOCK);
                    buyStock(turn, map, me, me.getCash(), buys, hotelChains, players, 5);
                    return turn;
                }
            }
        }
        if (cash < 2000 || map.lateGame()){
            for(SpecialPowers p : me.getPowers()){
                if(p.getCard() == SpecialPowers.CARD_FREE_3_STOCK){
                    turn.setCard(SpecialPowers.CARD_FREE_3_STOCK);
                    buyStock(turn, map, me, Math.max(me.getCash(), 10000), buys, hotelChains, players, 3);
                    return turn;
                }
            }
        } 
        /*if(map.lateGame()){
            for(HotelChain hc : hotelChains){
                boolean holdsStock = false;
                for(StockOwner so: hc.getOwners()){
                    if(so.getOwner() == me && hc.getSecondMajorityOwners().get(0).getNumShares() > so.getNumShares()){
                        for(HotelChain hc2: hotelChains){
                            if(hc2.getSecondMajorityOwners().get(0).getNumShares() < hc.getNumShares(me) && hc2.getStockPrice() > 2*hc.getStockPrice() && hc2.getNumAvailableShares() > 0){
                                turn.setCard(SpecialPowers.CARD_TRADE_2_STOCK);
                                turn.getTrade().add(new PlayerTurn.TradeStock(hc, hc2));
                                turn.getTrade().add(new PlayerTurn.TradeStock(hc, hc2));
                            }
                        }
                    }
                }
            }
        }*/
        buyStock(turn, map, me, me.getCash(), new int[hotelChains.size()], hotelChains, players, 3);
        return turn;
        
    }

    /**
     * Ask the AI what they want to do with their merged stock. If a merge is for 3+ chains, this will get called once
     * per removed chain.
     * @param map The game map.
     * @param me The player being setup.
     * @param hotelChains All hotel chains.
     * @param players All the players.
     * @param survivor The hotel that survived the merge.
     * @param defunct The hotel that is now defunct.
     * @return What you want to do with the stock.
     */
    public PlayerMerge QueryMergeStock(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players,
            HotelChain survivor, HotelChain defunct) {
        HotelStock myStock = null;
        for (HotelStock stock : me.getStock())
            if (stock.getChain() == defunct) {
                myStock = stock;
                break;
            }
        if(!map.lateGame()){
            int shares = myStock.getNumShares() - 1;
            double totalCash = 0.0;
            for(Player p: players){
                totalCash += p.getCash();
            }
            double desperation = 1 - Math.pow(me.getCash()/totalCash,.5);
            if(me.getCash() > 5000 && survivor.getStockPrice() > 2 * defunct.getStockPrice()){
                return new PlayerMerge(0, 1, shares);
            } else if (survivor.getStockPrice() > 2 * defunct.getStockPrice()){
                int sell = (int) (shares*desperation);
                int sell2 = (5000 - me.getCash())/defunct.getStockPrice();
                sell = Math.min(sell, sell2);
                return new PlayerMerge(sell, 1, shares-sell);
            }
            return new PlayerMerge(shares, 1, 0);
        } else {
            int shares = myStock.getNumShares();
            double totalCash = 0.0;
            for(Player p: players){
                totalCash += p.getCash();
            }
            double desperation = 1 - Math.pow(me.getCash()/totalCash,.5);
            if(me.getCash() > 5000 && survivor.getStockPrice() > 2 * defunct.getStockPrice()){
                return new PlayerMerge(0, 0, shares);
            } else if (survivor.getStockPrice() > 2 * defunct.getStockPrice()){
                int sell = (int) (shares*desperation);
                int sell2 = (5000 - me.getCash())/defunct.getStockPrice();
                sell = Math.min(sell, sell2);
                return new PlayerMerge(sell, 0, shares-sell);
            }
            return new PlayerMerge(shares, 0, 0);
        }
    }
}