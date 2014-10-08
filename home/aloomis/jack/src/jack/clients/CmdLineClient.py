#
# @author TJ Goff  goff.tom@gmail.com
# @version 1.0.0
#
# This library is free software; you can redistribute it and/or modify it under the
# terms of the GNU Lesser General Public License version 2.1 as published by the
# Free Software Foundation.
#
#
# This script implements a client to participate in an auction run on a server.
# The client waits to receive a message from the server, and when a message is
# received, the client should respond to the server with some information (such
# as the clients bids in the auction).
#
# When the client starts, it will try to connect to the server at the IP and
# port number specified in the text file IP_and_Port.txt (which should be in
# the same folder as this script).  This should allow you to easily change
# the connection information to reach the server.
#
# For help, contact: TJ Goff  goff.tom@gmail.com

import socket
import threading
import os

#Used to create a separate thread that listens for messages from server, leaving
#main thread free to interact with user.
def socketListen( client_socket, fromServer ):
    prev = "initial"
    while 1:
        fromServer = client_socket.recv(512)
        if fromServer == 'END' or fromServer == "":
            print "Auction is over.  Closing connection.  Goodbye."
            client_socket.close()
            os._exit(1)
        print "\n---------------------------------\n\n\n"
        print "Host: " + fromServer + "\nClient: "




#ADJUST THESE AS NEEDED
HOST = '127.0.0.1'    #IP of host (default is local machine)
PORT = 1300              #Default port as used by the server

print "AuctionClient started.  Attempting to connect to Server: "+HOST+", port: "+str(PORT)+"\n"

#connect to host server
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((HOST, PORT))

print "Connection to host established\n"

fromServer = ""
listenerThread = threading.Thread( target=socketListen, args=(client_socket, fromServer) )
listenerThread.start()

#the request/response loop... receive request, send a response to host
while 1:
    response = raw_input('') #prompt user for string
    #remember to end all socket.send("whatever" + "\n") statements with newline: \n
    sent = client_socket.send( response + "\n" )
    if sent == 0:
        print "message was not sent..."

os._exit(1)
