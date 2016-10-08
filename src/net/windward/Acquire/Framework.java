/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward some day, and you think this
 * stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire;

import net.windward.Acquire.AI.MyPlayerBrain;

import net.windward.Acquire.AI.PlayerMerge;
import net.windward.Acquire.AI.PlayerPlayTile;
import net.windward.Acquire.AI.PlayerTurn;
import net.windward.Acquire.Units.GameMap;
import net.windward.Acquire.Units.HotelChain;
import net.windward.Acquire.Units.HotelStock;
import net.windward.Acquire.Units.Player;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.*;


public class Framework implements IPlayerCallback {
    private TcpClient tcpClient;
    private MyPlayerBrain brain;
    private String ipAddress = "127.0.0.1";
    //private String ipAddress = "128.61.36.167";
    //private String ipAddress = "143.215.120.56";
    
    private String myGuid;

    private static Logger log = Logger.getLogger(IPlayerCallback.class);

    /**
     * Run the A.I. player. All parameters are optional.
     *
     * @param args I.P. address of server, name
     */
    public static void main(String[] args) throws IOException {
        if (log.isInfoEnabled())
            log.info("***** Acquire II starting *****");

        Framework framework = new Framework(Arrays.asList(args));
        framework.Run();
    }

    private Framework(java.util.List<String> args) {
        brain = new MyPlayerBrain(args.size() >= 2 ? args.get(1) : null);
        if (args.size() >= 1) {
            ipAddress = args.get(0);
        }
        String msg = String.format("Connecting to server %1$s for user: %2$s", ipAddress, brain.getName());
        if (log.isInfoEnabled())
            log.info(msg);
        System.out.println(msg);
    }

    private void Run() throws IOException {
        System.out.println("starting...");

        tcpClient = new TcpClient(this, ipAddress);
        tcpClient.Start();
        ConnectToServer();

        // It's all messages to us now.
        System.out.println("enter \"exit\" to exit program");
        while (true) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String line = in.readLine();
                if (line.equals("exit")) {
                    System.out.println("Exiting program...");
                    tcpClient.abort();
                    break;
                }
            } catch (Exception e) {
                System.out.println("ERROR restarting app (Exception: " + e.getMessage() + " )");
                log.error("restarting run(), Exception: " + e.getMessage());
            }

        }
    }

    public final void StatusMessage(String message) {
        System.out.println(message);
    }

    public final void IncomingMessage(String message) throws DocumentException, IOException {

        try {
            long startTime = System.currentTimeMillis();
            // get the xml - we assume we always get a valid message from the server.
            SAXReader reader = new SAXReader();
            Document xml = reader.read(new StringReader(message));

            String rootName = xml.getRootElement().getName();

            if (rootName.equals("setup")) {
                System.out.println("Received setup message");
                if(log.isInfoEnabled())
                    log.info("Recieved setup message");

                myGuid = xml.getRootElement().attribute("my-guid").getValue();
                Element elemMap = xml.getRootElement().element("map");
	            GameMap map = new GameMap(Integer.parseInt(elemMap.attribute("width").getValue()),
			                            Integer.parseInt(elemMap.attribute("height").getValue()));


	            DataObjects dataSetup = xmlToData(xml);

	            brain.Setup(map, dataSetup.me, dataSetup.hotels, dataSetup.players);

	            // say ready
	            Document docSetup = DocumentHelper.createDocument();
	            Element elem = DocumentHelper.createElement("ready");
	            docSetup.add(elem);
	            tcpClient.SendMessage(docSetup.asXML());
            }

            else if (rootName.equals("query-card")) {
	            DataObjects dataQuery = xmlToData(xml);
	            int card = brain.QuerySpecialPowerBeforeTurn(dataQuery.map, dataQuery.me, dataQuery.hotels,
			            dataQuery.players);

	            // send the selected card back
	            Document docQueryCard = DocumentHelper.createDocument();
	            Element elem = DocumentHelper.createElement("reply");
	            docQueryCard.add(elem);
	            elem.add(DocumentHelper.createAttribute(elem, "cmd", "query-card"));
	            elem.add(DocumentHelper.createAttribute(elem, "msg-id", xml.getRootElement().attribute("msg-id").getValue()));
	            elem.add(DocumentHelper.createAttribute(elem, "card", "" + card));
	            tcpClient.SendMessage(docQueryCard.asXML());
            }

            else if (rootName.equals("query-tile")) {
	            DataObjects dataQueryTile = xmlToData(xml);
	            PlayerPlayTile playTile = brain.QueryTileOnly(dataQueryTile.map, dataQueryTile.me, dataQueryTile.hotels,
			            dataQueryTile.players);

	            // send the selected tile back
	            Document docQueryCard = DocumentHelper.createDocument();
	            Element elem = DocumentHelper.createElement("reply");
	            docQueryCard.add(elem);
	            elem.add(DocumentHelper.createAttribute(elem, "cmd", "query-tile"));
	            elem.add(DocumentHelper.createAttribute(elem, "msg-id", xml.getRootElement().attribute("msg-id").getValue()));

	            if (playTile != null) {
		            if (playTile.tile != null) {
			            elem.add(DocumentHelper.createAttribute(elem, "tile-x", "" + playTile.tile.getX()));
			            elem.add(DocumentHelper.createAttribute(elem, "tile-y", "" + playTile.tile.getY()));
		            }
		            if (playTile.createdHotel != null) {
			            elem.add(DocumentHelper.createAttribute(elem, "created-hotel", playTile.createdHotel.getName()));
		            }
		            if (playTile.mergeSurvivor != null) {
			            elem.add(DocumentHelper.createAttribute(elem, "merge-survivor", playTile.mergeSurvivor.getName()));
		            }
	            }
	            tcpClient.SendMessage(docQueryCard.asXML());
            }

            else if (rootName.equals("query-tile-purchase")) {
	            DataObjects dataQueryTilePur = xmlToData(xml);
	            PlayerTurn playTurn = brain.QueryTileAndPurchase(dataQueryTilePur.map, dataQueryTilePur.me,
			            dataQueryTilePur.hotels, dataQueryTilePur.players);

	            // send the selected card back
	            Document docQueryCard = DocumentHelper.createDocument();
	            Element elem = DocumentHelper.createElement("reply");
	            docQueryCard.add(elem);
	            elem.add(DocumentHelper.createAttribute(elem, "cmd", "query-tile-purchase"));
	            elem.add(DocumentHelper.createAttribute(elem, "msg-id", xml.getRootElement().attribute("msg-id").getValue()));

	            if (playTurn != null) {
		            elem.add(DocumentHelper.createAttribute(elem, "card", "" + playTurn.getCard()));
		            if (playTurn.tile != null) {
			            elem.add(DocumentHelper.createAttribute(elem, "tile-x", "" + playTurn.tile.getX()));
			            elem.add(DocumentHelper.createAttribute(elem, "tile-y", "" + playTurn.tile.getY()));
		            }
		            if (playTurn.createdHotel != null) {
			            elem.add(DocumentHelper.createAttribute(elem, "created-hotel", playTurn.createdHotel.getName()));
		            }
		            if (playTurn.mergeSurvivor != null) {
			            elem.add(DocumentHelper.createAttribute(elem, "merge-survivor", playTurn.mergeSurvivor.getName()));
		            }
		            if (playTurn.getBuy() != null && playTurn.getBuy().size() > 0) {
			            StringBuilder buyStock = new StringBuilder();
			            for (HotelStock stock : playTurn.getBuy())
				            buyStock.append(stock.getChain().getName() + ':' + stock.getNumShares() + ';');
			            elem.add(DocumentHelper.createAttribute(elem, "buy", buyStock.toString()));
		            }
		            if (playTurn.getTrade() != null && playTurn.getTrade().size() > 0) {
			            StringBuilder tradeStock = new StringBuilder();
			            for(PlayerTurn.TradeStock trade : playTurn.getTrade())
			                tradeStock.append(trade.getTradeIn2().getName() + ':' + trade.getGet1().getName() + ';');
			            elem.add(DocumentHelper.createAttribute(elem, "trade", tradeStock.toString()));
		            }
	            }
	            tcpClient.SendMessage(docQueryCard.asXML());
            }

            else if (rootName.equals("query-merge")) {
	            DataObjects dataQueryMerge = xmlToData(xml);

	            HotelChain survivor = null;
	            for (HotelChain hotel : dataQueryMerge.hotels)
	                if (hotel.getName().equals(xml.getRootElement().attribute("survivor").getValue())) {
		                survivor = hotel;
		                break;
	                }
	            HotelChain defunct = null;
	            for (HotelChain hotel : dataQueryMerge.hotels)
		            if (hotel.getName().equals(xml.getRootElement().attribute("defunct").getValue())) {
			            defunct = hotel;
			            break;
		            }

	            PlayerMerge merge = brain.QueryMergeStock(dataQueryMerge.map, dataQueryMerge.me, dataQueryMerge.hotels,
			            dataQueryMerge.players, survivor, defunct);

	            // send the selected card back
	            Document docQueryMerge = DocumentHelper.createDocument();
	            Element elem = DocumentHelper.createElement("reply");
	            docQueryMerge.add(elem);
	            elem.add(DocumentHelper.createAttribute(elem, "cmd", "query-card"));
	            elem.add(DocumentHelper.createAttribute(elem, "msg-id", xml.getRootElement().attribute("msg-id").getValue()));
	            if (merge != null) {
		            elem.add(DocumentHelper.createAttribute(elem, "keep", "" + merge.getKeep()));
		            elem.add(DocumentHelper.createAttribute(elem, "sell", "" + merge.getSell()));
		            elem.add(DocumentHelper.createAttribute(elem, "trade", "" + merge.getTrade()));
	            }
	            tcpClient.SendMessage(docQueryMerge.asXML());
            }

            else if (xml.getRootElement().getName().equals("exit")) {
                System.out.println("Received exit message");
                if (log.isInfoEnabled()) {
                    log.info("Received exit message");
                }
                System.exit(0);

            } else {
                String msg = String.format("ERROR: bad message (XML) from server - root node %1$s", xml.getRootElement().getName());
                log.warn(msg);
                //Trace.WriteLine(msg);
            }

            long turnTime = System.currentTimeMillis() - startTime;
            if (turnTime > 800) {
                System.out.println("WARNING - turn took " + turnTime / 1000 + " seconds");

            }
        } catch (RuntimeException ex) {
            System.out.println(String.format("Error on incoming message. Exception: %1$s", ex));
            ex.printStackTrace();
            log.error("Error on incoming message.", ex);
        }
    }

	private class DataObjects {
		public GameMap map;
		public ArrayList<Player> players;
		public Player me;
		public ArrayList<HotelChain> hotels;
	}

	private DataObjects xmlToData (Document xml) {

		DataObjects rtn = new DataObjects();
		rtn.players = Player.fromXml(xml.getRootElement().element("players"));
		rtn.hotels = HotelChain.fromXml(xml.getRootElement().element("hotels"));
		for (Player plyrOn : rtn.players)
			if (plyrOn.getGuid().equals(myGuid)) {
				rtn.me = plyrOn;
				break;
			}
		rtn.map = GameMap.fromXml(xml.getRootElement().element("map"), rtn.hotels);

		// now the ones that point to each other
		Player.readStockForPlayers(xml.getRootElement().element("players"), rtn.players, rtn.hotels);
		HotelChain.readOwnersForChains(xml.getRootElement().element("hotels"), rtn.hotels, rtn.players);
		return rtn;

	}

    public final void ConnectionLost(Exception ex) throws IOException, InterruptedException {

        System.out.println("Lost our connection! Exception: " + ex.getMessage());

        int delay = 500;
        while (true) {
            try {
                if (tcpClient != null) {
                    tcpClient.Close();
                }
                tcpClient = new TcpClient(this,ipAddress);
                tcpClient.Start();

                ConnectToServer();
                System.out.println("Re-connected");

                return;
            } catch (RuntimeException e) {

                System.out.println("Re-connection fails! Exception: " + e.getMessage());
                Thread.sleep(delay);
                delay += 500;
            }
        }
    }

    private void ConnectToServer() throws IOException {
        try {
            Document doc = DocumentHelper.createDocument();
            Element root = DocumentHelper.createElement("join");
            root.addAttribute("name",brain.getName());
            root.addAttribute("school",MyPlayerBrain.SCHOOL);
            root.addAttribute("language","Java");

            byte[] data = brain.getAvatar();
            if (data != null) {
                Element avatarElement = DocumentHelper.createElement("avatar");
                BASE64Encoder encoder = new BASE64Encoder();
                avatarElement.setText(encoder.encode(data));
                root.add(avatarElement);
            }

            doc.add(root);

            tcpClient.SendMessage(doc.asXML());
        } catch (Exception e) {
            log.warn("ConnectToServer() threw Exception: " + e.getMessage());
        }
    }
}