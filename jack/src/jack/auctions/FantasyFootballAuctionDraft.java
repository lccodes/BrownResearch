package jack.auctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import jack.server.*;

public class FantasyFootballAuctionDraft implements Auction {

    private final int BUDGET = 200;
    private Vector<ComThread> clients;
    private String config;

    /** The currently registered managers, indexed by name. */
    private Map<String, Manager> managers = new HashMap<String, Manager>();

    /** The sequence of auctions that will run in this draft. */
    private Vector<AuctionBase> auctions = new Vector<AuctionBase>();

    public FantasyFootballAuctionDraft(Vector<ComThread> clients, String config) {
        this.clients = clients;
        this.config = config;
    }

    public void setComThreads(Vector<ComThread> clients) {
        this.clients = clients;
    }

    public Vector<String> runFullAuction() {
        System.out.println("FantasyFootballAuctionDraft::runFullAuction()");
        setupAuction();

        if (clients.size() == 0) {
            System.err.println("ERROR: Requires at least one client to run");
            return new Vector<String>();
        }

        for (AuctionBase  auction : auctions) {
            auction.run();
        }

        return new Vector<String>();
    }

    public void setupAuction() {
        System.out.println("FantasyFootballAuctionDraft::setupAuction()");

        /*
        FileInputStream in = null;
        try {
            in = new FileInputStream(config);

            Properties properties = new Properties();
            properties.load(in);


        } catch (FileNotFoundException e) {
            System.out.println("Failed to load configuration: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error reading configuration: " + e.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
        }

        auctions.add(new Registration(1, 0, clients));
        for (int i = 1; i < 20; ++i) {
            auctions.add(new FantasyFootballAuction(1, i, clients));
        }
        */
    }

    public void runAuction() {
        System.out.println("FantasyFootballAuctionDraft::runAuction()");
    }

    public void resolveAuction() {
        System.out.println("FantasyFootballAuctionDraft::resolveAuction()");
    }

    public Vector<String> getFinalResult() {
        System.out.println("FantasyFootballAuctionDraft::getFinalResult()");
        return new Vector<String>();
    }

    public String getDescription() {
        System.out.println("FantasyFootballAuctionDraft::getDescription()");
        return new String();
    }

    private class Registration extends AuctionBase {

        private int numBidders = 0;

        public Registration(int auctionId) {
            super(auctionId);
            putHandler("bidder", new BidderHandler());
        }

        @Override
        public void setParams(Map<String, String> params) {}

        private class BidderHandler implements MessageHandler {
            public void handle(Map<String, String> args)  throws IllegalArgumentException {

                // Verify this message contains the correct keys

                if (!args.containsKey("sessionId") || !args.containsKey("bidderId")) {
                    throw new IllegalArgumentException("Invalid bid message");
                }

                // Get the required values

                int msgSessionId = Integer.parseInt(args.get("sessionId"));
                String bidderId = args.get("bidderId");

                // Silently ignore this message as it was not meant for us.

                if (msgSessionId != sessionId) {
                    return;
                }

                Manager manager = new Manager(bidderId, BUDGET);
                managers.put(manager.getName(), manager);

                Map<String, String> retArgs = new HashMap<String, String>();
                retArgs.put("bidderId", manager.getName());
                retArgs.put("budget", Integer.toString(manager.getBudget()));
                sendMessage("bidder", retArgs);

                numBidders += 1;
                if (numBidders == 2) {

                    Random rand = new Random();
                    String[] positions = {"QB", "RB", "WR", "TE", "DEF", "K"};

                    for (int i = 0; i < 20; ++i) {
                        Map<String, String> playerArgs = new HashMap<String, String>();
                        playerArgs.put("sessionId", Integer.toString(sessionId));
                        playerArgs.put("auctionId", Integer.toString(i + 1));
                        playerArgs.put("name", "Player" + Integer.toString(i + 1));
                        playerArgs.put("position", positions[rand.nextInt(positions.length)]);
                        playerArgs.put("value", Integer.toString(rand.nextInt(50)));

                        for (ClientHandler client : clients) {
                            client.sendMessage("auction " + Registration.toString(playerArgs));
                        }

                        /*
                        for (ComThread client : clients) {
                            client.setServerMsg("auction " + Registration.toString(playerArgs));
                        }
                        //XXX: ClientThread needs to be rewritten so than messages do not
                        //overwrite each other.
                        try { Thread.sleep(50); } catch (InterruptedException e) {}
                        */
                    }

                    tryEndable();
                }
            }
        }
    }

    /**
     *  The manager class represents the current state of a manager/bidder in
     *  the fantasy football auction draft.
     */
    public class Manager {

        /** The name of this manager. */
        final private String name;

        /** The remaining budget of this manager. */
        private int budget;

        /** The current value of this manager's team. */
        private int value;

        /**
         * Constructs a manager with the specified name and budget.
         * @param name The name of the this manager
         * @param budget The starting budget of this manager
         */
        public Manager(String name, int budget) {
            this.name = name;
            this.budget = budget;
            this.value = 0;
        }

        /** @return The name of this manager. */
        public final String getName() {
            return name;
        }

        /** @return The remaining budget of this manager. */
        public int getBudget() {
            return budget;
        }

        /** @return The current value of this manager's team. */
        public int getValue() {
            return value;
        }
    }
}
