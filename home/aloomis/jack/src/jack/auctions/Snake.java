package jack.auctions;

import java.util.Map.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.LinkedList;

import jack.server.*;

/**
 * Snake draft where bidders choose the item rather than the item being tied to the auction, the bidder is tied to the auction
 */
public class Snake extends AuctionBase
{
    /** The maximum amount of time given to bidders after a new bid (ms) */
    private final long MAX_TIMEOUT = 30000;

    /** The minimum amount of time given to bidders after an new bid (ms) */
    private final long MIN_TIMEOUT = 10000;

    /** True if the auction is finished and false otherwise */
    private boolean isFinished = false;

    /** The name of the highest bidder */
    private String highBidder = null;

    /** The value of the highest bid */
    private int highBid = 0;

    /** The value of the current high bidder's budget */
    private int highBudget = 0;

    /** The time that this auction should end */
    private long endTime = 0;

    /** The time to wait before going from endable to ended*/
    private long endDelay = 5000;
    
    /** Complilation of known bidder's budgets */
    private Map<String, Integer> budgets = new HashMap<String, Integer>();

    /** Roster for each bidder */
    private Map<String, LinkedList<String>> rosters = new HashMap<String, LinkedList<String>>();

    /** List of taken players */
    private LinkedList<String> taken = new LinkedList<String>();

    /**
     * Constructs a single ascending auction as part of a draft
     * @param auctionId The unqiue id of this auction within the draft.
     */
    public Snake (int auctionId) {
        super(auctionId);
        putHandler("bid", new BidHandler2());
    }

    @Override
    public void setParams(Map<String, String> params) {
        highBidder = params.get("winner");
	super.setParams(params);
    }

    @Override
    protected void initialize() {
        sendStart();
    }
    
    /**
     * New class to exchange the budget
     * @return HashMap with budgets
     */
    public Map<String, Integer> getBudgets(){
    	return budgets;
    }
    
    /**
     * New class to set the budgets
     */
    public void setBudgets(Map<String, Integer> newBudgets){
    	budgets = newBudgets;
    }

    /**
     * New class to update the budget
     */
    public void updateBudgets(){
        budgets.put(highBidder, 0);
    }
   
     /**
     * New method to exchange the roster
     * @return HashMap with roster
     */
    public Map<String, LinkedList<String>> getRosters(){
    	return rosters;
    }
    
    /**
     * New method to set the rosters
     */
    public void setRosters(Map<String, LinkedList<String>> newRosters){
    	rosters = newRosters;
	for(Map.Entry<String, LinkedList<String>> kv : rosters.entrySet()){
	   if(kv.getKey() != null){
	      for(String player : kv.getValue()){
		taken.add(player);
		}
	   }
	}
    }

    /**
     * New method to update the rosters
     */
    public void updateRosters(){
        if(rosters.containsKey(highBidder)){
		LinkedList<String> templl = rosters.get(highBidder);
		templl.add(Integer.toString(highBid));
		rosters.put(highBidder, templl);
	}else{
		LinkedList<String> ll = new LinkedList<String>();
		ll.add(Integer.toString(highBid));
		rosters.put(highBidder, ll);
	}
    }



    @Override
    protected void resolve() {
        sendStop();

        try {
            Thread.sleep(endDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    protected void idle() {
        if (System.currentTimeMillis() > endTime) {
            tryEndable();
        }
    }

    private void sendStart() {
        endTime = System.currentTimeMillis() + MAX_TIMEOUT;
        long seconds = MAX_TIMEOUT / 1000;
        Map<String, String> args = new HashMap<String, String>();
        args.put("timer", Long.toString(seconds));
        sendMessage("start", args);
    }

    private void sendStatus() {
        long seconds = (endTime - System.currentTimeMillis()) / 1000;
        Map<String, String> args = new HashMap<String, String>();
        args.put("timer", Long.toString(seconds));
        if (highBidder != null) {
            args.put("bidderId", highBidder);
            args.put("bid", Integer.toString(highBid));
        }
        sendMessage("status", args);
    }

    private void sendStop() {
        Map<String, String> args = new HashMap<String, String>();
        if (highBidder != null) {
            args.put("bidderId", highBidder);
            args.put("bid", Integer.toString(highBid));
        }
        sendMessage("stop", args);
    }

    private class BidHandler2 implements MessageHandler {
        public void handle(Map<String, String> args) throws IllegalArgumentException {
            System.out.println("BidHandler::handle()");

            // Verify this message contains the correct keys
            if (!args.containsKey("sessionId") || !args.containsKey("auctionId") ||
                !args.containsKey("bidderId") || !args.containsKey("bid")) {
                throw new IllegalArgumentException("Invalid bid message");
            }

            // Get the required values
            int msgSessionId = Integer.parseInt(args.get("sessionId"));
            int msgAuctionId = Integer.parseInt(args.get("auctionId"));
            String msgBidderId = args.get("bidderId");
            int msgBid = Integer.parseInt(args.get("bid"));

            // Silently ignore this message as it was not meant for us.
            if (msgSessionId != sessionId || msgAuctionId != auctionId) {
                return;
            }        

            // Check for high bid
            if (msgBidderId.equals(highBidder) && !taken.contains(Integer.toString(msgBid))) {
                highBid = msgBid;

                sendStatus();
            }
        }
    }
}
