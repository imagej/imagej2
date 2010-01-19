/** This plugin implements the Plugins/Utilities/Find Commands 
    command. It provides an easy user interface to finding commands 
    you might know the name of without having to go through
    all the menus.  If you type a part of a command name, the box
    below will only show commands that match that substring (case
    insensitively).  If only a single command matches then that
    command can be run by hitting Enter.  If multiple commands match,
    they can be selected by selecting with the mouse and clicking
    "Run"; alternatively hitting the up or down arrows will move the
    keyboard focus to the list and the selected command can be run
    with Enter.  Double-clicking on a command in the list should
    also run the appropriate command.

    @author Mark Longair <mark-imagej@longair.net>
    @author Johannes Schindelin <johannes.schindelin@gmx.de>
 */

package ij.plugin;

import ij.*;
import ij.text.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuBar;
import java.awt.Container;

import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.KeyListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.TextEvent;
import java.awt.event.WindowEvent;

import java.awt.Dimension;
import java.awt.Point;

public class CommandFinder implements PlugIn, ActionListener, WindowListener, KeyListener, ItemListener, MouseListener {

	public CommandFinder() {
		Toolkit toolkit=Toolkit.getDefaultToolkit();
		Integer interval=(Integer)toolkit.getDesktopProperty("awt.multiClickInterval");
		if (interval==null)
			// Hopefully 300ms is a sensible default when the property
			// is not available.
			multiClickInterval=300;
		else
			multiClickInterval=interval.intValue();
	}

	class CommandAction {
		CommandAction(String classCommand, MenuItem menuItem, String menuLocation) {
			this.classCommand = classCommand;
			this.menuItem = menuItem;
			this.menuLocation = menuLocation;
		}
		String classCommand;
		MenuItem menuItem;
		String menuLocation;
		public String toString() {
			return "classCommand: " + classCommand + ", menuItem: "+menuItem+", menuLocation: "+menuLocation;
		}
	}

	int multiClickInterval;
	long lastClickTime=Long.MIN_VALUE;
	String lastClickedItem;
	JFrame d;
	JTextField prompt;
	JList completions;
	JScrollPane scrollPane;
	DefaultListModel completionsModel;
	JButton runButton, closeButton, exportButton;
	JCheckBox fullInfoCheckBox, closeCheckBox;
	Hashtable commandsHash;
	String [] commands;
	Hashtable listLabelToCommand;
	static boolean closeWhenRunning = Prefs.get("command-finder.close", true);
;

	protected String makeListLabel(String command, CommandAction ca, boolean fullInfo) {
		if (fullInfo) {
			String result = command;
			if( ca.menuLocation != null)
				result += " (in " + ca.menuLocation + ")";
			if( ca.classCommand != null )
				result += " [" + ca.classCommand + "]";
			String jarFile = Menus.getJarFileForMenuEntry(command);
			if( jarFile != null )
				result += " {from " + jarFile + "}";
			return result;
		} else {
			return command;
		}
	}

	protected void populateList(String matchingSubstring) {
		boolean fullInfo=fullInfoCheckBox.isSelected();
		String substring = matchingSubstring.toLowerCase();
		completionsModel.removeAllElements();
		for(int i=0; i<commands.length; ++i) {
			String commandName = commands[i];
			if (commandName.length()==0)
				continue;
			String lowerCommandName = commandName.toLowerCase();
			if( lowerCommandName.indexOf(substring) >= 0 ) {
				CommandAction ca = (CommandAction)commandsHash.get(commandName);
				String listLabel = makeListLabel(commandName, ca, fullInfo);
				completionsModel.addElement(listLabel);
			}
		}
	}

	private static class LevenshteinPair implements Comparable {
		int index, cost;

		LevenshteinPair(int index, int cost) {
			this.index = index;
			this.cost = cost;
		}

		public int compareTo(Object o) {
			return cost - ((LevenshteinPair)o).cost;
		}
	}

	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if (source==runButton) {
			String selected = (String)completions.getSelectedValue();
			if(selected==null) {
				IJ.error("Please select a command to run");
				return;
			}
			runFromLabel(selected);
		} else if (source == exportButton) {
			export();
		} else if (source == closeButton) {
			d.dispose();
		}
	}

	public void itemStateChanged(ItemEvent ie) {
		populateList(prompt.getText());
	}

	public void mouseClicked(MouseEvent e) {
		long now=System.currentTimeMillis();
		String justClickedItem=(String)completions.getSelectedValue();
		// Is this fast enough to be a double-click?
		long thisClickInterval=now-lastClickTime;
		if (thisClickInterval<multiClickInterval) {
			if (justClickedItem!=null&&
			    lastClickedItem!=null&&
			    justClickedItem.equals(lastClickedItem)) {
				runFromLabel(justClickedItem);
			}
		}
		lastClickTime=now;
		lastClickedItem=justClickedItem;
	}

	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	void export() {
		StringBuffer sb = new StringBuffer(5000);
		for (int i=0; i<completionsModel.size(); i++) {
			sb.append(i);
			sb.append("\t");
			sb.append((String)completionsModel.elementAt(i));
			sb.append("\n");
		}
		TextWindow tw = new TextWindow("ImageJ Menu Commands", " \tCommand", sb.toString(), 600, 500);
	}

	protected void runFromLabel(String listLabel) {
		String command = (String)listLabelToCommand.get(listLabel);
		CommandAction ca = (CommandAction)commandsHash.get(command);
		//if (ca.classCommand != null ) {
		IJ.showStatus("Running command "+ca.classCommand);
		IJ.doCommand(command);
		//} else if (ca.menuItem != null) {
		//	IJ.showStatus("Clicking menu item "+ca.menuLocation+" > "+command);
		//	ActionEvent ae = new ActionEvent(ca.menuItem, ActionEvent.ACTION_PERFORMED, command);
		//	ActionListener [] als = ca.menuItem.getActionListeners();
		//	for (int i=0; i<als.length; ++i)
		//		als[i].actionPerformed(ae);
		//} else {
		//	IJ.error("BUG: nothing to run found for '"+listLabel+"'");
		//	return;
		//}
		closeWhenRunning = closeCheckBox.isSelected();
		if (closeWhenRunning)
			d.dispose();
	}

	public void keyPressed(KeyEvent ke) {
		int key = ke.getKeyCode();
		int items = completionsModel.getSize();
		Object source = ke.getSource();
		if (key==KeyEvent.VK_ESCAPE) {
			d.dispose();
		} else if (source==prompt) {
			/* If you hit enter in the text field, and
			   there's only one command that matches, run
			   that: */
			if (key==KeyEvent.VK_ENTER) {
				if (1==items) {
					String selected = (String)completionsModel.elementAt(0);
					runFromLabel(selected);
				}
			}
			/* If you hit the up or down arrows in the
			   text field, move the focus to the
			   completions list and select the item at the
			   bottom or top of that list. */
			int index = -1;
			if (key==KeyEvent.VK_UP) {
				index = completions.getSelectedIndex() - 1;
				if (index < 0)
					index = items - 1;
			}
			else if (key==KeyEvent.VK_DOWN) {
				index = completions.getSelectedIndex() + 1;
				if (index >= items)
					index = Math.min(items - 1, 0);
			}
			else if (key==KeyEvent.VK_PAGE_DOWN)
				index = completions.getLastVisibleIndex();
			if (index>=0) {
				completions.requestFocus();
				completions.ensureIndexIsVisible(index);
				completions.setSelectedIndex(index);
			}
		} else if (key==KeyEvent.VK_BACK_SPACE) {
			/* If someone presses backspace they probably want to
			   remove the last letter from the search string, so
			   switch the focus back to the prompt: */
			prompt.requestFocus();
		} else if (source==completions) {
			/* If you hit enter with the focus in the
			   completions list, run the selected
			   command */
			if (key==KeyEvent.VK_ENTER) {
				String selected = (String)completions.getSelectedValue();
				if (selected!=null)
					runFromLabel(selected);
			}
			else if (key==KeyEvent.VK_UP) {
				if (completions.getSelectedIndex() <= 0) {
					completions.clearSelection();
					prompt.requestFocus();
				}
			}
			else if (key==KeyEvent.VK_DOWN) {
				if (completions.getSelectedIndex() == items-1) {
					completions.clearSelection();
					prompt.requestFocus();
				}
			}
		} else if (source==runButton) {
			if (key==KeyEvent.VK_ENTER) {
				String selected = (String)completions.getSelectedValue();
				if (selected!=null)
					runFromLabel(selected);
			}
		} else if (source==closeButton) {
			if (key==KeyEvent.VK_ENTER)
				d.dispose();
		}
	}

	public void keyReleased(KeyEvent ke) { }

	public void keyTyped(KeyEvent ke) { }

	class PromptDocumentListener implements DocumentListener {
		public void insertUpdate(DocumentEvent e) {
			populateList(prompt.getText());
		}
		public void removeUpdate(DocumentEvent e) {
			populateList(prompt.getText());
		}
		public void changedUpdate(DocumentEvent e) {
			populateList(prompt.getText());
		}
	}

	/* This function recurses down through a menu, adding to
	   commandsHash the location and MenuItem of any items it
	   finds that aren't submenus. */

	public void parseMenu(String path, Menu menu) {
		int n=menu.getItemCount();
		for (int i=0; i<n; ++i) {
			MenuItem m=menu.getItem(i);
			String label=m.getLabel();
			if (m instanceof Menu) {
				Menu subMenu=(Menu)m;
				parseMenu(path+" > "+label,subMenu);
			} else {
				String trimmedLabel = label.trim();
				if (trimmedLabel.length()==0 || trimmedLabel.equals("-"))
					continue;
				CommandAction ca=(CommandAction)commandsHash.get(label);
				if( ca == null )
					commandsHash.put(label, new CommandAction(null,m,path));
				else {
					ca.menuItem=m;
					ca.menuLocation=path;
				}
				CommandAction caAfter=(CommandAction)commandsHash.get(label);
			}
		}
	}

	/* Finds all the top level menus from the menu bar and
	   recurses down through each. */

	public void findAllMenuItems() {
		MenuBar menuBar = Menus.getMenuBar();
		int topLevelMenus = menuBar.getMenuCount();
		for (int i=0; i<topLevelMenus; ++i) {
			Menu topLevelMenu=menuBar.getMenu(i);
			parseMenu(topLevelMenu.getLabel(), topLevelMenu);
		}
	}

	public void run(String ignored) {

		commandsHash = new Hashtable();

		/* Find the "normal" commands; those which are
		   registered plugins: */

		Hashtable realCommandsHash = (Hashtable)(ij.Menus.getCommands().clone());

		Set realCommandSet = realCommandsHash.keySet();

		for (Iterator i = realCommandSet.iterator();
		     i.hasNext();) {
			String command = (String)i.next();
			// Some of these are whitespace only or separators - ignore them:
			String trimmedCommand = command.trim();
			if (trimmedCommand.length()>0 && !trimmedCommand.equals("-")) {
				commandsHash.put(command,
						 new CommandAction((String)realCommandsHash.get(command),
								   null,
								   null));
			}
		}

		/* There are some menu items that don't correspond to
		   plugins, such as those added by RefreshScripts, so
		   look through all the menus as well: */

		findAllMenuItems();

		/* Sort the commands, generate list labels for each
		   and put them into a hash: */

		commands = (String[])commandsHash.keySet().toArray(new String[0]);
		Arrays.sort(commands);

		listLabelToCommand = new Hashtable();

		for (int i=0; i<commands.length; ++i) {
			CommandAction ca = (CommandAction)commandsHash.get(commands[i]);
			listLabelToCommand.put(makeListLabel(commands[i], ca, true), commands[i]);
			listLabelToCommand.put(makeListLabel(commands[i], ca, false), commands[i]);
		}

		/* The code below just constructs the dialog: */

		ImageJ imageJ = IJ.getInstance();

		d = new JFrame("Command Finder") {
			public void setVisible(boolean visible) {
				if (visible)
					WindowManager.addWindow(this);
				super.setVisible(visible);
			}

			public void dispose() {
				WindowManager.removeWindow(this);
				super.dispose();
			}
		};
		Container contentPane = d.getContentPane();
		contentPane.setLayout(new BorderLayout());
		d.addWindowListener(this);

		fullInfoCheckBox = new JCheckBox("Show full information", false);
		fullInfoCheckBox.addItemListener(this);
		closeCheckBox = new JCheckBox("Close when running", closeWhenRunning);
		closeCheckBox.addItemListener(this);

		JPanel northPanel = new JPanel();

		northPanel.add(new JLabel("Type part of a command:"));

		prompt = new JTextField("", 30);
		prompt.getDocument().addDocumentListener(new PromptDocumentListener());
		prompt.addKeyListener(this);

		northPanel.add(prompt);

		contentPane.add(northPanel, BorderLayout.NORTH);

		completionsModel = new DefaultListModel();
		completions = new JList(completionsModel);
		scrollPane = new JScrollPane(completions);

		completions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		completions.setLayoutOrientation(JList.VERTICAL);

		completions.setVisibleRowCount(20);
		completions.addKeyListener(this);
		populateList("");

		contentPane.add(scrollPane, BorderLayout.CENTER);
		// Add a mouse listener so we can detect double-clicks
		completions.addMouseListener(this);

		runButton = new JButton("Run");
		exportButton = new JButton("Export");
		closeButton = new JButton("Close");

		runButton.addActionListener(this);
		exportButton.addActionListener(this);
		closeButton.addActionListener(this);
		runButton.addKeyListener(this);
		closeButton.addKeyListener(this);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());

		JPanel optionsPanel = new JPanel();
		optionsPanel.add(fullInfoCheckBox);
		optionsPanel.add(closeCheckBox);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(runButton);
		buttonsPanel.add(exportButton);
		buttonsPanel.add(closeButton);

		southPanel.add(optionsPanel, BorderLayout.CENTER);
		southPanel.add(buttonsPanel, BorderLayout.SOUTH);

		contentPane.add(southPanel, BorderLayout.SOUTH);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		d.pack();

		int dialogWidth = d.getWidth();
		int dialogHeight = d.getHeight();

		int screenWidth = (int)screenSize.getWidth();
		int screenHeight = (int)screenSize.getHeight();

		Point pos=imageJ.getLocationOnScreen();

		/* Generally try to position the dialog slightly
		   offset from the main ImageJ window, but if that
		   would push the dialog off to the screen to any
		   side, adjust it so that it's on the screen.
		*/
		int initialX = (int)pos.getX() + 38;
		int initialY = (int)pos.getY() + 84;

		if (initialX+dialogWidth>screenWidth)
			initialX = screenWidth-dialogWidth;
		if (initialX<0)
			initialX = 0;
		if (initialY+dialogHeight>screenHeight)
			initialY = screenHeight-dialogHeight;
		if (initialY<0)
			initialY = 0;

		d.setLocation(initialX,initialY);

		d.setVisible(true);
		d.toFront();
	}

	/* Make sure that clicks on the close icon close the window: */

	public void windowClosing(WindowEvent e) {
		d.dispose();
		Prefs.set("command-finder.close", closeWhenRunning);
 }

	public void windowActivated(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	public void windowClosed(WindowEvent e) { }
	public void windowOpened(WindowEvent e) { }
	public void windowIconified(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
}
