#!/bin/bash

JACKROOT=/home/aloomis/jack
JACKWEBROOT=/home/aloomis/jackweb

# Cleanup on exit

trap 'kill $(jobs -pr)' SIGINT SIGTERM EXIT

# List options
cd $JACKROOT
cd games/ffad
echo "Current scheduels:"
ls
cd ../..

# Get the schedule to execute
echo "Please enter a schedule"
read SCHEDULE

# Start the server
echo -n "Starting JACK..."


java -cp bin jack.server.AuctionServer games/ffad/"$SCHEDULE" &> /dev/null &
sleep 1.0

echo "done"

# Run the client

echo -n "Starting JACK Web Client..."

cd $JACKWEBROOT/ffad/client
python ffad_web_client.py &> /dev/null &

echo "done"
wait
