#!/bin/bash

JACKROOT=/home/aloomis/jack
JACKWEBROOT=/home/aloomis/jackweb

#Clean up on exit

trap 'kill $(jobs -pr)' SIGINT SIGTERM EXIT

# Run that server

echo -n "Starting JACK server..."

cd $JACKROOT
java -cp bin jack.server.AuctionServer games/ffad/dpa_schedule.xml &> /dev/null &
sleep 1.0

echo "Done"

# Run the client

echo -n "Starting JACK Web Client..."

cd $JACKWEBROOT/ffad/client
python ffad_web_client.py &> /dev/null &

echo "done"
wait
