package jack.gui;


import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
/*import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
*/
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*
 * @author Elizabeth Hilliard betsy@cs.brown.edu
 *
 * The BasicSetup creates a panel for a tab that takes the
 * basic parameters necessary to create an auction. If these
 * text fields are changed while creating the auction the information
 * on future tabs will need to be changed as well.
 */
public class BasicSetup {
	//The label, text field and panel for each of the values
	JPanel basicConfigs, basicConfigsT;

	JLabel nameL;	JTextField nameF;	JPanel name;

	JLabel minClientsL;	JTextField minClientsF ;	JPanel minClients;

	JLabel maxClientsL ;	JTextField maxClientsF;	JPanel maxClients;

	JLabel numItemsL ;	JTextField numItemsF;	JPanel numItems;

	JLabel maxSeqL;	 JTextField maxSeqF;	JPanel maxSeq;

	JLabel maxSimL ;	JTextField maxSimF;	JPanel maxSim;

	JLabel maxWaitL ;	JTextField maxWaitF;	JPanel maxWait;

	JLabel localL ;	JRadioButton localIP;	JRadioButton publicIP;	JPanel ipPane;

	JLabel portL;	JTextField portF;	JPanel port;

	JLabel respTime ;	JRadioButton trueRespTime;	JRadioButton falseRespTime;	JPanel rtPane;

	JTextArea inst;

	State state;
	GridBagConstraints c1;

	/*
	 * create a tab for the basic game info.
	 * Most of these values are used for the server config. file or
	 * for determining basic parameters used to outline the game being created
	 */
	public BasicSetup(State state){
		this.state = state;
		basicConfigs = new JPanel(new GridBagLayout());
		c1 = new GridBagConstraints();
		c1.anchor = GridBagConstraints.NORTHWEST;
        c1.gridwidth = GridBagConstraints.REMAINDER;
		basicConfigs = createPanel();
	}
	public JPanel createPanel(){
		//TAB FOR BASIC SETUP

		nameL = new JLabel("Game Name: ");
		nameF = new JTextField(state.getGameName(), 25);
		nameL.setLabelFor(nameF);
		name = new JPanel(new FlowLayout());
		name.add(nameL);
		name.add(nameF);

		inst = new JTextArea(getTextFor("basicInst.txt"));
		inst.setEditable(false);
		inst.setFont(new Font("Ariel", Font.BOLD, 13));


		minClientsL = new JLabel("Minimum Number of Clients: ");
		minClientsF = new JTextField(Integer.toString(state.getMinClients()), 5);
		minClientsL.setLabelFor(minClientsF);
		minClients = new JPanel(new FlowLayout());
		minClients.add(minClientsL);
		minClients.add(minClientsF);

		maxClientsL = new JLabel("Maximum Number of Clients: ");
		maxClientsF = new JTextField(Integer.toString(state.getMaxClients()), 5);
		maxClientsL.setLabelFor(maxClientsF);
		maxClients = new JPanel(new FlowLayout());
		maxClients.add(maxClientsL);
		maxClients.add(maxClientsF);

		maxWaitL = new JLabel("Maximum Wait Time for Clients: ");
		maxWaitF = new JTextField(Integer.toString(state.getMaxWait()), 8);
		maxWaitL.setLabelFor(maxWaitF);
		maxWait = new JPanel(new FlowLayout());
		maxWait.add(maxWaitL);
		maxWait.add(maxWaitF);

		maxSeqL = new JLabel("Number of Sequential Auctions: ");
		maxSeqF = new JTextField(Integer.toString(state.getMaxSeq()), 5);
		maxSeqL.setLabelFor(maxSeqF);
		maxSeq = new JPanel(new FlowLayout());
		maxSeq.add(maxSeqL);
		maxSeq.add(maxSeqF);


		maxSimL = new JLabel("Number of Simultaneous Auctions: ");
		maxSimF = new JTextField(Integer.toString(state.getMaxSim()), 5);
		maxSimL.setLabelFor(maxSimF);
		maxSim = new JPanel(new FlowLayout());
		maxSim.add(maxSimL);
		maxSim.add(maxSimF);

		numItemsL = new JLabel("Total Number of Good Types: ");
		numItemsF = new JTextField(Integer.toString(state.getNumItems()), 5);
		numItemsF.addActionListener(new NumItemsInput());
		numItemsL.setLabelFor(numItemsF);
		numItems = new JPanel(new FlowLayout());
		numItems.add(numItemsL);
		numItems.add(numItemsF);


		localL = new JLabel("IP Address: ");
		localIP = new JRadioButton ("local",true);
		localIP.addActionListener(new LocalSelected());
		publicIP = new JRadioButton ("public",false);
		publicIP.addActionListener(new PublicSelected());
		localL.setLabelFor(localIP);
		ipPane = new JPanel(new FlowLayout());
		ipPane.add(localL);
		ipPane.add(localIP);
		ipPane.add(publicIP);

		respTime = new JLabel("Allow Full Response Time: ");
		trueRespTime = new JRadioButton ("True",true);
		trueRespTime.addActionListener(new TrueRTSelected());
		falseRespTime = new JRadioButton ("False",false);
		falseRespTime.addActionListener(new FalseRTSelected());
		respTime.setLabelFor(trueRespTime);
		rtPane = new JPanel(new FlowLayout());
		rtPane.add(respTime);
		rtPane.add(trueRespTime);
		rtPane.add(falseRespTime);

		portL = new JLabel("Port: ");
		portF = new JTextField("1300",5);
		portL.setLabelFor(portF);
		port = new JPanel(new FlowLayout());
		port.add(portL);		port.add(portF);

		c1.gridx = 0;
		c1.gridy = 0;
		basicConfigs.add(inst, c1);
		++c1.gridy;
		basicConfigs.add(name, c1);
		++c1.gridy;
		basicConfigs.add(minClients, c1);
		++c1.gridy;
		basicConfigs.add(maxClients, c1);
		++c1.gridy;
		basicConfigs.add(maxWait, c1);
		++c1.gridy;
		basicConfigs.add(maxSim, c1);
		++c1.gridy;
		basicConfigs.add(maxSeq, c1);
		++c1.gridy;
		basicConfigs.add(numItems, c1);
		++c1.gridy;
		basicConfigs.add(ipPane, c1);
		++c1.gridy;
		basicConfigs.add(rtPane, c1);
		++c1.gridy;
		basicConfigs.add(port, c1);


		return basicConfigs;
	}
	//get text for instructions
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

	protected JPanel getBasicConfigs(){
		return basicConfigs;
	}


	public class NumItemsInput implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			state.setNumItems(Integer.parseInt(numItemsF.getText()));

		}

	}

	public class LocalSelected implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			state.localIP = true;
			publicIP.setSelected(false);

		}

	}
	public class PublicSelected implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			state.localIP = false;
			localIP.setSelected(false);

		}

	}

	public class TrueRTSelected implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			state.respTime = true;
			falseRespTime.setSelected(false);

		}

	}
	public class FalseRTSelected implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			state.respTime = false;
			trueRespTime.setSelected(false);

		}

	}

	/*
	 * Updates the state given user input to the tab
	 */
	protected void updateState(){
		state.setNumItems(Integer.parseInt(numItemsF.getText()));
		//System.out.println("BS "+state.numItems);
		state.setMaxSeq(Integer.parseInt(maxSeqF.getText()));
		state.setMaxSim(Integer.parseInt(maxSimF.getText()));
		state.setMaxWait(Integer.parseInt(maxWaitF.getText()));
		state.setMaxClients(Integer.parseInt(maxClientsF.getText()));
		state.setMinClients(Integer.parseInt(minClientsF.getText()));

		state.setGameName(nameF.getText());

		state.setPort(portF.getText());
		state.createAuctionParams();
	}


}
