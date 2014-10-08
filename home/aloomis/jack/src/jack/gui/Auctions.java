package jack.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/*
 * @author Elizabeth Hilliard betsy@cs.brown.edu
 *
 * The Auctions class keeps intermediate state for what type of
 * auctions are supposed to take place at what times in the schedule.
 */
public class Auctions {
	JPanel tableInfo;
	JPanel auctionTable;
	JPanel namingGrid;

	JTextArea inst;

	JButton[] addButtons;
	ArrayList<JButton>  toGroup = new ArrayList<JButton>();
	JButton[][] auctionButtons;

	State state;
	GridBagConstraints c;
	GridBagConstraints c2;
	GridBagConstraints c3;

	//int n = 5;
	//int m = 8;

	//creates the panel for the auctions tab
	public Auctions(State state){
		//TAB FOR AUCTIONS
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

		tableInfo = new JPanel(new GridBagLayout());
		c3 = new GridBagConstraints();
		auctionTable = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();

		namingGrid = new JPanel(new GridBagLayout());
		c2 = new GridBagConstraints();

		inst = new JTextArea(getTextFor("auctionInst.txt"));
		inst.setEditable(false);
		inst.setFont(new Font("Ariel", Font.BOLD, 13));

		auctionTable.setName("Auctions");
		auctionButtons = new JButton[n][m];
		addButtons = new JButton[4];

		//create a button for each auction slot
		for(int i=0; i<n; i++){
			for(int j = 0; j<m; j++){
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = j;
				c.gridy = i+1;
				c.weightx = .5;
				auctionButtons[i][j] = new JButton("?");
				auctionButtons[i][j].addActionListener(new AuctionButtonListner());
				auctionButtons[i][j].setActionCommand(Integer.toString((i*m) +j));
				//System.out.println((i*m) +j);
				auctionButtons[i][j].setBackground(Color.lightGray);
				auctionTable.add(auctionButtons[i][j], c);
			}

		}
		JLabel seqLabel = new JLabel("Sequential Auctions");
		c.fill = GridBagConstraints.HORIZONTAL;
		//c.anchor = GridBagConstraints.ABOVE_BASELINE;
		c.ipady = 20;      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = m;
		c.gridx = n/2 -1;
		c.gridy = n+1;
		auctionTable.add(seqLabel, c);
		c.ipady = 60;
		c.gridy =0;
		auctionTable.add(new JLabel("Auction Schedule"), c);

		//int numItems = 6;

		c2.gridx=0;
		c2.gridy = 0;
		namingGrid.add(new JLabel("       Auction Type"), c2);
		c2.gridx=1;
		c2.gridy = 0;
		//namingGrid.add(new JLabel("Item Name"), c2);
		//c2.gridx=2;
		//c2.gridy = 0;
		namingGrid.add(new JLabel("   "), c2);

		c2.anchor = GridBagConstraints.EAST;
		JLabel[] labels = new JLabel[4];

		//creates a button for each auction type and adds appropriate action listeners
		for(int k=1;k<=4;k++){
			String auctName = "";
			switch(k){
				case 1: auctName = "   1st Price Sealed Bid   ";
					break;
				case 2: auctName = "   2nd Price Sealed Bid   ";
				break;
				case 3: auctName = "   Ascending   ";
				break;
				case 4: auctName = "   Descending   ";
				break;
			}

			addButtons[k-1]=new JButton("add");
			addButtons[k-1].setActionCommand(Integer.toString(k));
			addButtons[k-1].addActionListener(new AddButtonListener());
			Color col = Color.gray;
			switch(k){

			case 1: col = new Color(34, 139, 34);

				break;
			case 2: col = new Color(255, 140, 0);

				break;
			case 3: col = new Color(30, 144, 255);

				break;
			case 4: col = new Color(138, 43, 226);

				break;

		}
			addButtons[k-1].setBackground(col);
			labels[k-1] = new JLabel(auctName);
			labels[k-1].setLabelFor(addButtons[k-1]);

			//c2.gridwidth = 1000; //next-to-last
			c2.fill = GridBagConstraints.HORIZONTAL; //reset to default
			c2.weightx = 0.0; //reset to default
			c2.gridx = 0;
			c2.gridy = k;
			namingGrid.add(labels[k-1], c2);

			//c2.gridwidth = GridBagConstraints.RELATIVE; //end row
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.weightx = 1.0;
			c2.gridx = 1;
			c2.gridy = k;
			//namingGrid.add(textfields[k-1], c2);

			//c2.gridwidth = GridBagConstraints.REMAINDER; //end row
			//c2.fill = GridBagConstraints.HORIZONTAL;
	    	//c2.weightx = 1.0;
	    	//c2.gridx = 2;
	    	//c2.gridy = k;
	    	namingGrid.add(addButtons[k-1], c2);

		}

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
		//tableInfo.add(itemNums);
		tableInfo.add(namingGrid, c3);

		return tableInfo;
	}

	//updates the state based on the input info
	protected void updateState(){
		//System.out.println(state.numItems);
		tableInfo = createPanel();

	}

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

	//puts an auction slot on a queue to be set as a to be determined type
	protected class AuctionButtonListner implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			if(((JButton) e.getSource()).getBackground()==Color.lightGray){
				((JButton) e.getSource()).setBackground(Color.yellow);
				toGroup.add(((JButton) e.getSource()));
			}else{
				((JButton) e.getSource()).setBackground(Color.lightGray);
				toGroup.remove(((JButton) e.getSource()));
				int auctNum = Integer.parseInt(((JButton)e.getSource()).getActionCommand());
				state.auctionsSchedule[auctNum] = -1;
				((JButton)e.getSource()).setText("?");
			}

		}

	}

	// sets a selected group of slots to be a type of auction
	protected class AddButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			int indexToAdd = Integer.parseInt(((JButton) e.getSource()).getActionCommand());
			//System.out.println(indexToAdd);
			for(int i =0; i<toGroup.size();i++){
				int auctNum = Integer.parseInt(toGroup.get(i).getActionCommand());
				//System.out.println(auctNum+" added to "+indexToAdd);
				state.auctionsSchedule[auctNum] = indexToAdd;

				Color col= Color.black;
				String txt = " ";
				switch(indexToAdd){

					case 1: col = new Color(34, 139, 34);
							txt = "1st";
						break;
					case 2: col = new Color(255, 140, 0);
							txt = "2nd";
							break;
					case 3: col = new Color(30, 144, 255);
							txt = "Asc.";
							break;
					case 4: col = new Color(138, 43, 226);
							txt = "Desc.";
							break;

				}
				toGroup.get(i).setText(txt);
				toGroup.get(i).setBackground(col);
			}
			//may want to change to removing as opposed to accessing, for clarity
			toGroup.clear();
		}
	}

	// returns the main panel of auction data
	public JPanel getTableInfo(){
		return tableInfo;
	}



}
