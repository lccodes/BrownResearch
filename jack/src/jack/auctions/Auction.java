/**
 * @author TJ Goff  goff.tom@gmail.com
 * @version 1.0.0
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by the
 * Free Software Foundation
 *
 * This interface defines common functionality for Auctions in which agents/clients
 * bid for goods.
 */

package jack.auctions;

import java.util.Vector;

import jack.server.ComThread;


public interface Auction {

    /**
     * Assign and store a Vector of ComThreads that enable socket communication with clients
     */
    void setComThreads(Vector<ComThread> threads);

    /**
     * Run every step auction, and return a String encoding the final result.
     * @return String which encodes information about the outcome of the auction.
     */
    Vector<String> runFullAuction();


    //Functions for running segments of the auction.  Typically, they should be called in order:
    /**
     * Read in configuration paramaters from file and setup the auction
     */
    void setupAuction();

    /**
     * Send information about auction to clients and prompt them for bids.
     */
    void runAuction();

    /**
     * After setting up and running the auction, determine winner and report to clients
     */
    void resolveAuction();

    /**
     * After an auction is completed, call this method to generate a string which
     * encodes information about the final outcome of the auction
     * @return String which encodes information about the outcome of the auction.
     */
    Vector<String> getFinalResult();

    /**
     * Generate a description of the auction (should be sent to clients before
     * prompting them to make decisions).
     * @return formatted String which encodes a description of auction, including
     * any sub-auctions.
     */
    String getDescription();
}
