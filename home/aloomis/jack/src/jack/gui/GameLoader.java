package jack.gui;


import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*
 * @author Elizabeth Hilliard betsy@cs.brown.edu
 *
 * The GameLoader class finds exisiting games and loads them into the correct
 * files so the server can use them to run a game.
 *
 * NOTE: Currently you cannot edit these files in the gui. You can, however,
 * edit them by hand.
 * This feature will be coming soon.
 */
public class GameLoader {

	protected JComboBox gameNameComboBox;
	protected JPanel loaderPanel;
	protected String gameToLoad;

        // Initializes the loader

	public GameLoader() {

                // Initialize the combobox

                gameNameComboBox = new JComboBox();
		gameNameComboBox.addActionListener(new TextSelected());

                // Scan the games directory and add directories to the combobox

                File gamesDirectory = new File("games");
                File[] games = gamesDirectory.listFiles();
                for (int i = 0; i < games.length; ++i) {
                    if (games[i].isDirectory()) {
                        gameNameComboBox.addItem(games[i].getName());
                    }
                }

                // Create the panel

		loaderPanel = new JPanel(new FlowLayout());
		loaderPanel.add(new JLabel("Select a Saved Game: "));
		loaderPanel.add(gameNameComboBox);
	}

	// Sets the game name to load based on what is selected

        protected class TextSelected implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			gameToLoad = (String)gameNameComboBox.getSelectedItem();
		}
	}

	/* DEPRECIATED
	protected void setGameToLoad(){
		toLoad = (String) c.getSelectedItem();
	}
        */

        /* OLD METHOD
	public boolean loadPackageFiles(String gameNameStr, String packageName){
		boolean success = true;
		File file = new File(System.getProperty("user.dir")+"/src/"+gameNameStr+"/"+packageName);

	    File[] files2 = file.listFiles();

	    for (int i = 0; i < files2.length; i++){
	    	 File currentFile = files2[i];
	    	 File newFile = new File(System.getProperty("user.dir")+"/src/"+packageName+"/"+currentFile.getName());
	    	 currentFile.renameTo(newFile);

	    }
	    return success;
	}
	*/
}
