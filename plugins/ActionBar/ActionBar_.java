import ij.*;
import ij.plugin.PlugIn;

import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.net.URL;
import java.io.*;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

/**
 * @author Jerome Mutterer jerome.mutterer(at)ibmp-ulp.u-strasbg.fr
 * 
 */

public class ActionBar_ extends JPanel implements PlugIn, ActionListener {

	private static final long serialVersionUID = 7436194992984622141L;

	String macrodir = IJ.getDirectory("macros");
	String configfile = "";
	String configfilepath = "";
	String separator = System.getProperty("file.separator");
	
	public void run(String s) {

		// getting the right config file from arguments
		configfile = s;
		if (s.equals("")) {
			configfilepath = ActionBar_.class.getResource("ActionBarConf.txt")
					.getFile();
			configfile = configfilepath.substring(configfilepath
					.lastIndexOf(separator) + 1);
		} else {
			configfilepath = IJ.getDirectory("startup") + s;
			try {
				configfile = configfilepath.substring(configfilepath
						.lastIndexOf("/") + 1);
			} catch (Exception e) {

			}
		}

		// create a new toolbar
		JToolBar toolBar = new JToolBar();
		design(toolBar);
		toolBar.setFloatable(false);
		
		// create a frame and add the toolbar
		JFrame frame = new JFrame(configfile.substring(0,configfile.length()-4));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		frame.pack();
		frame.setVisible(true);

	}

	protected void design(JToolBar toolBar) {
		JButton button = null;
		try {
			File file = new File(configfilepath);
			if (!file.exists())
				IJ.error("Config File not found");
			BufferedReader r = new BufferedReader(new FileReader(file));
			while (true) {
				String s = r.readLine();
				if (s.equals(null))
					break;
				else if (s.startsWith("<button>")) {
					String label = r.readLine().substring(6);
					String icon = r.readLine().substring(5);
					String action = r.readLine().substring(7);
					String arg = r.readLine().substring(4);
					button = makeNavigationButton(icon, action + ";" + arg,
							label, label);
					toolBar.add(button);
				}
				// could think of adding different items here
			}
			r.close();
		} catch (Exception e) {
			
		}

		button = makeNavigationButton("ABmanage.gif", "manage",
				"Edit the ActionBar config file", "...");
		toolBar.add(button);
	}

	protected JButton makeNavigationButton(String imageName,
			String actionCommand, String toolTipText, String altText) {

		String imgLocation = "icons/" + imageName;
		URL imageURL = ActionBar_.class.getResource(imgLocation);
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		if (imageURL != null) {
			button.setIcon(new ImageIcon(imageURL, altText));
		} else {
			button.setText(altText);
			//IJ.log("Resource not found: " + imgLocation);
		}
		return button;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("manage".equals(cmd)) {
			if ((e.getModifiers() & InputEvent.ALT_MASK) != 0) {
				IJ.run("Edit...", "open=[" + configfilepath + "]");

				WindowManager.getFrame(configfile).addWindowListener(
						new WindowAdapter() {
							public void windowClosing(WindowEvent evt) {
								IJ.error("Close ActionBar and restart it");
							}
						});
			} else {

			}
		} else if (cmd.startsWith("run_macro_file")) {
			IJ.runMacroFile(cmd.substring(cmd.indexOf(";") + 1));
		} else if (cmd.startsWith("install_macro")) {
			IJ.run("Install...", "install=[" + macrodir + "/"
					+ cmd.substring(cmd.indexOf(";") + 1) + "]");
		} else if (cmd.startsWith("run_macro_string")) {
			IJ.runMacro(cmd.substring(cmd.indexOf(";") + 1));
		}
	}

}