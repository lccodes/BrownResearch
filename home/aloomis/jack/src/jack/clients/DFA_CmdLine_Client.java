/**
 * @author TJ Goff  goff.tom@gmail.com
 * @version 1.0.0
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by the
 * Free Software Foundation.
 *
 * A command-line (terminal) driven auction client with
 * specific modifications to fascilitate interaction with
 * DutchFlowerAuctions.  Most of the responses are still
 * entered by the user on the command line, but some
 * message will auto-reply to acknowledge receiving
 * information, and parsing of messages will occur before
 * being displayed to the user
 */

package jack.clients;

import java.io.*;
import java.net.*;
import java.util.Vector;


public class DFA_CmdLine_Client {

    public static void main(String[] args) throws IOException {

        System.out.println("Auction Client starting");

        Socket auctSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        String serverIP = "127.0.0.1";//IP of host (may be local IP)
        int socketNum = 1300; //default socket
        //Try reading in the IP and socket number from the text file...
        try {
            in = new BufferedReader(new FileReader("IP_and_Port.txt"));
            //2 lines in file: serverIP/IP address, and socket number
            serverIP = in.readLine();
            socketNum = Integer.valueOf( in.readLine() );
            in.close();
        } catch (IOException e) {
        }
        System.out.println("server IP = " + serverIP + ".  port = " + socketNum);



        try {
            auctSocket = new Socket(serverIP, socketNum);
            out = new PrintWriter(auctSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(auctSocket.getInputStream()));
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

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromServer;



        //continue to prompt commands from host and respond
        while ((fromServer = in.readLine()) != null){
            //display message from server (no formatting applied in example)
            System.out.println("Server: " + fromServer);

            //command to measure latency in communication between server/client
            if(fromServer.startsWith("Echo")){
                out.println(fromServer);
            }

            /////////////////////////////////////////////////////
            //Message that client should close down
            else if(fromServer.equalsIgnoreCase("End")){
                break;
            }

            /////////////////////////////////////////////////////
            else if(fromServer.equalsIgnoreCase("Send_ID")){
                System.out.print("Enter a unique name: ");
                out.println( stdIn.readLine() );
            }

            /////////////////////////////////////////////////////
            //receive new FlowerContract from server
            else if(fromServer.startsWith("Add_Contract")){
                String[] parts = fromServer.split(" ");//[0] is "Add_Contract"
                //parts 1-4 are ints: roses, tulips, lillies, value
                System.out.println("You received a new FlowerContract:" +
                                    "\nRoses = " + parts[1] +
                                    "\nTulips = " + parts[2] +
                                    "\nLillies = " + parts[3] +
                                    "\nValue = " + parts[4]);
                out.println( "Contract_Received" );
            }

            /////////////////////////////////////////////////////
            //receive new schedule of auctions of FlowerLots from server
            else if(fromServer.startsWith("Add_Schedule")){
                String[] parts = fromServer.split(" ");//[0] is "Add_Schedule"

                System.out.println("Receiving schedule of FlowerLot auctions:");
                for(int i=1; i < parts.length; i+=4) //start at index 1
                    System.out.println( "Type = " + parts[i] +
                                        "\nFlower count = " + parts[i+1] +
                                        "\nLot ID = " + parts[i+2] +
                                        "\nStart price = " + parts[i+3] + "\n");
                out.println( "Schedule_Received" );
            }

            //////////////////////////////////////////////////////////////////////////
            //Each auction hall has a schedule of FlowerLots that it sells.
            else if(fromServer.startsWith("Auction_Prepare")){
                String[] parts = fromServer.split(" ");//[0] is "Auction_Start"

                //information about the current auction
                System.out.println("Next Auction:\nlotID = " + parts[1] +
                                    "\nFlower Type = " + parts[2] +
                                    "\nFlower count = " + parts[3] +
                                    "\nStarting Price = " + parts[4]);
                out.println( "Ready" );//indicate that info received, ready to start


                //wait for host to start auction...
                fromServer = in.readLine();// message will be "Enter_Bids"

                System.out.println("\nAuction Starting");
                System.out.println("Enter: bid quantity");
                out.println( stdIn.readLine() );
            }

            //default reaction to message from server
            else{
                //Prompt user for response
                System.out.print("Enter response: ");
                out.println( stdIn.readLine() );
            }
            System.out.println("--------------------------------------------");
        }

        //close the connection
        out.close();
        in.close();
        stdIn.close();
        auctSocket.close();
    }
}
