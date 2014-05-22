/**
 * @author TJ Goff  goff.tom@gmail.com
 * @version 1.0.0
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by the
 * Free Software Foundation.
 *
 * This Sequential Auction reads in a config file which
 * contains parameters for multiple sub-auctions, such
 * as FirstPriceAuction and/or SecondPriceAuction.
 *
 * Instances of these auctions are created and run in
 * order, one after the other.  Clients see the outcome
 * of each auction before proceeding to the next
 */
package jack.auctions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import jack.server.*;


public class SequentialAuction implements Auction
{
    Vector<ComThread> threads;//threads that communicate with clients
    Vector<Auction> auctions = new Vector<Auction>();//store sequence of auctons
    String configFile = new String("Config_SequentialAuction.txt");//default config file
    Vector<String> purchases = new Vector<String>();//store records of purchass: "ID good cost"

    /**
     * Constructor
     * @param threads Vector of ComThread objects that communicate with clients.
     * @param configFile File specifying configuration values for the auction.
     */
    public SequentialAuction(Vector<ComThread> threads, String configFile){
        this.threads = threads;
        this.configFile = configFile;
    }

    /**
     * Constructor
     * @param threads Vector of ComThread objects that communicate with clients.
     */
    public SequentialAuction(Vector<ComThread> threads){
        this.threads = threads;
    }

    /**
     * Import a Vector of threads used to communicate with clients
     */
    public void setComThreads(Vector<ComThread> threads) {
        this.threads = threads;
    }

    /**
     * Run the entire auction, and return a string encoding the outcome
     */
    public Vector<String> runFullAuction(){
        setupAuction();
        runAuction();
        //resolveAuction(); //Does nothing for SequentialAuction
        return getFinalResult();
    }

    /**
     * Read in configuration file and setup the auction and sub-auctions
     */
    public void setupAuction(){
        try{
            DataInputStream in = new DataInputStream(new FileInputStream("src/auctions/"+configFile));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            System.out.println("\n\nImporting settings from " + configFile);
            String currLine;
            while ((currLine = br.readLine()) != null) {
                if (currLine.isEmpty()) {
                    continue;//skip blank lines
                }
                //get auction type and configuration file name
                HashMap<String, String> currParams = AuctionServer.getParamValuePairs(currLine);
                //Create an auction and add it to queue
                auctions.add(AuctionServer.makeAuction(currParams.get("Auction_Type"),
                        threads, currParams.get("Config_File")));
            }
            in.close();//Close the input stream
        } catch (Exception e){//Catch exception if any
              System.err.println("Error: " + e.getMessage());
        }
        System.out.println("All auctions have been created");

        //Run first phase of each auction: setupAuction()
        //Record a schedule of the descriptions of each auction auctions
        String schedule = "Sequential Auction Schedule:";
        for (int i=0; i < auctions.size(); i++) {
            auctions.get(i).setupAuction();
            schedule = schedule + " " + auctions.get(i).getDescription() + ".";
        }
        //Send auction schedule to clients
        System.out.println(schedule);
        AuctionServer.sendToAllWaitForResponses(threads, schedule, 10000, false, "0");
    }

    /**
     * Run each of the sub-auctions in sequence
     */
    public void runAuction(){
        for (Auction auction : auctions) {
            Vector<String> subAuctionResults = auction.runFullAuction();
            for(String purchase : subAuctionResults) {
                purchases.add(purchase);
            }
        }
    }

    /**
     * After setting up and running the auction, determine winner and report to clients
     */
    public void resolveAuction(){
        //sub-auctions resolve themselves when the are run.  Nothing to do here
    }

    /**
     * Call after the auction has run to get a list of Strings encoding all purchases
     */
    public Vector<String> getFinalResult() {
        return purchases;
    }

    /**
     * Return a String encoding information about the auction, including sub-auctions
     */
    public String getDescription() {
        String desc = "SequentialAuction [";
        for (int i=0; i < auctions.size(); i++) {
            desc = desc + " " +auctions.get(i).getDescription();
        }
        desc = desc + " ]";
        return desc;
    }

}
