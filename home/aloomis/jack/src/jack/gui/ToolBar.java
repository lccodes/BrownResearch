package jack.gui;
/*
 * @author Elizabeth Hilliard betsy@cs.brown.edu
 *
 * The Toolbar class handles the file-level actions of saving,
 * loading and running.
 *
 */
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class ToolBar {

	JPanel toolbar;
	JButton button1, button2, button3, button4;
	State state;

	public ToolBar(State state){
		toolbar = new JPanel(new FlowLayout());
		button1 = new JButton("Start Server");
		button2 = new JButton("Create Configuration Files");
		button2.addActionListener(new CreateFiles());
		//button3 = new JButton("Clear Auction Game");
		button4 = new JButton("Create New Game");
		toolbar.add(button1);
		toolbar.add(button2);
		//toolbar.add(button3);
		toolbar.add(button4);

	}

	protected class CreateFiles implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			state.printStateFiles();
			//System.out.println("printed files");

		}

	}

	protected JPanel getToolBar(){
		return toolbar;
	}
}
