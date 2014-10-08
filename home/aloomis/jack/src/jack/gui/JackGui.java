package jack.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jack.server.*;

/*
 * @author Elizabeth Hilliard betsy@cs.brown.edu
 *
 * JACK GUI is a front end for the Java Auction Configuration Kit.
 * It's main purpose is to take user input and create the configuration files
 * needed to run a game in JACK.
 */

public class JackGui {
	//the different information panels
	protected BasicSetup setup;
	protected Auctions auctions;
	protected Items items;
	protected ValueParams valuation;
	protected GameLoader loader;
	protected AuctionParams auctionparam;

	//the main frame and game state
	protected JFrame jack;
	protected State state;
	protected JTabbedPane tabPanes;

	//toolbar buttons and
	protected JPanel toolbar;
	protected JButton submit;
	//protected JButton makefiles;
	protected JButton newButton;
	protected JButton saveButton;
	protected JButton loadButton;
	protected JButton runButton;
	protected JFrame nameWarning;

	public JackGui(){
		//create main frame for gui, label it, set size to avoid shifting information
		jack = new JFrame( "JACK: Java Auction Configuration Kit");
		jack.getContentPane().setLayout(new BorderLayout());
		jack.setPreferredSize(new Dimension(1000,800));
		state = new State();

		//create a toolbar of main operations
		toolbar = new JPanel(new FlowLayout());
		newButton = new JButton("New Game");
		newButton.addActionListener(new NewGame());
		runButton = new JButton("Run Server");
		runButton.addActionListener(new StartGame());
		saveButton = new JButton("Save Game");
		saveButton.addActionListener(new SaveFiles());
		loadButton = new JButton("Load Game");
		loadButton.addActionListener(new PickGameToLoad());
		toolbar.add(newButton); // clears state
		toolbar.add(saveButton); //creates and saves files
		toolbar.add(loadButton); // clears files and loads new state
		toolbar.add(runButton);	//writes, saves and runs server
		//create classes for each of the tabs that handle their panels
		setup = new BasicSetup(state);
		auctions = new Auctions(state);
		items = new Items(state);
		valuation = new ValueParams(state);
		auctionparam = new AuctionParams(state);
		//loader = new GameLoader();

		//create and add all of the tabs for the different information
		tabPanes = new JTabbedPane();

		tabPanes.addTab("Basic Setup", null, setup.getBasicConfigs(), "Configure basic parameters");
		tabPanes.addTab("Auction Schedule", null, auctions.getTableInfo(), "Set auction schedule");
		tabPanes.addTab("Goods Schedule", null, items.getTableInfo(), "Schedule goods");
		tabPanes.addTab("Auction Parameters", null, auctionparam.getTableInfo(), "set parameters for auctions");
		tabPanes.addTab("Value Type", null, valuation.getValSelector(), "Pick a value type");
		tabPanes.addTab("Value Parameters", null, null, "Set valuation Parameters");


		tabPanes.setEnabledAt(1, false);
		tabPanes.setEnabledAt(2, false);
		tabPanes.setEnabledAt(3, false);
		tabPanes.setEnabledAt(4, false);
		tabPanes.setEnabledAt(5, false);
		//create a next button that saves state and moves to the next tab.
		submit = new JButton("Next");
		submit.setActionCommand("next");
		submit.addActionListener(new SubmitClicked());
		tabPanes.addChangeListener(new TabChanged());
		//add the toolbar, tabs and next button to the Frame
		jack.add(submit, BorderLayout.SOUTH);
		jack.add(toolbar, BorderLayout.NORTH);
		jack.add(tabPanes, BorderLayout.CENTER);

	}

	/*
	 * This method ensures that the game name entered is not already in use.
	 * It prompts the user, with a popup, to pick a new game name.
	 */
	private boolean checkName(){
		boolean ok = true;
		File file = new File("src");

	    File[] files = file.listFiles();

		for (int i = 0; i < files.length;i++){
			//System.out.println(files[i].getName());
			//if the name exists, create the popup
			if(files[i].isDirectory()&& files[i].getName().compareToIgnoreCase(setup.nameF.getText())==0){
				nameWarning = new JFrame();
				JPanel warning = new JPanel(new FlowLayout());
				nameWarning.add(warning);
				warning.add(new JLabel("This name is taken. Try again."));
				nameWarning.pack();
				nameWarning.setPreferredSize(new Dimension(250,250));
				nameWarning.getDefaultCloseOperation();
				nameWarning.setVisible(true);
				ok = false;
			}
		}
		return ok;
	}

	//activates if the user changes a tab, sets the correct label
	// on the next/submit button
	public class TabChanged implements ChangeListener{
		@Override
		public void stateChanged(ChangeEvent e) {
			if(tabPanes.getSelectedIndex() < 5){
				submit.setText("Next");
			}else{
				submit.setText("Submit Parameters");
			}
		}
	}

	/*
	 * handles when the next button (used to submit the parameters
	 * and move forward) is pressed.
	 */
	public class SubmitClicked implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {

		//updates state for basicsetup and creates auction and item tabs
		if(tabPanes.getSelectedIndex() ==0){
			if(checkName()){
				try {
					saveConfigFiles("auctions");
					saveConfigFiles("server");
					saveConfigFiles("valuations");
				}catch (IOException e1) {
					e1.printStackTrace();
				}
				clearConfigFiles("auctions");
				clearConfigFiles("server");
				clearConfigFiles("valuations");
				setup.updateState();
				tabPanes.setComponentAt(1, auctions.recreatePanel(state));
				tabPanes.setComponentAt(2, items.recreatePanel(state));
				//allow input to auction, item and value tabs
				tabPanes.setEnabledAt(1, true);
				tabPanes.setEnabledAt(2, true);
				tabPanes.setEnabledAt(3, true);
				tabPanes.setSelectedIndex(1);
			}
			//move forward	and update state
			}else if(tabPanes.getSelectedIndex() ==1){
				//auctions.updateState();
				tabPanes.setSelectedIndex(2);
				tabPanes.setComponentAt(3, auctionparam.recreatePanel(state));
				tabPanes.setEnabledAt(3, true);
			}else if(tabPanes.getSelectedIndex() ==2){
				items.updateState();
				tabPanes.setEnabledAt(4, true);
				tabPanes.setSelectedIndex(3);
			//set what panel will fill parameters tab
			}else if(tabPanes.getSelectedIndex() ==3){
				auctionparam.updateState();
				tabPanes.setSelectedIndex(4);
			//set what panel will fill parameters tab
			}else if(tabPanes.getSelectedIndex() ==4){
				tabPanes.setComponentAt(5, valuation.getPanelFor(state.getValueFxn()));
				tabPanes.setEnabledAt(5, true);
				tabPanes.setSelectedIndex(5);
			}else if(tabPanes.getSelectedIndex() == 5){
				submit.setText("Submit Parameters");
				valuation.updateState();
				//should produce pop-up that tells user to print and save files or run the server
				}
			}
		}

	//creates new window that allows the user to load a previously created game
	public class PickGameToLoad implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			loader = new GameLoader();
			JFrame loadWindow = new JFrame();
			JPanel loadWindowP = new JPanel(new FlowLayout());
			loadWindowP.add(loader.loaderPanel);
			JButton load = new JButton("Run Game");
			load.addActionListener(new LoadGame());
			loadWindowP.add(load);
			loadWindow.add(loadWindowP);
			loadWindow.pack();
			loadWindow.setPreferredSize(new Dimension(200,200));
			loadWindow.getDefaultCloseOperation();
			loadWindow.setVisible(true);
		}
	}

	//if the user chooses to load a game, set all of gui off limits for editing
	public class LoadGame implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			//loader.setGameToLoad();
			try {
				saveConfigFiles("auctions");
				saveConfigFiles("server");
				saveConfigFiles("valuations");
			} catch (IOException e2) {
					e2.printStackTrace();
			}
			clearConfigFiles("auctions");
			clearConfigFiles("server");
			clearConfigFiles("valuations");
			//System.out.println(loader.toLoad);
			loadPackageFiles(loader.gameToLoad, "auctions");
			loadPackageFiles(loader.gameToLoad, "server");
			loadPackageFiles(loader.gameToLoad, "valuations");

			tabPanes.setEnabledAt(0, false);
			tabPanes.setEnabledAt(1, false);
			tabPanes.setEnabledAt(2, false);
			tabPanes.setEnabledAt(3, false);
			tabPanes.setEnabledAt(4, false);

			try {
				String[] name = {state.gameName};
				AuctionServer.main(name);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}

	//starts whatever game is in the server/auction directories
	public class StartGame implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			state.printStateFiles();
			items.updateState();
			valuation.updateState();
			try {
				String[] name = {state.gameName};
				AuctionServer.main(name);

			} catch (IOException e1) {
				e1.printStackTrace();
			}

			/*}else if(tabPanes.getSelectedIndex() ==2){
				state.printStateFiles();
				System.out.println("printed files");*/
			}
		}

		/*
		public class ReloadGame implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				moveConfigFiles("auctions");
				moveConfigFiles("server");
				clearConfigFiles("auctions");
				clearConfigFiles("server");
				loadPackageFiles(loader.toLoad, "auctions");
				loadPackageFiles(loader.toLoad, "auctions");
			}
		}*/

		//loads the specific directory of files from the game folder to the JACK's server/suction directories
		public boolean loadPackageFiles(String gameNameStr, String packageName){
                    boolean success = true;
                    File file = new File("games/"+gameNameStr+"/"+packageName);

                    File[] files2 = file.listFiles();
                    //System.out.println("files size "+files2.length);
                    for (int i = 0; i < files2.length; i++){
                        //System.out.println("i: "+i);
                         File currentFile = files2[i];
                         File newFile = new File("games/"+packageName+"/"+currentFile.getName());
                         currentFile.renameTo(newFile);

                    }
                    return success;
		}

		//prints the game's config files
		protected class CreateFiles implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				clearConfigFiles("auctions");
				clearConfigFiles("server");
				clearConfigFiles("valuations");
				items.updateState();
				valuation.updateState();
				state.printStateFiles();
				//System.out.println("printed files");

			}

		}

		//clears files from JACK's server and auction directories
		protected class ClearFiles implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {

			try {
				saveConfigFiles("auctions");
				saveConfigFiles("server");
				saveConfigFiles("valuations");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			clearConfigFiles("auctions");
			clearConfigFiles("server");
			clearConfigFiles("valuations");
		}

	}

	//saves files that are in JACK's auction and server directories
	protected class SaveFiles implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			items.updateState();
			valuation.updateState();
			state.printStateFiles();
			//System.out.println("printed files");

			try {
				saveConfigFiles("auctions");
				saveConfigFiles("server");
				saveConfigFiles("valuations");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//clearConfigFiles("auctions");
			//clearConfigFiles("server");
			//tabPanes.setComponentAt(5, loader.createLoader());

		}
	}

	//saves any current files and clears the gui so that a new game can be designed
	protected class NewGame implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			clearConfigFiles("auctions");
			clearConfigFiles("server");
			clearConfigFiles("valuations");
			state = new State();
			setup = new BasicSetup(state);
			tabPanes.setComponentAt(0, setup.getBasicConfigs());
			tabPanes.setEnabledAt(0, true);
			tabPanes.setEnabledAt(1, false);
			tabPanes.setEnabledAt(2, false);
			tabPanes.setEnabledAt(3, false);
			tabPanes.setEnabledAt(4, false);
			tabPanes.setSelectedIndex(0);


		}

	}

	//depriciated method
	protected class TabChange implements ChangeListener{

		@Override
		public void stateChanged(ChangeEvent e) {
			setup.updateState();
			auctions.updateState();
			valuation.updateState();

		}

	}

	//clears all files in JACK's auctions or server directory
	//start should be the directory you want to clear
	public boolean clearConfigFiles(String start){

		boolean success = true;
		File file = new File("src/"+start);// where files have to be concatenated and move to c:\Archive before deleting

	    File[] files2 = file.listFiles();

	    for (int i = 0; i < files2.length; i++){
	    	 File currentFile = files2[i];
	    	 if(currentFile.getName().split("\\.")[1].compareToIgnoreCase("txt")==0){
	    		 if (!currentFile.delete()){
	    			 success = false;
	    			 // Failed to delete file
	    			 System.out.println("Failed to delete "+ currentFile.getName());
	    		 }
	    	 }
	    }
	    return success;
	}


	//saves the config files in a given (start) directory
	public boolean saveConfigFiles(String start) throws IOException{

		boolean success = true;
		File file = new File("src/"+start);

	    File[] files2 = file.listFiles();

	    String gameFolderName = "src/"+state.gameName;
		File gameFolder = new File(gameFolderName);

		File gameInsideFolder;
		if(gameFolder.exists() ){
			String gameInsideFolderS = "src/"+state.gameName+"/"+start;
		gameInsideFolder = new File(gameInsideFolderS);

			//System.out.println("deleting old folder");
			if(gameInsideFolder.exists()){
				File[] dirFiles = gameInsideFolder.listFiles();
				//System.out.println("size dirFiles "+dirFiles.length);
				for(File f: dirFiles){
					//System.out.println(f.delete());
					f.delete();
				}

			}else{
				gameInsideFolder.mkdir();
			}
		}else{
			gameFolder.mkdir();
			String gameInsideFolderS = "src/"+state.gameName+"/"+start;
			gameInsideFolder = new File(gameInsideFolderS);
			gameInsideFolder.mkdir();

		}

	    for (int i = 0; i < files2.length; i++){
	    	 File currentFile = files2[i];
	    	// System.out.println(currentFile.getName().split("\\.")[0]+"__________________");
	    	 if(currentFile.getName().split("\\.")[1].compareToIgnoreCase("txt")==0){
	    		//System.out.println(currentFile.getName().split("\\.")[0]);
	    		 File newFile = new File(gameInsideFolder.getPath()+"/"+currentFile.getName());
	    		 FileReader in = new FileReader(currentFile);
	    		 FileWriter out = new FileWriter(newFile);
	    		 int c;

	    		 while ((c = in.read()) != -1){
	    		      out.write(c);
	    		 }

	    		 in.close();
	    		 out.close();

	    	 }
	    }
	    return success;
	}

	//sets the gui visible and handles exiting the gui
	public void showGui(){

		jack.pack();
		jack.setBackground(Color.cyan);
		jack.setVisible(true);

		jack.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String args[]){

		JackGui jack = new JackGui();
		jack.showGui();

	}


}
