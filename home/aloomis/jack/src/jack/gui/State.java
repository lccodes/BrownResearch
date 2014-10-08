package jack.gui;
/*
 * @author Elizabeth Hilliard betsy@cs.brown.edu
 *
 * The State class holds the state information, input from the user,
 * for a game.
 *
 * initial, generalized values are given
 *
 * NOTES: Currently, the reserve, initial price and increment
 * values are not set within the gui. Users can change these manually
 * by editing the created config files.
 *
 * CURRENT ONGOING EDITS: working on:
 *   1. loading state from a set of config. files so that
 *   the state can be editable
 *
 *   2. Including reserver and other clock auction parameters
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JTextField;


public class State {


	int numItems = 2;
	int maxSeq = 1;
	int maxSim = 1;
	int maxClients = 10;
	int minClients = 2;
	int maxWait = 60000;

	String port = "1300";
	String gameName = "JACK Game";
	boolean localIP = true;
	boolean respTime = true;

	ArrayList<String> itemNames = new ArrayList<String>();

	int[] itemsSchedule = new int[100];
	//String[] itemsSchedule = new String[100];
	int[] auctionsSchedule = new int[100];
	int minInc = 1;
	int startPrice = 100;
	int reserve = 1;
	int responseTime = 15000;

	String valueFxn = "Additive";
	HashMap <String, JTextField[]> valParamForItem;
	int numContracts = 5;

	ArrayList<HashMap<String, String>> auctionParams;

	public State(){


	}

	//print the Config_AuctionServer.txt file
	// This file sets up the basic parameters for the server.
	protected void printServerConfig(){
		/*String filePrefix = System.getProperty("user.dir")+"/src/server/"+gameName;
		File gameFolderServ = new File(filePrefix);
		String filePrefix2 = System.getProperty("user.dir")+"/src/auctions/"+gameName;
		File gameFolderAuct = new File(filePrefix2);

		File server_config = new File(filePrefix+"/Config_AuctionServer.txt");
		*/
		File server_config = new File("src/server/"+"Config_AuctionServer.txt");
		if(!server_config.exists()){

			 try{

				 //gameFolderServ.mkdir();
				 // gameFolderAuct.mkdir();
				 server_config.createNewFile();
				 FileWriter fstream = new FileWriter(server_config);
				 BufferedWriter out = new BufferedWriter(fstream);
				 if(localIP){
					 out.write("Host_IP:local\n");
				 }else{
					 out.write("Host_IP:public\n");
				 }

				 out.write("Port_Number:"+port+"\n");
				 //out.newLine();

				 out.write("Min_Number_Clients:"+minClients+"\n");
				 out.write("Max_Number_Clients:"+maxClients+"\n");
				 out.write("Max_Wait_For_Clients:"+maxWait+"\n");
				 out.write("Response_Time:"+responseTime);
				 out.newLine();
				 if(respTime){
					 out.write("Full_Response_Time:true");
				 }else{
					 out.write("Full_Response_Time:false");
				 }
				 out.newLine();
				 out.write("Server_Log_File:Log_AuctionServer.txt");
				 out.newLine();
				 out.write("Server_Results_File:Results_AuctionServer.txt");
				 out.newLine();

				 if(maxSeq>1 && maxSim>1){
					 out.write("Auction_Type:SequentialAuction");
					 out.newLine();
					 out.write("Auction_Config_File:Config_Sequential_of_Simultaneous.txt");
					 out.newLine();
				 }else if(maxSeq>1){
					 out.write("Auction_Type:SequentialAuction");
					 out.newLine();
					 out.write("Auction_Config_File:Config_SequentialAuction_0.txt");
					 out.newLine();
				 }else if(maxSim>1){
					 out.write("Auction_Type:SimultaneousAuction");
					 out.newLine();
					 out.write("Auction_Config_File:Config_SimultaneousAuction_0.txt");
					 out.newLine();
				 }else if(maxSeq==1 && maxSim==1){
					 out.write("Auction_Type:"+getAuctionName(auctionsSchedule[0])+"\n");
					 out.write("Auction_Config_File:Config_"+getAuctionName(auctionsSchedule[0])+"_0_0.txt");
					 out.newLine();
				 }else{
					 System.out.println("ERROR: no auctions");
				 }

				 out.write("Valuation_Type:"+valueFxn);
				 out.newLine();
				 out.write("Valuation_Config_File:Config_Valuation"+valueFxn+".txt");

				 out.close();

			 }catch  (IOException e){
					System.out.println("file error creating Server Config.");
			}
		}
	}


	//returns the name string for an auction in the
	// auctionsSchedule by mapping the id to the name
	//NOTE: This method should be edited if you add a new
	// auction type.
	private String getAuctionName(int i) {
		switch(i){
			case 1: return "FirstPriceAuction";
			case 2: return "SecondPriceAuction";
			case 3: return "AscendingPriceAuction";
			case 4: return "DescendingPriceAuction";
		}
		return "FirstPriceAuction";
	}

	//Prints a file if Simultaneous Sequential auctions
	// are to be run
	protected void printSeqSimConfig(){
		//Create the SeqSim file
		//File superAuctionFile = new File(System.getProperty("user.dir")+"/src/auctions/
		//"+gameName+"/Config_Sequential_of_Simultaneous.txt");
		File superAuctionFile = new File("src/auctions/Config_Sequential_of_Simultaneous.txt");

		//if the file does not already exist, create it and write the file contents
		if(!superAuctionFile.exists()){
			try{
				superAuctionFile.createNewFile();
				FileWriter fstreamsuper = new FileWriter(superAuctionFile);
				BufferedWriter outSuper = new BufferedWriter(fstreamsuper);

//				outSuper.write("Valuation:"+valueFxn +" Config_File:Config_Valuation"+valueFxn+".txt");
//				if(valueFxn.compareToIgnoreCase("contracts")==0){
//					outSuper.write(" Contracts_Per_Client:"+numContracts);
//				}
//				outSuper.newLine();

				//print a line for each sequential auction
				for(int i=0;i<maxSeq;i++){
					outSuper.write("Auction_Type:SimultaneousAuction");
					outSuper.write(" Config_File:Config_SimultaneousAuction_"+i+".txt");
					outSuper.newLine();
				}

				outSuper.close();
			}catch(IOException e){
				System.out.println("File error creating Sequential Simultaneous Config.");
			}

		}

	}

	protected void printSeqConfig(int simNum){
		/*File superAuctionFile = new File(System.getProperty("user.dir")+
				"/src/auctions/"+gameName+"/Config_SequentialAuction_"+seqNum+".txt");*/
		File superAuctionFile = new File("src/auctions/Config_SequentialAuction_"+simNum+".txt");

		//if the file does not exist, create and write the file
		if(!superAuctionFile.exists()){
			try{
				superAuctionFile.createNewFile();
				FileWriter fstreamsuper = new FileWriter(superAuctionFile);
				BufferedWriter outSuper = new BufferedWriter(fstreamsuper);
//
//				outSuper.write("Valuation:"+valueFxn +" Config_File:Config_Valuation"+valueFxn+".txt");
//				if(valueFxn.compareToIgnoreCase("contracts")==0){
//					outSuper.write(" Contracts_Per_Client:"+numContracts);
//				}
//				outSuper.newLine();


				//String auctName = "";
				//for(int j = (maxSim*maxSeq)-1; j>0;){

				//write a line for each sub auction
				for(int j = 0; j< maxSeq; j++){
					/* METHOD CREATED
					switch(auctionsSchedule[maxSeq*(j-1)+(seqNum)]){
						case 1: auctName = "FirstPriceAuction";
							break;
						case 2: auctName = "SecondPriceAuction";
							break;
						case 3: auctName = "AscendingAuction";
							break;
						case 4: auctName = "DescendingAuction";
							break;
					}*/

					outSuper.write("Auction_Type:"+ getAuctionName(auctionsSchedule[maxSeq*simNum+j]));
					outSuper.write(" Config_File:Config_"+getAuctionName(auctionsSchedule[maxSeq*simNum+j])+
							"_"+j+"_"+simNum+".txt");
					outSuper.newLine();



				}
				outSuper.close();
			}catch(IOException e){
				System.out.println("File error creating Sequential Config.");
			}

		}

	}

	/*
	 * printSimConfig prints the config file for the ith simultaneous auction
	 */
	protected void printSimConfig(int seqNum){
		/*File superAuctionFile = new File(System.getProperty("user.dir")+
				"/src/auctions/"+gameName+"/Config_SimultaneousAuction_"+seqNum+".txt");*/
		File superAuctionFile = new File("src/auctions/Config_SimultaneousAuction_"+seqNum+".txt");
		if(!superAuctionFile.exists()){
			try{
				superAuctionFile.createNewFile();
				FileWriter fstreamsuper = new FileWriter(superAuctionFile);
				BufferedWriter outSuper = new BufferedWriter(fstreamsuper);

//				outSuper.write("Valuation:"+valueFxn +" Config_File:Config_Valuation"+valueFxn+".txt");
//				if(valueFxn.compareToIgnoreCase("contracts")==0){
//					outSuper.write(" Contracts_Per_Client:"+numContracts);
//				}
//				outSuper.newLine();


				//String auctName = "";
				//for(int j = (maxSim*maxSeq)-1; j>0;){
				for(int j = maxSim; j>0; j--){
					/* METHOD CREATED
					switch(auctionsSchedule[maxSeq*(j-1)+(seqNum)]){
						case 1: auctName = "FirstPriceAuction";
							break;
						case 2: auctName = "SecondPriceAuction";
							break;
						case 3: auctName = "AscendingAuction";
							break;
						case 4: auctName = "DescendingAuction";
							break;
					}*/
					outSuper.write("Auction_Type:"+ getAuctionName(auctionsSchedule[maxSeq*(j-1)+(seqNum)]));
					outSuper.write(" Config_File:Config_"+
							getAuctionName(auctionsSchedule[maxSeq*(j-1)+(seqNum)])+"_"+seqNum+"_"+((maxSeq*(j-1)+(seqNum))/maxSeq)+".txt");
					outSuper.newLine();



				}
				outSuper.close();
			}catch(IOException e){
				System.out.println("File error creating Simultaneous Config.");
			}

		}

	}

	/*
	 * print SubAuction creates and prints the config file
	 * for the ith sub auction in the auction schedule
	 *
	 * Note: the auction schedule is created by indexing the array from the bottom
	 * left-hand corner to the top right.
	 *
	 * To access the ith simultaneous auction in the jth sequential auction,
	 * access the i = maxSim-(n/maxSeq)-1; j = n%maxSeq
	 * where n is the index of the auctionsSchedule array
	 */
	protected void printSubAuction(int i){
		/*
		File subAuctionFile = new File(System.getProperty("user.dir")+"/src/auctions/"
				+gameName+"/Config_subAuct_"+(i%maxSeq)+"_"+(maxSim-(i/maxSeq)-1)+".txt");*/
		File subAuctionFile = new File("src/auctions/Config_"+getAuctionName(auctionsSchedule[i])+"_"
				+(i%maxSeq)+"_"+(i/maxSeq)+".txt");

		//if the file does not exist, create and write it
		if(!subAuctionFile.exists()){
			try{
				subAuctionFile.createNewFile();
				FileWriter fstreamsuper = new FileWriter(subAuctionFile);
				BufferedWriter outSub = new BufferedWriter(fstreamsuper);



				if(auctionsSchedule[i]==3 || auctionsSchedule[i]==4){
					if(respTime){
						outSub.write("Full_Response_Time:true");
					}else{
						outSub.write("Full_Response_Time:false");
					 }
					outSub.write(" Response_Time:"+responseTime);
					outSub.newLine();
				}
				outSub.write("Good:"+ itemNames.get(itemsSchedule[i]));
				outSub.write(" Reserve:"+auctionParams.get(i).get("Reserve"));
				if(auctionsSchedule[i]==1 || auctionsSchedule[i]==2){
					if(respTime){
						outSub.write(" Full_Response_Time:true");
					}else{
						outSub.write(" Full_Response_Time:false");
					 }
					outSub.write(" Response_Time:"+responseTime);
				}

				//add parameter for the price increment for ascending auction
				if(auctionsSchedule[i]==3){
					outSub.write(" Min_Price_Increment:"+auctionParams.get(i).get("Min_Price_Increment"));
				//add parameter for the starting price for a descending clock auction
				}else if(auctionsSchedule[i]==4){
					outSub.write(" Start_Price:"+auctionParams.get(i).get("Start_Price"));
				}

				outSub.newLine();
				outSub.close();
			}catch(IOException e){
				System.out.println("File error creating Sub Auction Config.");
			}
		}


	}

	/*
	 * printIP_Port creates and prints a file that any agents participating
	 * in the auction will need. It access the IP address for the host machine
	 * and sets the port to be used.
	 *
	 * Note: If you are running the auction on a different machine than the one
	 * you created the auction on, you will need to check these files.
	 * The server will print what port and IP it is using when it starts.
	 */
	protected void printIP_Port(){
		File iP_PortFile = new File(System.getProperty("user.dir")+"/IP_and_Port.txt");

			try{
				if(!iP_PortFile.exists()){
					iP_PortFile.createNewFile();
				}
				FileWriter fstreamsuper = new FileWriter(iP_PortFile);
				BufferedWriter outIP = new BufferedWriter(fstreamsuper);


				outIP.write(getIP());
				outIP.newLine();
				outIP.write(port);

				outIP.close();
			}catch(IOException e){
				System.out.println("File error creating IP_port file");
			}

	}

	/*
	 * printValConfig creates and writes the configuration file for the
	 * different value functions (Schedule, Additive, Contracts)
	 *
	 * If you create your own value function you may have to create these files on your own.
	 *
	 * Future: Create a way to arbitrarily print the needed info for a value function.
	 */
	protected void printValConfig(){
		//System.out.println("writing values");
		if(valueFxn.compareToIgnoreCase("additive")==0){
			printAdditive();

		}else if(valueFxn.compareToIgnoreCase("schedule")==0){
			printSchedule();


		}else if(valueFxn.compareToIgnoreCase("contract")==0){
			printContracts();

		}else{
			System.out.println("Value function not set correctly.");
		}
	}

	protected void printContracts(){
		File valueFile = new File("src/valuations/Config_ValuationContract.txt");
		if(!valueFile.exists()){
			try{
				//System.out.println("writing contracts");
				valueFile.createNewFile();
				FileWriter fstreamWriter = new FileWriter(valueFile);
				BufferedWriter outValue = new BufferedWriter(fstreamWriter);
				outValue.write("Contracts_Per_Client:"+numContracts);
				outValue.newLine();
				for(int i=0;i<itemNames.size();i++){
					//System.out.println("item name"+itemNames.get(i));
					outValue.write("Good:"+itemNames.get(i));
					for(int j=0;j<4;j++){
						outValue.write(" ");
						switch(j){
						case 0:
							outValue.write("Need_Low_Bound:");
							break;
						case 1:
							outValue.write("Need_High_Bound:");
							break;
						case 2:
							outValue.write("Value_Low_Bound:");
							break;
						case 3:
							outValue.write("Value_High_Bound:");
							break;
						}
						outValue.write(valParamForItem.get(itemNames.get(i))[j].getText());
					}
				outValue.newLine();
				}
				outValue.close();
			}catch(Exception e){
				System.out.println("Error printing Value Function");
			}
		}
	}

	protected void printAdditive(){
		File valueFile = new File("src/valuations/Config_ValuationAdditive.txt");
		if(!valueFile.exists()){
			try{
				//System.out.println("writing additive");
				valueFile.createNewFile();
				FileWriter fstreamWriter = new FileWriter(valueFile);
				BufferedWriter outValue = new BufferedWriter(fstreamWriter);
				//System.out.println("writer made");
				for(int i=0;i<itemNames.size();i++){
					//System.out.println("Good "+itemNames.get(i));
					outValue.write("Good:"+itemNames.get(i));
//					for(int j=0;j<4;j++){
//						outValue.write(" ");
//						System.out.println(j);
//						System.out.println(valParamForItem.size());
//						outValue.write(valParamForItem.get(itemNames.get(i))[j].getText());
//					}
					outValue.write(" Linear_Low_Bound:"+valParamForItem.get(itemNames.get(i))[0].getText());
					outValue.write(" Linear_High_Bound:"+valParamForItem.get(itemNames.get(i))[1].getText());
					outValue.write(" Exponent_Low_Bound:"+valParamForItem.get(itemNames.get(i))[2].getText());
					outValue.write(" Exponent_High_Bound:"+valParamForItem.get(itemNames.get(i))[3].getText());
					//System.out.println("out for");
				outValue.newLine();
				}
				outValue.close();
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Error printing Value Function");
			}

		}

	}

	protected void printSchedule(){
		File valueFile = new File("src/valuations/Config_ValuationSchedule.txt");
		if(!valueFile.exists()){
			try{
				//System.out.println("writing schedule");
				valueFile.createNewFile();
				FileWriter fstreamWriter = new FileWriter(valueFile);
				BufferedWriter outValue = new BufferedWriter(fstreamWriter);

				outValue.write("Value_Low_Bound:"+valParamForItem.get("Min Val")+" Value_High_Bound:"+
						valParamForItem.get("Max Val"));

				outValue.write(" Number_Of_Goods:"+Integer.toString(Math.max(maxSeq, maxSim)));

				outValue.write(" Need_Low_Bound:"+valParamForItem.get("Min Number")+" Need_High_Bound:"+
						valParamForItem.get("Max Number"));

				outValue.write(" Deadline_Loe_Bound:"+valParamForItem.get("Earliest Deadline")+" Deadline_High_Bound:"+
						valParamForItem.get("Latest Deadline"));
				outValue.close();
			}catch(Exception e){
				System.out.println("Error printing Value Function");
			}
		}
	}

	/*
	 * printStateFiles decides what files need to be printed and ensures
	 * that the right number and types of files are created.
	 */
	protected void printStateFiles(){
		printServerConfig();
		printValConfig();

		//case where simultaneous sequential auctions are being run
		if(maxSeq>1 && maxSim>1){
			 printSeqSimConfig();
			 for(int i = 0; i<(maxSeq*maxSim); i++){
					if(i<maxSeq){
						//System.out.println("SeqNum "+i);
						printSimConfig(i);
					}
					printSubAuction(i);
				}
		//case where sequential auctions are being run
		 }else if(maxSeq>1){
			 printSeqConfig(0);
			 for(int i = 0; i<maxSeq; i++){
					printSubAuction(i);
			}
		//case where simultaneous auctions are being run
		 }else if(maxSim>1){
			 printSimConfig(0);
			 for(int i = 0; i<maxSim; i++){
					printSubAuction(i);
			}
		//case where a single auction is being run
		 }else if(maxSeq==1 && maxSim==1){
			 //printSimConfig(0);
			 printSubAuction(0);
		 }else{
			 System.out.println("ERROR: no auctions");
		 }

		printIP_Port();

	}

	//getIP accesses the IP address of the server host
	public String getIP() throws UnknownHostException{
		InetAddress addr = InetAddress.getLocalHost();//Get local IP
 	    String ip = addr.toString().split("\\/")[1];//local IP
 	    //ip = ip.indexOf('\\');
 	    if(!getIsLocal()){
	    	try{
		    	URL whatismyip = new URL("http://automation.whatismyip.com/n09230945.asp");
		    	BufferedReader in = new BufferedReader(new InputStreamReader(
		    	                whatismyip.openStream()));
		    	ip = in.readLine(); //you get the IP as a String
		    	//System.out.println(ip);
		    	in.close();
		    	return ip;

	    	}
	    	catch(IOException e){
	    		e.printStackTrace();
	    	}
	    	return null;
 	    }
		return ip;
	    }

	//may need this kind of method in the future?
	protected void setToDefault(){

	}

	protected void addItemName(String name){
		if(!itemNames.contains(name)){
			itemNames.add(name);
		}
	}

	protected int[] getItemsSchedule(){
		return itemsSchedule;
	}
	protected int[] getAuctionsSchedule(){
		return auctionsSchedule;
	}
	protected int getNumItems(){
		return numItems;
	}
	public int getMaxSeq() {
		return maxSeq;
	}
	public int getMaxSim() {

		return maxSim;
	}

	public int getMaxClients() {
		return maxClients;
	}
	public int getMinClients() {
		return minClients;
	}

	public String getGameName() {
		return gameName;
	}
	public int getMaxWait() {
		return maxWait;
	}

	public String getValueFxn() {
		return valueFxn;
	}

	public HashMap <String, JTextField[]> getValParam(){
		return valParamForItem ;
	}

	public void addToItemsInAuct(int index, int item){
		itemsSchedule[index]=item;
	}

	public String getAuctionParam(String param, int num){
		return this.auctionParams.get(num).get(param);
	}

	public void setNumItems(int num){
		numItems = num;
	}
	public void setMaxSeq(int seq) {

		maxSeq = seq;
	}
	public void setMaxSim(int sim) {
		maxSim=sim;
	}

	public void setMaxClients(int maxclients) {
		maxClients = maxclients;
	}
	public void setMinClients(int minclients) {
		 minClients = minclients;
	}

	public void setMaxWait(int wait) {

		maxWait = wait;
	}

	public void setGameName(String name) {
		gameName = name;
	}

	public void setPort(String portStr) {
		port = portStr;
	}

	public void setValueFxn(String valFxn) {
		valueFxn = valFxn;
	}

	public void setValParam(HashMap <String, JTextField[]> valParams){
		valParamForItem = valParams;
	}

	private boolean getIsLocal() {
		return localIP;
	}
	public void setNumContracts(int numContracts) {
		this.numContracts = numContracts;
	}

	public void setAuctionParam(String param, String val, int num){


		this.auctionParams.get(num).put(param, val);
	}

	public void createAuctionParams() {
		auctionParams= new ArrayList<HashMap<String,String>>();
		for(int i=0; i<maxSeq*maxSim;i++){
			this.auctionParams.add(i, new HashMap<String,String>());
		}

	}

}
