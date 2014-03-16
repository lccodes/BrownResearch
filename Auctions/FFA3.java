package jack.auctions;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import jack.server.*;

/**
 * FFA3 works with the expiremental scheduler
 */
public class FFA3 extends AuctionBase
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

    /**
     * Constructs a single ascending auction as part of a draft
     * @param auctionId The unqiue id of this auction within the draft.
     */
    public FFA3 (int auctionId) {
        super(auctionId);
        putHandler("bid", new BidHandler2());
    }

    @Override
    public void setParams(Map<String, String> params) {
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
        budgets.put(highBidder, (highBudget-highBid));
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
            
            //Finds this Bidder's budget or assumes max budget
            /** Budgets start at 200 */
            int currentBudget = budgets.get(msgBidderId);
            if(currentBudget == null){
            	budgets.put(msgBidderId, 200);
            	currentBudget = 200;
            }else{
                currentBudget = budgets.get(msgBidderId);
            }

            }

            // Check for high bid
            if (highBid < msgBid && msgBid <= currentBudget) {
                highBidder = msgBidderId;
                highBid = msgBid;
                highBudget = currentBudget;

                // Increase the end time if necessary
                long currTime = System.currentTimeMillis();
                if (endTime - currTime < MIN_TIMEOUT) {
                    endTime = currTime + MIN_TIMEOUT;
                }

                sendStatus();
            }
        }
    }
}
