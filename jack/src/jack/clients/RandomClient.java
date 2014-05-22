/**
 * @author TJ Goff  goff.tom@gmail.com
 * @version 1.0.0
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by the
 * Free Software Foundation.
 *
 * This is a very basic autonomous client.  It bids randomly whenever it thinks the
 * server is asking for a bid.
 */

package jack.clients;

import java.io.*;
import java.net.*;
import java.util.Random;


public class RandomClient
{
    static public Socket mySocket = null;
    static public PrintWriter out = null;
    static public BufferedReader in = null;
    //server sends Strings that encode information about auction state
    static public String serverMsg = "";
    static private Random rand = new Random();

    public static void main(String[] args) throws IOException {
        System.out.println("Auction Client starting");
        String serverIP = "127.0.0.1";//IP of host (may be local IP)
        int socketNum = 1300; //default socket
        //Try reading in the IP and socket number from the text file...
        try {
            in = new BufferedReader(new FileReader("IP_and_Port.txt"));
            //2 lines in file: serverIP/IP address, and socket number
            serverIP = in.readLine();
            socketNum = Integer.valueOf( in.readLine() );
            in.close();
        } catch (IOException e) { }
        System.out.println("server IP = " + serverIP + ".  port = " + socketNum);

        try {
            mySocket = new Socket(serverIP, socketNum);
            out = new PrintWriter(mySocket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+serverIP+".");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: host.");
            System.exit(1);
        }
        System.out.println("Connection to host established");

        //Create and start Listener which receives messages from Server
        SocketListener listener = new SocketListener(mySocket);
        listener.start();

        //Use to read user input
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        //continue to prompt commands from host and respond
        while (true) {
            if(serverMsg.equalsIgnoreCase("END")){
                break; //Special message that client should close down
            } else if (serverMsg.contains("Enter_ID")) {
                int idNum = rand.nextInt(20);//random 0 to 19
                out.println( "Random_Bidder_"+idNum ); //what a terrible name...
                serverMsg = "";
                System.out.println("\n--------------------------------------------\n");
            } else if (serverMsg.contains("Bid")) {
                int bid = rand.nextInt(20);//bid randomly 0 to 19
                out.println( bid ); //what a terrible way to bid...
                serverMsg = "";
                System.out.println("\n--------------------------------------------\n");
            }
        }
        //close the connection
        listener.interrupt();
        out.close();
        in.close();
        stdIn.close();
        mySocket.close();
    }



    /**
     * A Listener thread that is launched once listens for messages from the server.
     * Whenever a message is sent from the server, the listener update the Client's
     * record of the auction state and print it.
     */
    private static class SocketListener extends Thread {
        private Socket socket = null;
        private BufferedReader in;

        public SocketListener(Socket s){
            super("SocketListener"); //call superclass constructor: Thread(String name)
            socket = s;
            try{
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * SocketListener loops forever, listening for messages from host and relaying
         * them to the user.
         */
        public void run() {
            while (true) {
                try {
                    String hostMsg = in.readLine();//receive messages from client
                    if (!hostMsg.equals(RandomClient.serverMsg)) {
                        RandomClient.serverMsg = hostMsg;
                        System.out.print("\n\n\n");
                        if (hostMsg.startsWith("Final_Result:")) {
                            String[] parts = hostMsg.split(" ");
                            for(String part : parts)
                                System.out.println(part);
                            continue;
                        }
                        System.out.print("Host: " + hostMsg + "\n\nClient Msg: ");
                    }
                } catch (IOException e) {
                    RandomClient.serverMsg = "END";//tells client to close
                    break;
                } catch (NullPointerException e){
                    //Typically means the host-client connection is shut down
                    System.exit(0);
                }
            }
        }

    }


}
