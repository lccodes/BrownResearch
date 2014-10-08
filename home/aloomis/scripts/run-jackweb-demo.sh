#!/bin/bash

JACKROOT=/home/aloomis/jack
JACKWEBROOT=/home/aloomis/jackweb

# Cleanup on exit

trap 'kill $(jobs -pr)' SIGINT SIGTERM EXIT

# Run the server

echo -n "Starting JACK Server..."

cd $JACKROOT
java -cp bin jack.server.AuctionServer games/ffad/test1.xml &> /dev/null &
sleep 1.0

echo "done"

# Run the client

echo -n "Starting JACK Web Client..."

cd $JACKWEBROOT/ffad/client
python ffad_web_client.py &> /dev/null &

echo "done"
wait
