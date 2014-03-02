package jack.scheduler;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import jack.auctions.AuctionBase;

/**
 * EXPIREMENTAL SCHEDULER USED TO IMPLEMENT BUDGET
 * @author lcamery <lcamery@cs.brown.edu> 
 */

/**
 * The Scheduler class represents a general scheduling of auctions as two
 * dependency graphs. One set of dependencies enumerates the starting conditions
 * for a particular auction and the other set enumerates the ending conditions.
 * The auctions are started and stopped by the schedular based on these
 * dependencies.
 */
public class Scheduler {

    /** Adjacency list of an auctions starting dependencies */
    private final Map<Integer, Set<Integer>> startDepends
                                    = new HashMap<Integer, Set<Integer>>();

    /** Adjacency list of an auctions ending dependencies */
    private final Map<Integer, Set<Integer>> endDepends
                                    = new HashMap<Integer, Set<Integer>>();

    /** Lock to synchronize auctions */
    private final Object stateLock = new Object();
    
    /** Complilation of known bidder's budgets */
    private Map<String, Integer> budgets = new HashMap<String, Integer>();

    /**
     * Adds the specified auction to the schedule. If a auction with the same
     * ID has already been added to the schedule then this function does not
     * change the schedule and returns false.
     * @param auction The auction to add to the schedule
     * @return true if the auction is added to the schedule
     */
    public boolean addAuction(int auctionId) {
        if (startDepends.containsKey(auctionId)) {
            return false;
        }

        startDepends.put(auctionId, new HashSet<Integer>());
        endDepends.put(auctionId, new HashSet<Integer>());
        return true;
    }

    /**
     * Adds the specified starting dependency to the schedule. A starting
     * dependency constrains auction1 from starting until auction2 has finished.
     * Both auction1 and auction2 must be present in the schedule or this will
     * throw an IllegalArgumentException.
     * @param auction1 The auction to constrain
     * @param auction2 The auction that must end before other starts
     */
    public void addStartDepend(int auctionId1, int auctionId2) {
        if (!startDepends.containsKey(auctionId1)
                || !startDepends.containsKey(auctionId2)) {
            throw new IllegalArgumentException("no such auctionId");
        }

        if (auctionId1 == auctionId2) {
            throw new IllegalArgumentException("an auction cannot depend on itself");
        }

        startDepends.get(auctionId1).add(auctionId2);
    }

    /**
     * Returns the starting dependencies for the given auctionId. If the
     * auctionId is invalid this function throws an IllegalArgumentException.
     * @param auctionId The unique identifier of an auction
     * @return The set of auctionIds that this auction depends on
     */
    public Set<Integer> getStartDepends(int auctionId) {
        if (!startDepends.containsKey(auctionId)) {
            throw new IllegalArgumentException("no such auctionId");
        }

        return startDepends.get(auctionId);
    }

    /**
     * Adds the specified ending dependency to the schedule. An ending
     * dependency constrains auction1 from being ended until auction2 is
     * endable. Both auction1 and auction2 must be present in the schedule or
     * this will throw an IllegalArgumentException.
     * @param auction1 The auction to constrain
     * @param auction2 The auction that must be endable before the other ends
     */
    public void addEndDepend(int auctionId1, int auctionId2) {
        if (!endDepends.containsKey(auctionId1)
                || !endDepends.containsKey(auctionId2)) {
            throw new IllegalArgumentException("no such auctionId");
        }

        if (auctionId1 == auctionId2) {
            throw new IllegalArgumentException("an auction cannot depend on itself");
        }

        endDepends.get(auctionId1).add(auctionId2);
    }

    /**
     * Returns the ending dependences for the given auctionId. If the
     * auctionId is invalid this function throws an IllegalArgumentException.
     * @param auctionId The unique identifier of an auction
     * @return The set of auctionIds that this auction depends on
     */
    public Set<Integer> getEndDepends(int auctionId) {
        if (!endDepends.containsKey(auctionId)) {
            throw new IllegalArgumentException("no such auctionId");
        }

        return endDepends.get(auctionId);
    }

    /**
     * Returns a topological sorting of the starting dependency graph. This
     * function uses Kahn's algorithm to determine the sort order. It only
     * considered starting dependencies, and does not take ending dependencies
     * into account. The sort that is returned is not necessarily the same order
     * that the final auction will be executed in.
     * @return The topological sort over starting dependencies
     */
    public List<Integer> getTopoSort() {

        // Create a duplicate list of edges

        Map<Integer, Set<Integer>> edges =
            new HashMap<Integer, Set<Integer>>(startDepends);

        // Find all starting nodes and insert them into the queue

        LinkedList<Integer> queue = new LinkedList<Integer>();
        for (Map.Entry<Integer, Set<Integer>> entry : edges.entrySet()) {
            if (entry.getValue().isEmpty()) {
                queue.add(entry.getKey());
            }
        }

        // Kahn's algorithm

        List<Integer> sorted = new ArrayList<Integer>();
        while (!queue.isEmpty()) {
            int node = queue.remove();
            sorted.add(node);

            for (Map.Entry<Integer, Set<Integer>> entry : edges.entrySet()) {
                if (entry.getValue().remove(node)) {
                    if (entry.getValue().isEmpty()) {
                        queue.add(entry.getKey());
                    }
                }
            }
        }

        // Check for cycles

        for (Map.Entry<Integer, Set<Integer>> entry : edges.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                sorted.clear();
                break;
            }
        }

        return sorted;
    }

    /** Prints an adjacency list representation of the schedule. */
    public void dump() {
        for (Map.Entry<Integer, Set<Integer>> entry : startDepends.entrySet()) {
            System.out.format("%d: { ", entry.getKey());
            for (int startDepend : entry.getValue()) {
                System.out.format("%d ", startDepend);
            }
            System.out.format("} { ");
            for (int endDepend : endDepends.get(entry.getKey())) {
                System.out.format("%d ", endDepend);
            }
            System.out.println("}");
        }
    }

    /**
     * This function executes the auction schedule. The procedure for executing
     * the schedule is simple: try to end any endable tasks, start any startable
     * tasks, and repeat. This is done until there are no endable and startable
     * tasks remaining. Any auction that is passed to this function which has
     * not been explicitly added tot he scheduler will be ignored.
     * @param auctions Map of auctions indexed by their unique identification
     * 
     * NOTE: Here is where the budget is implemented
     *
     * TODO: This function does not check that a given schedule can be executed
     * before hand. It would be useful to try and topologically sort the
     * dependency graph beforehand so that we can alert the user to any warnings
     * and avoid
     */
    public void execute(Map<Integer, AuctionBase> auctions) {

        // Set the synchronization object for all auctions in the schedule. This
        // will allow us to synchronize execution of the schedule and individual
        // auctions without the auctions themselves knowing specifically about
        // the schedule that they are being run by.

        for (AuctionBase auction : auctions.values()) {
            auction.setStateLock(stateLock);
        }

        // Create a thread pool to execute the auctions. We use a cached thread
        // pool here because it only gives us as many threads as we need to
        // request in order to execute the schedule without blocking.

        ExecutorService threadPool = Executors.newCachedThreadPool();

        synchronized (stateLock) {
            while (true) {

                // Get the set of endable auctions and try to end them
                Set<AuctionBase> endableAuctions = getEndables(auctions);
                for (AuctionBase auction : endableAuctions) {
                	/**Grab the budgets from the last auction */
                	budgets = auction.getBudgets();
                    auction.tryEnd();
                }

                // Wait for each of those auctions to end. This approach does
                // have some limitations in that while we are waiting, other
                // auctions that were running may become endable, but we will
                // not be able to immediately end them. The primary assumption
                // that goes into this approach is that auctions will generally
                // resolve quickly. If this is not the case the schedule will
                // still be executed correctly, but not necessarily efficiently.

                for (AuctionBase auction : endableAuctions) {
                    auction.waitForEnd();
                }

                // Get the set of startable auctions and execute them

                Set<AuctionBase> startableAuctions = getStartables(auctions);
                for (AuctionBase auction : startableAuctions) {
                	/**Hand the budgets off to the next auction */
                	if(budgets != null){
                		auction.setBudgets(budgets);
                	}
                    threadPool.execute(auction);
                }

                // Now chack if we have finished executing the auction schedule.
                // If there are any auctions that we just started then we have
                // not finished. This check is necessary because the auctions
                // that we just started may not have moved into STATE_RUNNING.
                // Alternatively it may be better to include a waitForStart
                // function in the AuctionBase. That would allow us to just call
                // isEnded here.

                if (startableAuctions.isEmpty() && isEnded(auctions)) {
                    break;
                }

                // Now we block until we get a change in the number of endable
                // auctions. This is really what keys the actions of the
                // schedule. As long as no auctions move into STATE_ENDABLE,
                // then they cannot be ended and no other auctions can be
                // started.

                try {
                    while (endableAuctions.equals(getEndables(auctions))) {
                        stateLock.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Terminate the thread pool

        threadPool.shutdown();
    }

    /**
     * Returns the set of auctions that are endable. An endable auction is not
     * only in STATE_ENDABLE, but also has met all of its ending dependencies.
     * To meet this requirement, each ending dependency must be in either
     * STATE_ENDABLE, STATE_ENDING, or STATE_ENDED.
     * @param auctions Map of auctions indexed by their unique identification
     * @return The set of endable auctions
     */
    private Set<AuctionBase> getEndables(Map<Integer, AuctionBase> auctions) {
        Set<AuctionBase> endables = new HashSet<AuctionBase>();
        for (AuctionBase auction : auctions.values()) {

            // Ignore auctions that are not endable or have not been added to
            // the scheduler.

            if (auction.getState() != AuctionBase.STATE_ENDABLE
                    || !endDepends.containsKey(auction.getId())) {
                continue;
            }

            // Check if the auction has met all of its end dependencies

            boolean canEnd = true;
            for (int id : endDepends.get(auction.getId())) {
                if (!auctions.containsKey(id)
                        || auctions.get(id).getState()
                            < AuctionBase.STATE_ENDABLE) {
                    canEnd = false;
                    break;
                }
            }

            if (canEnd) {
                endables.add(auction);
            }
        }
        return endables;
    }

    /**
     * Returns the set of auctions that are startable. A startable auction is
     * currently in STATE_NEW and has also met all of its starting dependencies.
     * This requires that each starting dependency must be in STATE_ENDED. In
     * addition if there are any starting or ending dependencies of an auction
     * that are not present in the map, then hat auction will never be
     * considered startable.
     * @param auctions Map of auctions indexed by their unique identification
     * @return The set of startable auctions
     */
    private Set<AuctionBase> getStartables(Map<Integer, AuctionBase> auctions) {
        Set<AuctionBase> startables = new HashSet<AuctionBase>();
        for (AuctionBase auction : auctions.values()) {

            // Ignore auctions that have not been started or have not been added
            // to the schduler.

            if (auction.getState() != AuctionBase.STATE_NEW
                    || !startDepends.containsKey(auction.getId())) {
                continue;
            }

            // Check if the auction has met all of its start dependencies

            boolean canStart = true;
            for (int id : startDepends.get(auction.getId())) {
                if (!auctions.containsKey(id)
                        || auctions.get(id).getState()
                            != AuctionBase.STATE_ENDED) {
                    canStart = false;
                    break;
                }
            }

            // In addition check that we *know* about all of this auction's
            // ending dependencies. We do not want to accidentally start an
            // auction that we can never end.

            if (canStart && auctions.keySet().containsAll(
                    endDepends.get(auction.getId()))) {
                startables.add(auction);
            }
        }
        return startables;
    }

    /**
     * Returns true if the all of the auctions that have been started have also
     * been ended. This function alone does not determine if we have completed
     * executing the schedule.
     * @param auctions Map of auctions indexed by their unique identification
     */
    private boolean isEnded(Map<Integer, AuctionBase> auctions) {
        for (AuctionBase auction : auctions.values()) {
            switch (auction.getState()) {
                case AuctionBase.STATE_RUNNING:
                case AuctionBase.STATE_ENDABLE:
                case AuctionBase.STATE_ENDING:
                    return false;
            }
        }
        return true;
    }
}
