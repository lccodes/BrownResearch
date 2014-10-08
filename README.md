BrownResearch
=============
A multiyear project, the Java Auction Configuration Kit has grown into a versatile and customizable auction simulator 
which can accomodate command line clients, web clients, and agents together. 
  See the web GUI at jack.cs.brown.edu/jackweb/ffad/1
  
The back-end is in Java and the front-end is in python and the control panel is bash. 
	home/aloomis is the location of Jack
	home/aloomis/jack is the auction simulator
	home/aloomis/jackweb is the front-end for the simulator
	web/html contains front-end scripts for the BrownFF Chrome extension
	web/respice contains the data recieved from BrownFF
	

For full development history and usage see JackHelpFile.txt

New Developments by lccodes:
  1. Random and Psuedo Random Auctions (web compatible)
  2. First and Second Price sealed bid auctions (w.c.)
  3. Recovery auctions, which allow for the restart to the second of the current sequence
  4. Crash prevention mechanisms
  5. Bugets; now we can assign each bidder a budget which persists throughout the sequence
  6. Updated web interface; more user friendly and fewer glitches
  7. Second Web Interface (under construction)
  8. Bash control panel: easily configure auction sequences and run different xml files with a simple command
      See scripts/run.sh
