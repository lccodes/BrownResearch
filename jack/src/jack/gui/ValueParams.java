package jack.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import java.util.HashMap;
import javax.swing.*;

/*
 * @author Elizabeth Hilliard betsy@cs.brown.edu
 *
 * The ValueParams class creates the tabs that control the valuation
 * parameter selection and specification.
 */
public class ValueParams {

	protected JPanel valSelector;//tab to select value function and info here
	protected JPanel valParams; // set parameters specific to the value function here

	JTextArea inst;
	JPanel finalPanel;

	int count = 0;
	State state;
	final String[] valuations = { "Additive", "Contract", "Schedule" };//add to this if add a value function
	JTextArea t;
	JComboBox c;
	JTextField  numContrField;

	JLabel[] itemLabels;
	HashMap <String, JTextField[]> valParamForItem = new HashMap<String, JTextField[]>();

	//creates the initial value selector
	public ValueParams(State state){
		this.state = state;
		valSelector = new JPanel(new BorderLayout());
		valSelector.setPreferredSize(new Dimension(500,300));

		t = new JTextArea(getTextFor("vals", "Additive"));
		c = new JComboBox();

		for (int i = 0; i < valuations.length;i++){
			c.addItem(valuations[count++]);
		}
		t.setEditable(false);
		c.addActionListener(new TextSelected());

		JPanel selector = new JPanel(new FlowLayout());
		selector.add(new JLabel("Select a Valuation Function: "));
		selector.add(c);
		valSelector.add(selector, BorderLayout.NORTH);

		valSelector.add(t, BorderLayout.CENTER);

	}

	/*
	 * returns a panel for a given value function type
	 *
	 * NOTE: THIS SHOULD BE SPLIT INTO FUNCTIONS FOR EACH
	 */

	protected JPanel getPanelFor(String valFxn){
		valParams = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy=0;
		c.gridx=0;
		finalPanel = new JPanel(new GridBagLayout());
		inst = new JTextArea(getTextFor("inst", "valueInst"));
		inst.setEditable(false);
		inst.setFont(new Font("Ariel", Font.BOLD, 13));
		finalPanel.add(inst, c);

		if(valFxn.compareToIgnoreCase("additive")==0){


				valParams.add(new JLabel("<html>" + "Item" + "<br>" + "Name" + "</html>"));
				c.gridx=1;
				valParams.add(new JLabel("<html>" + "Min" + "<br>" + "alpha" + "</html>"));
				c.gridx=2;
				valParams.add(new JLabel("<html>" + "Max" + "<br>" + "alpha" + "</html>"));
				c.gridx=3;
				valParams.add(new JLabel("<html>" + "Min" + "<br>" + "beta" + "</html>"));
				c.gridx=4;
				valParams.add(new JLabel("<html>" + "Max" + "<br>" + "beta" + "</html>"));

				for(int j=0;j<state.itemNames.size();j++){
					JLabel itemLab = new JLabel(state.itemNames.get(j));
					JTextField[] itemValFields = new JTextField[4];
					itemValFields[0] = new JTextField("0", 5);
					itemValFields[1] = new JTextField("1", 5);
					itemValFields[2] = new JTextField("0", 5);
					itemValFields[3] = new JTextField("1", 5);

					c.gridy = j+1;
					valParamForItem.put(itemLab.getText(), itemValFields);

					for(int k=0; k<5;k++){
						c.gridx = k;
						if(k==0){

							valParams.add(itemLab, c);
						}else{
							valParams.add(itemValFields[k-1], c);
						}
					}
				}

			}else if(valFxn.compareToIgnoreCase("contract")==0){

				valParams.add(new JLabel("<html>" + "Item" + "<br>" + "Name" + "</html>"));
				c.gridx=1;
				valParams.add(new JLabel("<html>" + "Min #" + "<br>" + "items" + "</html>"));
				c.gridx=2;
				valParams.add(new JLabel("<html>" + "Max #" + "<br>" + "items" + "</html>"));
				c.gridx=3;
				valParams.add(new JLabel("<html>" + "Min" + "<br>" + "value" + "</html>"));
				c.gridx=4;
				valParams.add(new JLabel("<html>" + "Max" + "<br>" + "value" + "</html>"));

				for(int j=0;j<state.itemNames.size();j++){
					JLabel itemLab = new JLabel(state.itemNames.get(j));
					JTextField[] itemValFields = new JTextField[4];
					itemValFields[0] = new JTextField("0", 5);
					itemValFields[1] = new JTextField("1", 5);
					itemValFields[2] = new JTextField("0", 5);
					itemValFields[3] = new JTextField("1", 5);

					c.gridy = j+1;
					valParamForItem.put(itemLab.getText(), itemValFields);

					for(int k=0; k<5;k++){
						c.gridx = k;
						if(k==0){

							valParams.add(itemLab, c);
						}else{
							valParams.add(itemValFields[k-1], c);
						}
					}
				}
				c.gridy = state.itemNames.size()+1;
				c.gridx = 0;
				valParams.add(new JLabel("Other Parameter "),c);
				c.gridy = state.itemNames.size()+2;
				c.gridx = 0;

				valParams.add(new JLabel("Number of Contracts per Agent: "),c);
				c.gridx = 1;
				numContrField =new JTextField("5", 3);
				valParams.add(numContrField,c);

			}else if(valFxn.compareToIgnoreCase("schedule")==0){

				valParams.add(new JLabel("Min Value"),c);
				c.gridx=1;
				JTextField[] fieldMinV = new JTextField[1];
				fieldMinV[0] = new JTextField("0", 8);
				valParams.add(fieldMinV[0],c);
				valParamForItem.put("Min Value",fieldMinV);

				c.gridy=1;
				c.gridx =0;
				valParams.add(new JLabel("Max Value"),c);
				c.gridx =1;
				JTextField[] fieldMaxV = new JTextField[1];
				fieldMaxV[0] = new JTextField("100", 8);
				valParams.add(fieldMaxV[0],c);
				valParamForItem.put("Max Value", fieldMaxV);

				//These values are set by the auction and item tabs
				//c.gridy=3;
				//valParams.add(new JLabel("Min number of items for sale"));
				//valParams.add(new JTextField("1"), 3);
				//c.gridy=3;
				//valParams.add(new JLabel("Max number of items for sale"));
				//valParams.add(new JTextField("8"), 3);
				c.gridy=2;
				c.gridx =0;
				valParams.add(new JLabel("Min number of items required for task:  "),c);
				c.gridx =1;
				JTextField[] fieldMinN = new JTextField[1];
				fieldMinN[0] = new JTextField("1",3);
				valParams.add(fieldMinN[0] ,c);
				valParamForItem.put("Min Number", fieldMinN);

				c.gridy=3;
				c.gridx =0;
				valParams.add(new JLabel("Max number of items required for task:  "),c);
				c.gridx =1;
				JTextField[] fieldMaxN = new JTextField[1];
				fieldMaxN[0]=new JTextField("8",3);
				valParams.add(fieldMaxN[0],c);
				valParamForItem.put("Max Number", fieldMaxN);

				c.gridy=4;
				c.gridx =0;
				valParams.add(new JLabel("Earlist Deadline:"),c);
				c.gridx =1;
				JTextField[] fieldEarlyD = new JTextField[1];
				fieldEarlyD[0] = new JTextField("1",3);
				valParams.add(fieldEarlyD[0],c);
				valParamForItem.put("Min Deadline", fieldEarlyD);

				c.gridy=5;
				c.gridx =0;
				valParams.add(new JLabel("Latest Deadline:"),c);
				c.gridx =1;
				JTextField[] fieldLateD = new JTextField[1];
				fieldLateD[0] = new JTextField(Integer.toString(Math.max(state.getMaxSeq(), state.getMaxSim())), 3);
				valParams.add(fieldLateD[0],c);
				valParamForItem.put("Max Deadline", fieldLateD);

			}else{
				System.out.println("Can't find correct value fxn");
			}
		c.gridy= 1;
		c.gridx = 0;
		finalPanel.add(valParams, c);
		return finalPanel;

	}


	/*
	 * getTextFor gets the text file that describes a selected value function.
	 * These files can be edited.
	 */
	private String getTextFor(String folder, String val){
		StringBuilder builder = new StringBuilder();
	    try {
	    	File input = new File("src/gui/"+folder+"/"+val+".txt");
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

	/*
	 * ActionListener that waits for the user to select a value type and
	 * then displays the text describing that function and sets the state chosen
	 * to that value type
	 */
	protected class TextSelected implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String text;
			text = getTextFor("vals", valuations[c.getSelectedIndex()]);
			t.setText(text);
			state.setValueFxn(valuations[c.getSelectedIndex()]);

		}
	}

	/*
	 * getValSelector returns the valSelector JPanel for the Parameters Tab
	 */
	public JPanel getValSelector(){
		return valSelector;
	}

	/*
	 * updateState() updates the game state based on the state of the data
	 * structures that hold the parameter data
	 */
	protected void updateState(){
		state.setValParam(valParamForItem);
		//set parameters for contracts
		if(state.getValueFxn().compareToIgnoreCase("contract")==0){
			state.setNumContracts(Integer.parseInt(numContrField.getText()));
		}
		//System.out.println("val updating state, size: "+valParamForItem.size());

	}

}
