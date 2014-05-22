package jack.gui;

import java.awt.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.util.Properties;


	/*
	 * @author Elizabeth Hilliard betsy@cs.brown.edu
	 * The auction parameters class creates panels for each of the
	 * parameters required by each auction type, handles requesting
	 * this information from users and then passes user input to the
	 * state class for printing.
	 *
	 */
public class AuctionParams {
		JPanel tableInfo;
		JPanel auctionTable;
		JPanel namingGrid;

		JTextArea inst;//instructions

		//the panels, one for each auction
		JPanel[][] auctionParams;

		//an array list that holds all the labels and text fields
		ArrayList<HashMap<String,JTextField>> paramFields;

		State state;

		GridBagConstraints c;
		GridBagConstraints c3;


		//creates the panel for the auction parameter tab
		public AuctionParams(State state){
			this.state = state;
			tableInfo = createPanel();
		}

		//recreates the panel to reflect the parameters in a new basicSetup state
		protected JPanel recreatePanel(State state){
			this.state = state;
			tableInfo = createPanel();
			return tableInfo;
		}

		/*
		 * createPanel creates a new panel based on parameters
		 * entered in the BasicSetup tab
		 */
		protected JPanel createPanel(){
			int m = state.getMaxSeq();
			int n = state.getMaxSim();


			paramFields = new ArrayList<HashMap<String,JTextField>>() ;

			tableInfo = new JPanel(new GridBagLayout());
			c3 = new GridBagConstraints();
			auctionTable = new JPanel(new GridBagLayout());
			c = new GridBagConstraints();

			inst = new JTextArea(getTextFor("auctionParamInst.txt"));
			inst.setEditable(false);
			inst.setFont(new Font("Ariel", Font.BOLD, 13));

			auctionTable.setName("Auctions");
			auctionParams = new JPanel[n][m];

			//create a panel for each auction
			for(int i=0; i<n; i++){
				for(int j = 0; j<m; j++){
					c.fill = GridBagConstraints.BOTH;
					c.gridx = j;
					c.gridy = i+1;
					c.weightx = .5;
					//get a new panel for a given type of auction
					auctionParams[i][j] = getParamPanel(state.auctionsSchedule[m*i+j], m*i+j);
					//System.out.println((i*m) +j);
					auctionTable.add(auctionParams[i][j], c);
				}

			}

			JLabel seqLabel = new JLabel("Sequential Auctions");
			c.fill = GridBagConstraints.HORIZONTAL;
			c.ipady = 20;      //make this component tall
			c.weightx = 0.0;
			c.gridwidth = m;

			c.gridx = n/2 -1;
			c.gridy = n+1;
			auctionTable.add(seqLabel, c);
			c.ipady = 60;
			c.gridy =0;
			auctionTable.add(new JLabel("Auction Parameters"), c);


			String labelSA = "<html>" + "Simultaneous" + "<br>" + "Auctions" + "</html>";
			JLabel labelSim = new JLabel(labelSA);
			c3.gridwidth = 3;
			c3.gridx = 0;
			c3.gridy = 0;
			tableInfo.add(inst, c3);
			c3.gridwidth = 1;
			c3.gridy = 1;
			tableInfo.add(labelSim, c3);
			c3.gridx = 1;
			tableInfo.add(auctionTable, c3);
			c3.gridx = 2;

			return tableInfo;
		}

		//updates the state based on the input info
		protected void updateState(){
			//for each auction add all parameters to the state
			for(int i=0; i<state.getMaxSeq()*state.getMaxSim(); i++){
				Set<String> keys = paramFields.get(i).keySet();
				for(String ky : keys){
					String val = paramFields.get(i).get(ky).getText();
					state.setAuctionParam(ky, val, i);
				}
			}
		}

		//creates a panel for a given auction type to be held at a given time
		private JPanel getParamPanel(int type, int numAuction){
			JPanel paramPan = new JPanel(new GridBagLayout());
			GridBagConstraints c1 = new GridBagConstraints();
			Properties prop = new Properties();
			//System.out.println("Type: "+type);
			c1.gridx = 0;

			try {
				//load a properties file for the specific auction type
				//prop.load(new FileInputStream(System.getProperty("user.dir")+"/src/gui/Initialize/Initialize_"+getTypeName(type)+".properties"));
				prop.load(new FileInputStream("src/gui/Initialize/Initialize_"+getTypeName(type)+".properties"));

				Set<String> params = prop.stringPropertyNames();
	    		c1.gridy = 0;
	    		//label the type of auction
	    		paramPan.add(new JLabel (getTypeName(type)), c1);
	    		HashMap<String, JTextField> auctHM= new HashMap<String, JTextField>();
	    		for(String parameter:params){
	    			//label and create a field for all parameters
	    			JLabel label = new JLabel(parameter);
	    			JTextField tfield = new JTextField(prop.getProperty(parameter),6);
	    			auctHM.put(parameter, tfield);
	    			paramFields.add(numAuction, auctHM);
	    			c1.gridy = c1.gridy +1;
	    			c1.gridx = 0;
	    			paramPan.add(label, c1);
	    			c1.gridx = 1;
	    			paramPan.add(tfield, c1);
	    		}

	    	} catch (IOException ex) {
	    		ex.printStackTrace();
	        }
	    	//create a boarder for the panel, creating a table-like structure
	    	paramPan.setBorder(BorderFactory.createLineBorder(Color.black, 3));
			return paramPan;
		}

		//gets instructions for the tab from a txt file
		private String getTextFor(String tabName){
			StringBuilder builder = new StringBuilder();
		    try {
		    	File input = new File("src/gui/inst/"+tabName);
		    	BufferedReader in = new BufferedReader(new FileReader(input));
		    	char[] chars = new char[1 << 16];

		    	int length;
				while ((length = in.read(chars)) > 0) {

				  builder.append(chars, 0, length);

				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return builder.toString();
		}

		//returns the string name of an auction type id number
		private String getTypeName(int type){
			String name = "FirstPrice";
			switch(type){
				case 1:
					name = "FirstPrice";
					break;
				case 2:
					name = "SecondPrice";
					break;
				case 3:
					name = "AscendingPrice";
					break;
				case 4:
					name = "DescendingPrice";
					break;
			}
			//System.out.println("NM: "+name);
			return name;
		}

		// returns the main panel of auction data
		public JPanel getTableInfo(){
			return tableInfo;
		}


}
