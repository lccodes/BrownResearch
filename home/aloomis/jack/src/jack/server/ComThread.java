/**
 * @author TJ Goff  goff.tom@gmail.com
 * @version 1.0.0
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License version 2.1 as published by the
 * Free Software Foundation.
 *
 * ComThread manages asynchronous, bidirectional socket communication between a
 * client and the server.
 *
 * The ComThread class initializes a SocketListener thread,
 * which is dedicated to incoming messages from the client.  This leaves the
 * ComThread's main thread with the task of sending messages from the server.
 *
 * Client participate in auctions running on the host, and messages between the host
 * and client are processed through these threads.
 */

package jack.server;

import java.net.*;
import java.util.Vector;
import java.util.Calendar;
import java.io.*;

import jack.auctions.AuctionBase;

public class ComThread
{
    private Socket socket = null;
    private String clientID = "";//unique ID for assigned client
    private String serverMsg = "";//latest message from server to client
    private long serverMsgTimeStamp = 0;//stamp when messages sent to client
    private String clientMsg = "";//latest message from client to server

    private long latency = 0;//last measure of server/client latency

    //Threads that interface with socket to communicate with client
    public Listener listener;
    public Sender sender;

    private Vector<AuctionBase> auctions;

    public ComThread(Socket s) {
        socket = s;
        //Initialize the two threads that will do the communicating
        listener = new Listener(socket, this);
        sender = new Sender(socket, this);

        auctions = new Vector<AuctionBase>();

        //Start the communication threads
        listener.start();
        sender.start();
    }

    public void register(AuctionBase auction) {
        auctions.add(auction);
    }

    public void unregister(AuctionBase auction) {
        auctions.remove(auction);
    }

    //Resets messages to/from server to empty strings
    public void clearMsgs() {
        serverMsg = "";
        clientMsg = "";
    }

    //Used by associated SocketListener to record message(s) from client
    public void setClientMsg(String msg) {
        clientMsg = msg;
        AuctionServer.writeToLogFile("Client IP:"+this.getClientIP()+
                ", ID: "+this.getClientID()+", setClientMsg("+msg+")", 10);

        for (AuctionBase auction : auctions) {
            auction.queueMessage(msg);
        }
    }

    //Access client's latest response (may be blank if client hasn't responded)
    public String getClientMsg() {
        return clientMsg;
    }

    //Record server's last message sent to client
    public void setServerMsg(String msg){
        clientMsg = "";//clear any prior client response to avoid confusion
        serverMsg = msg;
        //Change timestamp to alert Sender that this is a new message to client
        serverMsgTimeStamp = Calendar.getInstance().getTimeInMillis();
        AuctionServer.writeToLogFile("Client IP:"+this.getClientIP()+
                ", ID: "+this.getClientID()+", setServerMsg("+msg+")", 10);
    }

    //Access the last message sent to the client from the server
    public String getServerMsg(){
        return serverMsg;
    }

    //Access time-stamp of last message from server
    public long getServerMsgTimeStamp(){
        return serverMsgTimeStamp;
    }

    //Return latency of communication with client in millisecond
    public long findLatency(){
        clearMsgs();
        java.util.Date startTime = new java.util.Date();
        setServerMsg("Echo");//special cmd to client: echo msg back to server
        java.util.Date endTime = new java.util.Date();
        latency = endTime.getTime() - startTime.getTime();
        clearMsgs();
        return latency;
    }

    //Access clientID String that uniquely identifies the client
    public String getClientID(){
        return clientID;//if never specified, "" is empty default
    }

    public void setClientID(String id){
        clientID = id;
    }

    //Return a String representation of the client's IP address
    public String getClientIP(){
        if(socket != null)
            return socket.getInetAddress().toString();
        return null;//there is no connected client
    }

    //Close connection with client. Send message instructing client to close.
    public void closeConnection(){
        AuctionServer.writeToLogFile("Client IP:"+this.getClientIP()+
                ", ID: "+this.getClientID()+", closing socket connection", 10);
        try {
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Listener is a class used within ComThread to listen for messages from
     * the ComThread's client which may be sent asynchronously from messages
     * sent by the server to the client.
     */
    private class Listener extends Thread
    {
        private Socket socket = null;
        private BufferedReader in;

        //Stores reference to ComThread with which this SocketListener works.
        //Use reference to update the String clientMsg stored in ComThread.
        private ComThread com;

        public Listener(Socket s, ComThread com){
            super("SocketListener"); //call superclass constructor
            socket = s;
            //this.com should hold a reference to parent ComThread
            this.com = com;

            try{
                in = new BufferedReader(new
                        InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //When socketListener is created, loop to listen for client messages.
        public void run() {
            while(true) {
                try{
                    String clientMsg = in.readLine();//get messages from client
                    if (!clientMsg.isEmpty()) {
                        com.setClientMsg(clientMsg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //Close connection with client.
        public void closeConnection(){
            try {
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Sender is used within ComThread to send messages from the server to a
     * client.  Outgoing messages are sent asynchronously from incoming
     * messages.
     */
    private class Sender extends Thread
    {
        private Socket socket = null;
        private PrintWriter out;

        //Stores reference to ComThread with which this SocketSender works.
        //Use this reference to check for new messages to send to the client.
        private ComThread com;

        //ComThread associates a timeStamp with each message.
        long timeStamp = 0;//used to detect new messages from Server to client.

        public Sender(Socket s, ComThread com){
            super("SocketSender"); //call superclass constructor
            socket = s;
            //this.com should hold a reference to the parent ComThread
            this.com = com;
            try{
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Keep checking for new messages (by time-stamp) from server to client.
        //When new message found, send it to the client.
        public void run() {
            while(true) {
                //detect if time-stamp changed, else wait for NEW message
                if (com.getServerMsgTimeStamp() != this.timeStamp) {
                    try {
                        timeStamp = com.getServerMsgTimeStamp();
                        out.println( com.getServerMsg() );//send message to client...
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try{
                        sleep(50);
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        //Close connection with client. Send message instructing client to close.
        public void closeConnection(){
            try {
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
