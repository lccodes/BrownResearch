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
import javax.swing.JTextField;

/*
 * @author Elizabeth Hilliard betsy@cs.brown.edu
 *
 * The Items class holds intermediate state for what items will be sold in what auctions.
 * It holds a 2 dimensional array that maintains what items have been assigned to an auction.
 */
public class Items {
	JPanel tableInfo;
	JPanel itemTable;
	JPanel namingGrid;
	JTextField[] textfields;
	JButton[] addButtons;
	ArrayList<JButton>  toGroup = new ArrayList<JButton>();
	JButton[][] itemButtons;

	JTextArea inst;

	State state;
	GridBagConstraints c;
	GridBagConstraints c2;
	GridBagConstraints c3;
	//int n = 5;
	//int m = 8;
	public Items(State state){
		//TAB FOR items
		this.state = state;

		tableInfo = createPanel();
		tableInfo.setPreferredSize(new Dimension(500,500));
	}

	/*
	 * redraws the panel if the BasicSetup has been altered
	 */
	protected JPanel recreatePanel(State state){
		this.state = state;
		tableInfo = createPanel();
		return tableInfo;
	}
	/*
	 * creates the panel
	 */

	protected JPanel createPanel(){

		int m = state.getMaxSeq();
		int n = state.getMaxSim();
		int numItems = state.getNumItems();
		//System.out.println("M now "+m+" and N now "+n);

		//creates a panel for the tab
		tableInfo = new JPanel(new GridBagLayout());
		c3 = new GridBagConstraints();
		//a panel for the item matrix
		itemTable = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		//a panel for the item names and add buttons
		namingGrid = new JPanel(new GridBagLayout());
		c2 = new GridBagConstraints();

		inst = new JTextArea(getTextFor("goodsInst.txt"));
		inst.setEditable(false);
		inst.setFont(new Font("Ariel", Font.BOLD, 13));

		itemTable.setName("goods");

		//holds a matrix of buttons for each auction
		itemButtons = new JButton[n][m];
		//holds add buttons for each item
		addButtons = new JButton[numItems];

		//add a button for each item
		for(int i=0; i<n; i++){
			for(int j = 0; j<m; j++){
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = j;
				c.gridy = i+1;
				c.weightx = 0.5;
				itemButtons[i][j] = new JButton("?");
				itemButtons[i][j].addActionListener(new ItemButtonListener());
				itemButtons[i][j].setActionCommand(Integer.toString((i*m) +j));
				//System.out.println((i*m) +j);
				itemButtons[i][j].setBackground(Color.lightGray);
				itemTable.add(itemButtons[i][j], c);
			}
		}

		JLabel seqLabel = new JLabel("Sequential Auctions");
		c.fill = GridBagConstraints.HORIZONTAL;
		//c.anchor = GridBagConstraints.ABOVE_BASELINE;
		c.ipady = 20;      //makes this component tall
		c.weightx = 0.0;
		c.gridwidth = m;
		c.gridx = n/2 -1;
		c.gridy = n+2;
		itemTable.add(seqLabel, c);
		c.weightx = .5;
		c.ipady = 60;
		c.gridy =0;
		itemTable.add(new JLabel("Goods Schedule"), c);


		//int numItems = 6;

		c2.gridx=0;
		c2.gridy = 0;
		namingGrid.add(new JLabel("       Good #"), c2);
		c2.gridx=1;
		c2.gridy = 0;
		namingGrid.add(new JLabel("Good Name"), c2);
		c2.gridx=2;
		c2.gridy = 0;
		namingGrid.add(new JLabel("   "), c2);


		c2.anchor = GridBagConstraints.EAST;
		JLabel[] labels = new JLabel[numItems];
		textfields = new JTextField[numItems];

		//create the add buttons for each item,
		//these buttons have action listeners that set the item
		// for the corresponding auction
		for(int k=1;k<=numItems;k++){
			textfields[k-1] = new JTextField("Good"+k, 18);
			labels[k-1] = new JLabel("     Good "+k+":  ");
			labels[k-1].setLabelFor(textfields[k-1]);
			addButtons[k-1]=new JButton("add");
			addButtons[k-1].setActionCommand(Integer.toString(k));
			addButtons[k-1].addActionListener(new AddButtonListener());
			Color col = Color.gray;

			//note, it would be good to have more colors.
			switch(k){
			case 1: col = new Color(124, 252, 0);
			break;
		case 2: col = new Color(255, 140, 0);
			break;
		case 3: col = new Color(112,147,219);
			break;
		case 4: col = new Color(138, 43, 226);
			break;
		case 5: col = new Color(255, 20, 147);
			break;
		case 6: col = new Color(0, 238, 238);
			break;
		case 7: col = new Color(255,170,171);
			break;
		case 8: col = new Color(0, 0, 255);
			break;
		case 9: col = new Color(0, 100, 0);
			break;
		case 10: col = new Color(255, 0, 0);
			break;
	}
			addButtons[k-1].setBackground(col);


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
			namingGrid.add(textfields[k-1], c2);

			//c2.gridwidth = GridBagConstraints.REMAINDER; //end row
			c2.fill = GridBagConstraints.HORIZONTAL;
	    	c2.weightx = 1.0;
	    	c2.gridx = 2;
	    	c2.gridy = k;
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
		//add the components to the main panel, tableInfo
		tableInfo.add(labelSim, c3);
		c3.gridx = 1;
		tableInfo.add(itemTable, c3);
		c3.gridx = 2;
		//tableInfo.add(itemNums);
		tableInfo.add(namingGrid, c3);

		return tableInfo;
	}

	//update the state to know what the item names are supposed to be
	protected void updateState(){
		for(int itemButton = 1; itemButton<=state.numItems; itemButton++){
			//remove any spaces in the names as this causes problems with the config files
			state.addItemName(textfields[itemButton-1].getText().replace(" ", ""));
		}
		//System.out.println(state.numItems);


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

	protected class ItemButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			//if not assigned, add to list to add, change color
			if(((JButton) e.getSource()).getBackground()==Color.lightGray){
				((JButton) e.getSource()).setBackground(Color.yellow);
				toGroup.add(((JButton) e.getSource()));

				//if assigned, unassign, change color/text
			}else{
				((JButton) e.getSource()).setBackground(Color.lightGray);
				toGroup.remove(((JButton) e.getSource()));
				int auctNum = Integer.parseInt(((JButton)e.getSource()).getActionCommand());
				//set the item at that spot to not be an item, label with ?
				state.itemsSchedule[auctNum] = -1;
				((JButton)e.getSource()).setText("?");
			}

		}

	}

	protected class AddButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			//set what item is supposed to be set for that auction
			int indexToAdd = Integer.parseInt(((JButton) e.getSource()).getActionCommand());
			//System.out.println(indexToAdd);
			//for each button that has been selected, set the item for that auction
			while (!toGroup.isEmpty()){
				JButton button = toGroup.remove(0);
				int auctNum = Integer.parseInt(button.getActionCommand());
				state.itemsSchedule[auctNum] = Integer.parseInt(((JButton) e.getSource()).getActionCommand())-1;

				/*
				state.itemsSchedule[auctNum] = textfields[indexToAdd-1].getText();
				System.out.println(auctNum+"selling "+textfields[indexToAdd-1].getText());
				textfields[indexToAdd-1].setEditable(false);
				state.addItemName(textfields[indexToAdd-1].getText());
				*/
				//set the text of the button to the item that has been set there
				button.setText(((JButton) e.getSource()).getActionCommand());
				Color col= Color.gray;
				//assign the same color as the add button, for clarity
				switch(indexToAdd){
				case 1: col = new Color(124, 252, 0);
				break;
			case 2: col = new Color(255, 140, 0);
				break;
			case 3: col = new Color(112,147,219);
				break;
			case 4: col = new Color(138, 43, 226);
				break;
			case 5: col = new Color(255, 20, 147);
				break;
			case 6: col = new Color(0, 238, 238);
				break;
			case 7: col = new Color(255,170,171);
				break;
			case 8: col = new Color(0, 0, 255);
				break;
			case 9: col = new Color(0, 100, 0);
				break;
			case 10: col = new Color(255, 0, 0);
				break;
		}

				button.setBackground(col);
			}
			//toGroup.clear();



		}

	}
	public Color getColorFor(int val, int numberVals){

		return null;

	}
	public JPanel getTableInfo(){
		return tableInfo;
	}

}
