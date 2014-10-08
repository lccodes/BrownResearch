/**
 * @author TJ Goff  goff.tom@gmail.com
 * @version 1.0.0
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by the
 * Free Software Foundation.
 *
 *
 * A basic client that participates in one or more asynchronously simultaneous
 * DutchFlowerAuction.
 *
 * Given several contracts that can be fulfilled, this client averages the total
 * possible revenue over the sum of all flowers needed to fulfull all of the
 * contracts.  This client then bids 20% below this average value for each flower,
 * while trying to have roughly the same number of each type of flower.
 *
 * This client does not do a good job of planning how it will  fulfill contracts,
 * and is intended only as a simple example.
 */

package jack.clients;

import java.io.*;
import java.net.*;
import java.util.*;


public class DFA_Client_averager {

     static public Socket mySocket = null;
     static public PrintWriter outWriter = null;
     static public BufferedReader inReader = null;
   //server sends Strings that encode information about auction state
     static public String serverMsg = "";

     static public String lastMsg = serverMsg;//filter out repeats so no loops

     //A schedule contains one or more FlowerLots that will be auctioned off
     //in sequence.  If there are multiple schedules, each of these schedules
     //will proceed simultaneously, independently, and asynchronously.  The
     //FlowerLots are sold using descending-price auctions.
     static public Vector<Vector<FlowerLot>> schedules = new Vector<Vector<FlowerLot>>();





    public static void main(String[] args) throws IOException {

        System.out.println("DFA \"Averager\" Client starting");
        String tmp;//used to prevent race condition?

        String serverIP = "127.0.0.1";//IP of host (may be local IP)
        int socketNum = 1300; //default socket
        //Try reading IP and socket number from text file...
        try{
            inReader = new BufferedReader(new FileReader("IP_and_Port.txt"));
            //2 lines in file: serverIP/IP address, and socket number
            serverIP = inReader.readLine();
            socketNum = Integer.valueOf( inReader.readLine() );
            inReader.close();
        }
        catch (IOException e) {
        }

        System.out.println("server IP = " + serverIP + ".  port = " + socketNum);


        //Try to connect using IP address and port number
        try {
            mySocket = new Socket(serverIP, socketNum);
            outWriter = new PrintWriter(mySocket.getOutputStream(), true);
            inReader = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
        }
        catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+serverIP+".");
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: host.");
            System.exit(1);
        }
        System.out.println("Connection to host established");


        //Create and start Listener which receives messages from Server
        DFAClientListener listener = new DFAClientListener(mySocket);
        listener.start();


        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        //Create Vector to store FlowerContracts sent from the server
        Vector<FlowerContract> myContracts = new Vector<FlowerContract>();

        //when contract is added from server, recalculate average value of flowers
        double avgFlowerValue = 0;



      //continue to prompt commands from host and respond
        while (true){

            if(serverMsg.equalsIgnoreCase( lastMsg) )
                continue;//don't re-process same messages over and over

            lastMsg = serverMsg;


            //Message that client should close down
            if(serverMsg.equalsIgnoreCase("END")){
                break;
            }

            //////////////////////////////////////////////////////////////////////////
            else if(serverMsg.equalsIgnoreCase("Send_ID") ){
                   //System.out.print("Enter a unique name: ");
                   outWriter.println( "DFA_Averager_Client" );
               }


            //////////////////////////////////////////////////////////////////////////
               else if( (tmp=serverMsg).startsWith("Add_Contract")){
                   String[] parts = tmp.split(" ");//[0] is "Add_Contract"
                   //parts 1-4 are ints: roses, tulips, lillies, value
                   FlowerContract curr = new FlowerContract(Integer.parseInt(parts[1]),
                                                            Integer.parseInt(parts[2]),
                                                            Integer.parseInt(parts[3]),
                                                            Integer.parseInt(parts[4]));
                   myContracts.add(curr);

                //Recalculate average value per flower over all contracts.
                avgFlowerValue =  findAverageFlowerValue( myContracts );
                System.out.println("I think average flower value is... " + avgFlowerValue);
                System.out.println("FlowerContract Received!");
                   outWriter.println( "Contract_Received" );
            }

            //Auction_State reported by server.  Client may decide to bid!
               else if( (tmp=serverMsg).startsWith("Auction_State")){
                   String[] parts = tmp.split(" ");
                   //parts[0] = "Auction_State
                   //parts[1] = Schedule_ID
                   //parts[2] = Lot_ID
                   //parts[3] = flower_type
                   //parts[4] = lot_count
                   //parts[5] = currPrice
                   //parts[6] = reservePrice

                   //If price is below out model for what flowers cost on average, maybe buy
                   int currPrice = Integer.parseInt(parts[5]);
                   if( currPrice < avgFlowerValue){
                       System.out.println("Thinking about bidding...");

                       Random r = new Random();
                       if( r.nextDouble() < 0.15 ){//a 15% chance of buying per lower price
                           //identify the schedule and lot from which you want to buy...
                           //buy all you can at current price
                           String bid = parts[1]+" "+parts[2]+" "+parts[5]+" "+parts[4];
                           outWriter.println( bid );
                           System.out.println("Bid: " + bid);
                       }
                   }
               }

               else if( !serverMsg.isEmpty() ){
                   //may be redundant, but just repeat most recent message from server
                   System.out.println("Recent Msg from server: <" + serverMsg + ">");
                System.out.println("--------------------------------------------");
               }

        }






            /*


            //////////////////////////////////////////////////////////////////////////
            //Each auction "Schedule" has its own sequence of flower Lots which are
            //auctioned use descending-price (Dutch) auctions.
            else if(fromServer.startsWith("Add_Schedule")){
                String[] parts = fromServer.split(" ");//[0] is "Add_Schedule"

                Vector<FlowerLot> currSchedule = new Vector<FlowerLot>();
                for(int i=1; i < parts.length; i+=5) //start at index 1
                    currSchedule.add( new FlowerLot(parts[i], //type
                                              Integer.parseInt(parts[i+1]), //count
                                              parts[i+2], //lotID
                                              Integer.parseInt(parts[i+3]), //startPrice
                                              Integer.parseInt(parts[i+4]) ) );//reservePrice

                schedules.add( currSchedule );
                System.out.println( "Schedule_Received:" );
                for(int i=0; i < parts.length; i+= 5)
                    System.out.println("Type = " + parts[i] +
                                       ", count = " + parts[i+1] +
                                       ", lotID = " + parts[i+2] +
                                       ", startPrice = " + parts[i+3] +
                                       ", reservePrice = " + parts[i+4]);
            }

            //////////////////////////////////////////////////////////////////////////
            //Each auction hall has a schedule of FlowerLots that it sells.
            else if(fromServer.startsWith("Auction_Prepare")){
                String[] parts = fromServer.split(" ");//[0] is "Auction_Start"

                //information about the current auction
                   String lotID = parts[1];
                   String type = parts[2];
                   int count = Integer.parseInt(parts[3]);
                   int startPrice = Integer.parseInt(parts[4]);
                   System.out.println("Auction starting:\nlotID = " + lotID +
                                      "\nFlower Type = " + type +
                                     "\nFlower count = " + count);
                System.out.println( "Ready" );//indicate that info received, ready to start


                //wait for host to start auction...
                fromServer = inReader.readLine();// message will be "Enter_Bids"


                   //output a bid and quantity
                   System.out.println("avgFlowerValue = " + avgFlowerValue);
                   int bid = (int) Math.round(avgFlowerValue * 0.9);
                int quantity = count;//buy all flowers you can!
                outWriter.println( ""+ bid + " " + quantity );
               }

            //////////////////////////////////////////////////////////////////////////
            //default reaction to message from server
            else{
                   //Prompt user for response
                   System.out.print("Enter response: ");
                   outWriter.println( stdIn.readLine() );
            }
            System.out.println("--------------------------------------------");
        }*/

        //close the connection
        outWriter.close();
        inReader.close();
        stdIn.close();
        mySocket.close();
    }//end main)


    //Average the total value of all contracts over the number of flowers needed
    //to satisfy all of the contracts (without regard to flower type).
    static public double findAverageFlowerValue(Vector<FlowerContract> myContracts){
        //recalculate average value of a flower
        int sumFlowers = 0; //not "sunflowers"...
           int sumValue = 0;
           for(int i=0; i < myContracts.size(); i++){
               sumFlowers += myContracts.get(i).roses +
                             myContracts.get(i).tulips +
                             myContracts.get(i).lillies;
               sumValue += myContracts.get(i).value;
        }
        return  ((double)sumValue) / ((double)sumFlowers);
    }


}//end DFA_Client_averager







//Class used for parsing and organizing flower contracts
class FlowerContract{
    //Types of flowers needed to fulfill the contract
    public int roses;
    public int tulips;
    public int lillies;
    public int value;

    public FlowerContract(int r, int t, int l, int v){
        this.roses = r;
        this.tulips = t;
        this.lillies = l;
        this.value = v;
    }

    //return a String encoding of this FlowerContract. Spaces are delimiters.
    public String toString(){
        return  Integer.toString(roses)+ " " +
                Integer.toString(tulips)+ " " +
                Integer.toString(lillies)+ " " +
                Integer.toString(value);
    }

    //Given a string encoding of a FlowerContract, reconstruct the original
    public void setFromString(String str){
        String[] parts = str.split(" ");
        //for now, assume str is always properly formatted
        roses = Integer.parseInt(parts[0]);
        tulips = Integer.parseInt(parts[1]);
        lillies = Integer.parseInt(parts[2]);
        value = Integer.parseInt(parts[3]);
    }
}//end FlowerContract


//Small class for organizing flowers into lots
class FlowerLot{
    public String lotID;
    public int count;
    public String type;
    public int startPrice;
    public int reservePrice;

    public FlowerLot(String type, int num, String id, int price, int reserve){
        this.count = num;
        this.type = type;
        this.lotID = id;
        this.startPrice = price;
        this.reservePrice = reserve;
    }
}//end FlowerLot







class AuctionSchedule{
    public String scheduleID;

    //tracks the FlowerLots that will be auctioned in sequence
    public Vector<FlowerLot> myLots = new Vector<FlowerLot>();

    //tracks current price per unit in current FlowerLot. Decreases over time.
    public int currPrice;


    public AuctionSchedule(String id, Vector<FlowerLot> sched){
        this.scheduleID = id;

        //copy FlowerLots from "sched" to "myLots"
        for(int i=0; i < sched.size(); i++)
            myLots.add(sched.get(i));
    }
}//end AuctionSchedule





//A Listener thread that is launched once listens for messages from the server.
//Whenever a message is sent from the server, the listener update the Client's
//record of the auction state and print it.
class DFAClientListener extends Thread {
    private Socket socket = null;
    private BufferedReader in;

    public DFAClientListener(Socket s){
        super("DFAClientListener"); //call superclass constructor: Thread(String name)
        socket = s;

        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    //When the socket is created, opens communication loop with client.
    public void run() {
        while(true){

            try{
                String input = in.readLine();//receive messages from client

                //only report changes...
                if( !input.equals(DFA_Client_averager.serverMsg) ){
                    DFA_Client_averager.serverMsg = input;
                    System.out.println("Update: " + input);

                    if(DFA_Client_averager.serverMsg.contains("Agent") &&
                       DFA_Client_averager.serverMsg.contains("bought") )
                        System.out.println("BIG NEWS!!! " + DFA_Client_averager.serverMsg);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                DFA_Client_averager.serverMsg = "END";//tells client to close
                break;
            }
        }
    }//end run()
}//end DFAClientListener
