/**
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by the
 * Free Software Foundation.
 */

package jack.server;

import java.io.File;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jack.scheduler.Scheduler;
import jack.scheduler.SchedulerFactory;





import java.net.*;
import java.sql.Timestamp;
import java.util.*;
import java.io.*;

import jack.auctions.*;
import jack.valuations.*;



/**
 * @author <a href="mailto:goff.tom@gmail.com">TJ Goff</a>
 * @author <a href="mailto:andrew_loomis@brown.edu">Andy Loomis</a>
 * @version 1.0.1
 */

public class AuctionServer
{
    private Scheduler scheduler = null;

    private Map<Integer, AuctionBase> auctions = new HashMap<Integer, AuctionBase>();

    private final Logger logger = Logger.getLogger(AuctionServer.class.getName());

    int port = 1300;

    int maxWaitTime = 10000;



    //Store threads for communicating with each client
    static Vector<ComThread> myThreads = new Vector<ComThread>();
    static Valuation valuator = null;
    static HashMap<String, String> valuationFunctions = new HashMap<String, String>();

    //Settings get default values, but can be set via Config_AuctionServer.txt
    static boolean useLocalIP = true;//default use local IP, else use public IP address
    static int portNum = 1300; //You may need to adjust this for your machine
    static int minNumClients = 2;//default, require 2+ clients
    static int maxNumClients = 100;//default, no more than 100 clients
    static int maxWaitForClients = 600000;//default, max wait for clients (milliseconds)
    static long responseTime = 10000;//wait time (in ms) for clients to respond (default= 10s)
    static boolean fullResponseTime = false;
    static String auctionType = "None";
    static String auctionConfigFile = "None";
    static String valuationType = "None";
    static String valuationConfigFile = "None";
    static String configFileName = "Config_AuctionServer.txt";

    static String serverLogFileName = "log.txt";
    static FileWriter logStream;
    static BufferedWriter logWriter = null;
    static int logVerbosity = 10;//1 logs least, 10 logs most
    static String serverResultsFileName = "results.txt";
    static FileWriter resultsStream;
    static BufferedWriter resultsWriter = null;







    public AuctionServer(String filename) {

        // Initialize the logger and add a log file

        try {
            FileHandler logFile = new FileHandler("server.log");
            Formatter logFormatter = new SimpleFormatter();
            logFile.setFormatter(logFormatter);

            logger.addHandler(logFile);
        } catch (IOException e) {
            logger.warning("Failed in create log file");
        }

        // Load the configuration file

        loadConfig(filename);
    }

    public void run() {

        // Wait for client connections. If we do not get at least one
        // connection, then we cannot continue.

        Vector<ClientHandler> clients = waitForClients();
        if (clients.isEmpty()) {
            logger.info("Failed to receive any connections");
            return;
        }

        // Notify each auction of the clients that will be participating. It is
        // the auctions responsibility to register themselves with each client
        // before they begin and unregister themselves after they have ended.

        for (AuctionBase auction : auctions.values()) {
            auction.setSessionId(1);
            auction.setClients(clients);
        }

        // Not entirely sure how this should work yet, but for the moment, each
        // auction is going to send its "specification" to the bidders. This is
        // effectively notifying them of the scheduler before it is executed.

        for (AuctionBase auction : auctions.values()) {
            auction.sendSpec();
        }

        // XXX: This should not be here--wait 5 secods before executing the
        // schedule. This is really just a nicety especially when dealing with
        // human interfaces.

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Execute the schedule

        scheduler.execute(auctions);

        // Shutdown the sockets

        for (ClientHandler client : clients) {
            client.close();
        }
    }

    private Vector<ClientHandler> waitForClients() {

        // Set up the server socket to listen for clients

        Vector<ClientHandler> clients = new Vector<ClientHandler>();
        ServerSocket serverSocket = null;

        try {
            InetSocketAddress endpoint = new InetSocketAddress(port);
            serverSocket = new ServerSocket();
            serverSocket.bind(endpoint);
            serverSocket.setSoTimeout(1000);
            logger.info("Listening for connections at " + endpoint.toString());

        } catch (IOException e) {
            logger.warning("Failed to bind to address: " + e.getMessage());
            return clients;
        }

        // Wait for connections until we either run out of time or we have
        // reached the max number of clients.

        long endTime = System.currentTimeMillis() + maxWaitTime;
        while (System.currentTimeMillis() < endTime) {

            // Update the terminal with the remaining time

            int remainingTime = (int)(endTime - System.currentTimeMillis());
            logger.fine("" + (remainingTime / 1000) + " seconds remaining");

            try {

                // Wait for a client connection until the socket times out. Once
                // we get a connection, create a handler to deal with
                // communicating to and from the socket.

                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket);
                client.start();
                clients.add(client);

                InetAddress clientAddress = clientSocket.getInetAddress();
                logger.info("Received connection from " + clientAddress.toString());

            } catch (SocketTimeoutException e) {
                continue;

            } catch (IOException e) {
                logger.warning("Error accepting connection: " + e.getMessage());
                break;
            }
        }

        return clients;
    }

    private void loadConfig(String filename) {
        try {

            // Load the xml configuration and get a DOM document object

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(filename));
            doc.getDocumentElement().normalize();

            // Load the schedule

            NodeList schedulerNodes = doc.getElementsByTagName("schedule");
            scheduler = SchedulerFactory.newScheduler(schedulerNodes.item(0));
            //scheduler.dump();

            // Load the auction(s)

            NodeList auctionNodes = doc.getElementsByTagName("auction");
            for (int i = 0; i < auctionNodes.getLength(); ++i) {
                AuctionBase auction = AuctionFactory.newAuction(auctionNodes.item(i));
                auctions.put(auction.getId(), auction);
            }

        // TODO: Break these out
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("usage: AuctionServer configuration.xml");
            return;
        }

        AuctionServer server = new AuctionServer(args[0]);
        server.run();












/*
        //Create log and results file.  Log contains more detailed info, results
        //file should contain information about purchases and final scores.
        try {
            logStream = new FileWriter(serverLogFileName);
            logWriter = new BufferedWriter(logStream);
            resultsStream = new FileWriter(serverResultsFileName);
            resultsWriter = new BufferedWriter(resultsStream);
        }catch (Exception e){//Catch exception if any
            AuctionServer.writeToLogFile("Caught Exception: "+e.getMessage(), 0);
        }

        //Open config file, read parameters and assign setting values
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(configFileName));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;//store line from file
            System.out.println("\n\nImporting settings from " + configFileName);
            while ((strLine = br.readLine()) != null) { //read each line from config file
                if (strLine.isEmpty()) {
                    continue;//skip blank lines
                }
                //split line from config file at colon, possibly followed by whitespace
                String [] parts = strLine.split(":");
                //parts[0] designates setting, parts[1] designates the value to use

                if (parts[0].equalsIgnoreCase("Host_IP")) {
                    //Host_IP can be set as "local" or "public" IP address
                    useLocalIP = parts[1].equalsIgnoreCase("local");
                } else if (parts[0].equalsIgnoreCase("Port_Number")) {
                    portNum = Integer.parseInt(parts[1]);
                } else if (parts[0].equalsIgnoreCase("Min_Number_Clients")) {
                    minNumClients = Integer.parseInt(parts[1]);
                } else if (parts[0].equalsIgnoreCase("Max_Number_Clients")) {
                    maxNumClients = Integer.parseInt(parts[1]);
                } else if (parts[0].equalsIgnoreCase("Max_Wait_For_Clients")) {
                    maxWaitForClients = Integer.parseInt(parts[1]);
                } else if (parts[0].equalsIgnoreCase("Response_Time")) {
                    responseTime = Integer.parseInt(parts[1]);
                } else if (parts[0].equalsIgnoreCase("Full_Response_Time")) {
                    fullResponseTime = Boolean.parseBoolean(parts[1]);
                } else if (parts[0].equalsIgnoreCase("Auction_Type")) {
                    auctionType = parts[1];
                } else if (parts[0].equalsIgnoreCase("Auction_Config_File")) {
                    auctionConfigFile = parts[1];
                } else if (parts[0].equalsIgnoreCase("Valuation_Config_File")) {
                    valuationConfigFile = parts[1];
                } else if (parts[0].equalsIgnoreCase("Valuation_Type")) {
                    valuationType = parts[1];
                } else if (parts[0].equalsIgnoreCase("Server_Log_File")) {
                    serverLogFileName = parts[1];
                } else if (parts[0].equalsIgnoreCase("Server_Results_File")) {
                    serverResultsFileName = parts[1];
                } else if (parts[0].equalsIgnoreCase("Logging_Verbosity")) {
                    logVerbosity = Integer.parseInt(parts[1]);
                }

                // Print the config settings on the console
                for(int i=0; i < parts.length; i++)
                    System.out.print( parts[i] + " ");
                System.out.println();
            }
            in.close();//Close the input stream
        } catch (Exception e) {
            AuctionServer.writeToLogFile("Caught Exception: "+e.getMessage(), 0);
        } finally {
            System.out.println("\n");
        }



        //Find and report host name, and local (or public) IP address
        try {
            InetAddress addr = InetAddress.getLocalHost();//Get local IP
            String hostname = addr.getHostName(); // Get the host name
            String ip = addr.toString();//local IP
            if( !useLocalIP )
                ip = getPublicIP();
            System.out.println("Host name = " + hostname + "\nIP address = " + ip);
        } catch (UnknownHostException e) {
            AuctionServer.writeToLogFile("Caught Exception: "+e.getMessage(), 0);
        }


        //Create socket for server to listen for client connections
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portNum);
        } catch (IOException e) {
            System.err.println("AuctionServer could not listen on port: "+portNum+".");
            AuctionServer.writeToLogFile("AuctionServer could not listen on port: "
                    + portNum + ".\n" + e.getMessage(), 0);
            System.exit(-1);
        }
        System.out.println("Clients should join the auction now...");


        //Listen for new client connections until meet criteria to start game/auction
        socketConnectionListener sListener = new socketConnectionListener(myThreads,
                                                        serverSocket, maxNumClients);
        sListener.start();
        long startTime = System.currentTimeMillis();//Calendar.getInstance().getTimeInMillis();
        boolean timeNoticeGiven = false;
        while (sListener.isAlive() ) {
            try{
                sListener.join(1000);
                if (((System.currentTimeMillis() - startTime) > maxWaitForClients) &&
                        myThreads.size() >= minNumClients && sListener.isAlive()) {
                    //stop listening for new connections
                    sListener.interrupt();
                    serverSocket.close();
                } else {
                    long timeLeft = Math.round(((maxWaitForClients-(System.currentTimeMillis()
                            -startTime))/1000.0));
                    if(timeLeft >= 0){
                        System.out.println("\nWaiting for minimum number of clients. " +
                            "Time remaining: " + timeLeft + "s\nNumber of Clients = " +
                            myThreads.size() + " (" + minNumClients+" needed)");
                    } else if(!timeNoticeGiven) {
                        System.out.println("Maximim wait time reached.  Auction will "+
                                "start when " + minNumClients + " have connected.");
                        timeNoticeGiven = true;
                    }
                }
            } catch(InterruptedException e) {
                System.out.println("Interruption while listening for socket connections");
            }
        }

        //Measure latency with clients
        //System.out.println("\n\n-------------\nTesting latency:");
        //for(int i=0; i < myThreads.size(); i++)
        //    System.out.println("Latency = " + myThreads.get(i).findLatency() ) ;
        clearAllMsgs(myThreads);//clear messages for next communication

        //Prompt all clients for unique IDs before beginning the auction
        promptIdForAllClients(myThreads, 10000);

        //Starting conditions are satisfied to begin the auction.
        System.out.println("\nReady to start with " + myThreads.size() + " agents!\n");
        System.out.println("Generating Valuation functions for each client");

        if (valuationType.equalsIgnoreCase("Contract")) {
            System.out.println("Gonna make contract valuator");
            valuator = new ContractValuation(valuationConfigFile);
            System.out.println("Made contract valuator");
        } else if (valuationType.equalsIgnoreCase("Additive")) {
            valuator = new AdditiveValuation(valuationConfigFile);
        } else if(valuationType.equalsIgnoreCase("Schedule")) {
            valuator = new ScheduleValuation(valuationConfigFile);
        } else {
            System.err.println("Unrecognized valuation type");
            AuctionServer.writeToLogFile("Unrecognized valuation type", 0);
        }

        //Generate a scoring function for each client/thread and send it
        if (valuator != null) {
            for (ComThread currThread : myThreads) {
                String currFunction = valuator.generateScoringFunction();
                System.out.println("Valuation Function = " + currFunction);

                valuationFunctions.put(currThread.getClientID(), currFunction);
                currThread.clearMsgs();//clear messages in preparation for new message
                currThread.setServerMsg("Valuation_Function: " + currFunction);
                writeToLogFile("Client_IP: "+currThread.getClientIP()+", Client_ID: "+
                        currThread.getClientID() + ", Assigned Valuation_Function:"+
                        currFunction, 1);
            }
            waitForAllResponses(myThreads, 10000, false, "Valuation_Received");
        }


        //Create the Auction of the specified mechanism
        Auction myAuction = null;
        if (auctionType.equalsIgnoreCase("FirstPriceAuction")) {
            myAuction = new FirstPriceAuction(myThreads, auctionConfigFile);
        } else if (auctionType.equalsIgnoreCase("SecondPriceAuction")) {
            myAuction = new SecondPriceAuction(myThreads, auctionConfigFile);
        } else if ( auctionType.equalsIgnoreCase("SequentialAuction")) {
            myAuction = new SequentialAuction(myThreads, auctionConfigFile);
        } else if (auctionType.equalsIgnoreCase("SimultaneousAuction")) {
            myAuction = new SimultaneousAuction(myThreads, auctionConfigFile);
        } else if (auctionType.equalsIgnoreCase("AscendingPriceAuction")) {
            myAuction = new AscendingPriceAuction(myThreads, auctionConfigFile);
        } else if (auctionType.equalsIgnoreCase("DescendingPriceAuction")) {
            myAuction = new DescendingPriceAuction(myThreads, auctionConfigFile);
        } else if (auctionType.equalsIgnoreCase("FantasyFootballAuctionDraft")) {
            myAuction = new FantasyFootballAuctionDraft(myThreads, auctionConfigFile);
        } else {//Default auction.  If you make your own auction, replace the default below
            throw new IllegalArgumentException("Unrecognized auction type in "+ configFileName);
        }


        //Run the root auction here (auctions can be nested)
        myAuction.setComThreads(myThreads);//ensure auction has ComThreads to clients
        myAuction.runFullAuction();


        //Get final results from root auction. Each index in vector contains a String
        //which encodes a 3-tuple: "clientID good cost".  Thus, the string in a given
        //index represents a purchase made by the cliend with the specified clientID,
        //in which they purchased the specified good and incurred the specified cost.
        Vector<String> purchases = myAuction.getFinalResult();
        HashMap<String, Vector<String>> clientGoods = new HashMap<String, Vector<String>>();
        HashMap<String, Integer> clientCosts = new HashMap<String, Integer>();
        //initialize the HashMaps
        for (ComThread thread : myThreads) {
            clientGoods.put(thread.getClientID(), new Vector<String>());
            clientCosts.put(thread.getClientID(), new Integer(0));
        }
        for (String purchase : purchases) {
            String[] parts = purchase.split("\\s+");//[0]=clientID, [1]=good, [2]=cost
            if (clientGoods.containsKey(parts[0])) {
                clientGoods.get(parts[0]).add(parts[1]);
                clientCosts.put(parts[0], new Integer(clientCosts.get(parts[0])
                        +Integer.parseInt(parts[2])));
            } else {
                clientGoods.put(parts[0], new Vector<String>());
                clientGoods.get(parts[0]).add(parts[1]);
                clientCosts.put(parts[0], new Integer(Integer.parseInt(parts[2])));
            }
        }

        //calculate valuation score for each client, now that they have a list of goods
        HashMap<String, Double> clientValues = new HashMap<String, Double>();
        for (ComThread client : myThreads) {
            String clientId = client.getClientID();
            clientValues.put(clientId, valuator.getScore(valuationFunctions.get(clientId),
                    clientGoods.get(clientId)));
        }
        //For each client we have list of their goods, their total cost, and their valuation
        //Determine winner(s) and compile final results to send to clients.
        String finalReport = "Final_Result:";
        Vector<ComThread> winners = new Vector<ComThread>();
        double topScore = Double.NEGATIVE_INFINITY;
        for (ComThread client : myThreads) {
            String clientId = client.getClientID();
            double clientScore = clientValues.get(clientId) - clientCosts.get(clientId);
            //determine if winner or tied with winner(s)
            if (clientScore > topScore) {
                topScore = clientScore;
                winners.clear();
                winners.add(client);
            } else if (clientScore == topScore) {
                winners.add(client);
            }

            String goodsStr = "";
            for (String good : clientGoods.get(clientId)){
                goodsStr = goodsStr + "," + good;
            }
            while (!goodsStr.isEmpty() && goodsStr.charAt(0) == ','){
                goodsStr = goodsStr.substring(1);
            }

            //Add record of this client's performance to final report
            finalReport = finalReport + " Agent:" + clientId +
                                ",Goods:[" + goodsStr + "]" +
                                ",Valuation:" + clientValues.get(clientId) +
                                ",Cost:" + clientCosts.get(clientId) +
                                ",Final_Score:" + clientScore ;
        }

        String winMsg = "";
        if( winners.size() >= 2 ) { //tie for best score = valuation - cost
            winMsg = "Tie between Agents[";
            for (ComThread client : winners) {
                winMsg = winMsg + client.getClientID() + ", ";
            }
            winMsg = winMsg.substring(0, winMsg.length()-2) + "]";//trailing ", " -> "]"
        } else if (winners.size() == 1) {
            winMsg = "Winner: " + winners.get(0).getClientID() + ",  Score: " + topScore;
        }

        System.out.println("\n\nFinal Result:\n"+finalReport+"\n\n"+winMsg+"\n");
        AuctionServer.writeToResultsFile(finalReport);
        sendToAllWaitForResponses(myThreads, finalReport, 1000, false, "0");
        sendToAllWaitForResponses(myThreads, winMsg, 2000, false, "0");


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Auction is over.  Press ENTER to close threads and exit.");
        br.readLine();

        try {
            //Alert clients that they can close
            sendToAllClients(myThreads, "END");
            for (int i=myThreads.size()-1; i >= 0; i--) {
                AuctionServer.writeToLogFile("Closing ComThread for client IP:"+
                        myThreads.get(i).getClientIP()+", ID:"+
                        myThreads.get(i).getClientID(), 10);
                myThreads.get(i).closeConnection();
            }
            //Close the server
            serverSocket.close();
            logWriter.close();
            resultsWriter.close();
        } catch(Exception e ) {
            AuctionServer.writeToLogFile(e.getMessage(), 0);
        } finally {
            System.exit(0);
        }
*/
    }//end main()










    ////////////////////////////////////////////////////////////////////////////////
    //UTILITY FUNCTIONS
    ////////////////////////////////////////////////////////////////////////////////
    /**
     * Send a String message to multiple clients.
     * @param threads Vector of ComThread objects that manage communication with clients.
     * @param str String message sent to all clients connections provided in params.
     */
    static public void sendToAllClients(Vector<ComThread> threads, String str){
        for (int i=0; i < threads.size(); i++) {
            threads.get(i).clearMsgs();//clear messages in preparation for new message
            threads.get(i).setServerMsg(str);
        }
    }

    /**
     * Clears all messages stored on ComThreads
     * @param threads Vector of ComThread objects that manage communication with clients.
     */
    static public void clearAllMsgs(Vector<ComThread> threads){
        for(ComThread thread : threads) {
            thread.clearMsgs();
        }
    }

    /**
     * Prompt all threads that do not have a clientID to get a String ID from client
     * if a duplicate ID is returned, append client's IP address to the provided ID.
     * If duplicates still exists, append counters until there is no duplication of IDs.
     * @param Vector of ComThread objects that manage communication with clients.
     * @param timeLimit Long defining time limit (in ms) to wait for client responses.
     */
    static public void promptIdForAllClients(Vector<ComThread> threads, long timeLimit){
        sendToAllWaitForResponses(threads, "Enter_ID", timeLimit, false, "default_name");
        //All clients have responded with an ID.  To ensure unique IDs, append a
        //counter to the end of any duplicates (ex: dup, dup_2, dup_3, dup_4...)
        //Use a hash-table to map ID strings to counts of duplicates
        Hashtable<String, Integer> idCounts = new Hashtable<String, Integer>();
        for (ComThread thread : threads) {
            String currName = thread.getClientMsg();
            if (idCounts.containsKey(currName)) {
                idCounts.put(currName, new Integer(idCounts.get(currName).intValue()+1) );
                currName = currName +"_"+ idCounts.get(currName);
            } else {
                idCounts.put(currName, new Integer(1));
            }
            thread.setClientID(currName);
            writeToLogFile("Client_IP: "+thread.getClientIP()+", Client_ID: "+currName, 5);
        }
    }


    /**
     * Clear past messages between client and host, send message to all clients, and wait for
     * response from all clients.  After specified time limit, non-responses from clients are
     * replaced with default string.
     * @param threads List of client communication sockets to which message will be sent.
     * @param msg The message sent to clients.
     * @param timeLimit After the message is sent to clients, wait this long for responses.
     * @param def Default response substituted for non-responses after time limit reached.
     */
    static public void sendToAllWaitForResponses(Vector<ComThread> threads, String msg,
            long timeLimit, boolean waitFull, String def){
        writeToLogFile("AuctionServer.sendToAllWaitForResponses("+threads.size()+
                " threads, Msg: "+msg +") starting", 5);
        AuctionServer.clearAllMsgs(threads);
        AuctionServer.sendToAllClients(threads, msg);
        AuctionServer.waitForAllResponses(threads, timeLimit, waitFull, def);
        writeToLogFile("AuctionServer.sendToAllWaitForResponses("+threads.size()+
                " threads, Msg: "+msg +") completed", 5);
    }


    /**
     * Wait for all ComThreads to indicate a response from their respectiveClients. If time-limit is
     * exceeded, assign a default response for Clients. If time-limit is exceeded, assign a default
     * response for unresponsive Clients.
     * @param threads Vector of communication threads from which responses are expected.
     * @param timeLimit Clients have a finite time to respond to the host.
     * @param def String substituted for non-responses from clients after time limit reached.
     */
    static public void waitForAllResponses(Vector<ComThread> threads, long timeLimit,
            boolean waitFull, String def){
        writeToLogFile("AuctionServer.waitForAllResponses("+threads.size()+
                " threads, Time_Limit: "+timeLimit +", waitFull: "+waitFull+
                ", Default_Msg: "+def +") starting", 5);
        boolean allResp;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis()-startTime < timeLimit) {
            allResp = true;
            for (ComThread com : threads) {
                if (com.getClientMsg().isEmpty()) {
                    allResp = false;//this client hasn't responded yet
                }
            }
            if(allResp && !waitFull) {
                break; //all clients have responded, and set not to wait full time.
            }
        }
        //check each client's response, and replace non-responses with default
        for (ComThread com : threads) {
            if (com.getClientMsg().isEmpty()) {
                com.setClientMsg(def);
            }
        }
        writeToLogFile("AuctionServer.waitForAllResponses("+threads.size()+
                " threads, Time_Limit: "+timeLimit +", waitFull: "+waitFull+
                ", Default_Msg: "+def +") complete", 5);
    }

    /**
     * @return String representation of the server's public IP address
     */
    static public String getPublicIP(){
        try {
            URL whatismyip = new URL("http://automation.whatismyip.com/n09230945.asp");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                            whatismyip.openStream()));
            String ip = in.readLine(); //you get the IP as a String
            System.out.println(ip);
            return ip;
        } catch(IOException e) {
            AuctionServer.writeToLogFile("Caught Exception: "+e.getMessage(), 0);
        }
        return null;
    }


    /**
     * Open an output file (create it if it doesn't exist), and append the specified string
     * to the end.  This is good for logging activity.
     * @param outFileName The file to which data will be appended.
     * @param data The data appended to the param output file.
     */
    static public void appendToFile(String outFileName, String data){
        try {
            FileWriter out = new FileWriter(outFileName, true);
            out.write(data);
            out.close();
        } catch (IOException e) {
            AuctionServer.writeToLogFile("Caught Exception: "+e.getMessage(), 0);
        }
    }


    /**
     * Given a string, return a hashmap of parameter:value pairs.
     * The format of the string should be parameter:value pairs separated
     * by whitespace, and each pair separated by a colon.
     * @param str String containing "Param:Value" pairs separates by white-space.
     * @return HashMap which maps the String name of a parameter to its value.
     */
    static public HashMap<String,String> getParamValuePairs(String str){
        HashMap<String,String> hash = new HashMap<String,String>();
        String[] parts = str.split("\\s+");//each index contains a param:val pair
        for(int i=0; i < parts.length; i++){
            String[] pair = parts[i].split(":");
            if(pair.length != 2){
                System.out.println("Warning: config file line improper format.\n"+
                                    str + "\nImproper format: " + parts[i]);
                continue;
            }
            hash.put(pair[0], pair[1]);
        }
        return hash;
    }

    /**
     * Create an instance of an Auction object of the specified type.
     * @param auctionType String naming the type of auction to create.
     * @param threads List of client communication sockets to which message will be sent.
     * @param configFile String naming the config file to use for the new Auction.
     * @return
     */
    static public Auction makeAuction(String auctionType, Vector<ComThread> threads,
            String configFile) {
        writeToLogFile("AuctionServer.makeAuction("+auctionType+", "+ threads.size()+
                " threads, Config_File: " + configFile +") starting", 5);
        Auction auct = null;
            /*
        if (auctionType.equalsIgnoreCase("FirstPriceAuction")) {
            auct = new FirstPriceAuction(threads, configFile);
        } else if(auctionType.equalsIgnoreCase("SecondPriceAuction")) {
            auct = new SecondPriceAuction(threads, configFile);
        } else if(auctionType.equalsIgnoreCase("SimultaneousAuction")) {
            auct = new SimultaneousAuction(threads, configFile);
        } else if (auctionType.equalsIgnoreCase("SequentialAuction")) {
            auct = new SequentialAuction(threads, configFile);
        } else if (auctionType.equalsIgnoreCase("AscendingPriceAuction")) {
            auct = new AscendingPriceAuction(threads, configFile);
        } else if (auctionType.equalsIgnoreCase("DescendingPriceAuction")) {
            auct = new DescendingPriceAuction(threads, configFile);
        } else {
            System.err.println("Unrecognized auction type: " + auctionType);
            AuctionServer.writeToLogFile("Unrecognized auction type: "+auctionType, 0);
        }
        writeToLogFile("AuctionServer.makeAuction("+auctionType+", "+ threads.size()+
                " threads, Config_File: " + configFile +") complete", 5);
        */
        return auct;
    }

    /**
     * Write the string to the log file
     * @param str String to be written to log file.
     * @throws IOException
     */
    static public void writeToLogFile(String str, int verbosity){
        if (verbosity > logVerbosity) {
            return; //ignore if logVerbosity ignores low priority msg (high number)
        }
        try {
            java.util.Date date= new java.util.Date();
            logWriter.write(new Timestamp(date.getTime()) + ": " + str);
            logWriter.newLine();
        } catch (IOException e) {
            System.out.println("Warning: Message not written to log file: " + str);
        }
    }

    static public void writeToLogFile(String str){
        int verbosity = 0;//Default to highest priority. Msg WILL be logged.
        writeToLogFile(str, verbosity);
    }

    /**
     * Write the string to the results file, as well as the log file.
     * @param str String to be written to results file.
     * @throws IOException
     */
    static public void writeToResultsFile(String str){
        try {
            writeToLogFile(str, 0);
            resultsWriter.write(str);
            resultsWriter.newLine();
        } catch (IOException e){
            System.out.println("Warning: Message not written to results file: " + str);
        }
    }

}//end of AuctionServer class


/**
 * Thread class used to listen for socket connections with clients and to
 * allocate ComThreads for new client connections as they are found. These
 * threads may be interrupted if the minimum number of clients has been
 * reached and more than a specified amount of time has passed.
 */
class socketConnectionListener extends Thread
{
    private int maxNumClients;
    private Vector<ComThread> threadPool;
    private ServerSocket serverSocket;

    socketConnectionListener(Vector<ComThread> threadPool,
            ServerSocket serverSocket, int maxNumClients) {
        this.serverSocket = serverSocket;
        this.threadPool  = threadPool;
        this.maxNumClients = maxNumClients;
    }

    public void run ( ) {
        try{
            //listen for socket connections until max number or interrupted
            while(threadPool.size() < maxNumClients) {
                //When socket connection made, create a new ComThread dedicated
                //to the client connection
                ComThread currThread = new ComThread( serverSocket.accept() );
                //currThread.start();
                threadPool.add( currThread );

                //update display on server about status of client connections
                if (threadPool.size() == maxNumClients) {
                    System.out.println("Maximum number of clients have connected: "
                            +threadPool.size());
                } else {
                    System.out.println("Client added: " + threadPool.size());
                }
            }
        } catch (IOException e) {
            AuctionServer.writeToLogFile("Caught Exception"+e.getMessage(), 0);
        } finally {
            System.out.println("Closing SocketConnectionListener");
        }
    }
}
