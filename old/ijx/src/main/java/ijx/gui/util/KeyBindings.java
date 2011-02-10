/*
from http://tips4java.wordpress.com/2008/10/10/key-bindings/
 */

package ijx.gui.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.filechooser.*;

public class KeyBindings implements ItemListener
{
	private	static final String PACKAGE = "javax.swing.";

	private static final String[] COLUMN_NAMES =
		{"Action", "When Focused", "When In Focused Window", "When Ancestor"};

	private static String selectedItem;

	private JComponent contentPane;
	private JMenuBar menuBar;
	private JTable table;
	private JComboBox comboBox;
	private Hashtable<String, DefaultTableModel> models;

	/*
	 *  Constructor
	 */
	public KeyBindings()
	{
		models = new Hashtable<String, DefaultTableModel>();

		contentPane = new JPanel( new BorderLayout() );
		contentPane.add(buildNorthComponent(), BorderLayout.NORTH);
		contentPane.add(buildCenterComponent(), BorderLayout.CENTER);

		resetComponents();
	}

	/*
	 *  The content pane should be added to a high level container
	 */
	public JComponent getContentPane()
	{
		return contentPane;
	}

	/*
	 *  A menu can also be added which provides the ability to switch
	 *  between different LAF's.
	 */
	public JMenuBar getMenuBar()
	{
		if (menuBar == null)
			menuBar = createMenuBar();

		return menuBar;
	}

	/*
	 *  This panel is added to the North of the content pane
	*/
	private JComponent buildNorthComponent()
	{
		comboBox = new JComboBox();

		JLabel label = new JLabel("Select Component:");
		label.setDisplayedMnemonic('S');
		label.setLabelFor( comboBox );

		JPanel panel = new JPanel();
		panel.setBorder( new EmptyBorder(15, 0, 15, 0) );
		panel.add( label );
		panel.add( comboBox );
		return panel;
	}

	/*
	 *  Check the key name to see if it is the UI property
	 */
	private String checkForUIKey(String key)
	{
			if (key.endsWith("UI")
			&&  key.indexOf(".") == -1)
			{
				String componentName = key.substring(0, key.length() - 2);

				//  Ignore these components

				if (componentName.equals("PopupMenuSeparator")
				||  componentName.equals("ToolBarSeparator")
				||  componentName.equals("DesktopIcon"))
					return null;
				else
					return componentName;
			}

			return null;
	}

	/*
	**  Build the emtpy table to be added in the Center
	*/
	private JComponent buildCenterComponent()
	{
		DefaultTableModel model = new DefaultTableModel(COLUMN_NAMES, 0);

		table = new JTable(model)
		{
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};

		table.setAutoCreateColumnsFromModel( false );
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);
		table.getColumnModel().getColumn(3).setPreferredWidth(200);
		Dimension d = table.getPreferredSize();
		d.height = 350;
		table.setPreferredScrollableViewportSize( d );

		table.getTableHeader().setFocusable( true );

		return new JScrollPane( table );
	}

	/*
	 *  When the LAF is changed we need to reset all the items
	 */
	public void resetComponents()
	{
		models.clear();
		((DefaultTableModel)table.getModel()).setRowCount(0);

//		buildItemsMap();

		Vector<String> comboBoxItems = new Vector<String>(50);
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();

		//  All Swing components will have a UI property.

		for (Object key : defaults.keySet())
		{
			String componentName = checkForUIKey( key.toString() );

			if (componentName != null)
			{
				comboBoxItems.add( componentName );
			}
		}

		Collections.sort( comboBoxItems );

		comboBox.removeItemListener( this );
		comboBox.setModel( new DefaultComboBoxModel( comboBoxItems ) );
		comboBox.setSelectedIndex(-1);
		comboBox.addItemListener( this );
		comboBox.requestFocusInWindow();

		if (selectedItem != null)
			comboBox.setSelectedItem(selectedItem);
	}

	/**
	 *  Create menu bar
	 */
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		menuBar.add( createFileMenu() );
		menuBar.add( createLAFMenu() );

		return menuBar;
	}

	/**
	 *  Create menu items for the Application menu
	 */
	private JMenu createFileMenu()
	{
		JMenu menu = new JMenu("Application");
		menu.setMnemonic('A');

		menu.addSeparator();
		menu.add( new ExitAction() );

		return menu;
	}

	/**
	 *  Create menu items for the Look & Feel menu
	 */
	private JMenu createLAFMenu()
	{
		ButtonGroup bg = new ButtonGroup();

		JMenu menu = new JMenu("Look & Feel");
		menu.setMnemonic('L');

		String lafId = UIManager.getLookAndFeel().getID();
		UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();

		for (int i = 0; i < lafInfo.length; i++)
		{
			String laf = lafInfo[i].getClassName();
			String name= lafInfo[i].getName();

			Action action = new ChangeLookAndFeelAction(laf, name);
			JRadioButtonMenuItem mi = new JRadioButtonMenuItem( action );
			menu.add( mi );
			bg.add( mi );

			if (name.equals(lafId))
			{
				mi.setSelected(true);
			}
		}

		return menu;
	}

	/*
	 *  Implement the ItemListener interface
	 */
	public void itemStateChanged(ItemEvent e)
	{
		String componentName = (String)e.getItem();
		changeTableModel( getClassName(componentName) );
		selectedItem = componentName;
	}

	/*
	 *  Use the component name to build the class name
	 */
	private String getClassName(String componentName)
	{
		//  The table header is in a child package

		if (componentName.equals("TableHeader"))
			return PACKAGE + "table.JTableHeader";
		else
			return PACKAGE + "J" + componentName;
	}

	/*
	 *  Change the TabelModel in the table for the selected component
	 */
	private void changeTableModel(String className)
	{
		//  Check if we have already built the table model for this component

		DefaultTableModel model = models.get( className );

		if (model != null)
		{
			table.setModel( model );
			return;
		}

		//  Create an empty table to start with

		model = new DefaultTableModel(COLUMN_NAMES, 0);
		table.setModel( model );
		models.put(className, model);

		//  Create an instance of the component so we can get the default
		//  Action map and Input maps

		JComponent component = null;

		try
		{
			//  Hack so I don't have to sign the jar file for usage in
			//  Java Webstart

			if (className.endsWith("JFileChooser"))
			{
				component = new JFileChooser( new DummyFileSystemView() );
			}
			else
			{
				Object o = Class.forName(className).newInstance();
				component = (JComponent)o;
			}
		}
		catch(Exception e)
		{
			Object[] row = {e.toString(), "", "", ""};
			model.addRow( row );
			return;
		}

		//  Not all components have Actions defined

		ActionMap actionMap = component.getActionMap();
		Object[] keys = actionMap.allKeys();

		if  (keys == null)
		{
			Object[] row = {"No actions found", "", "", ""};
			model.addRow( row );
			return;
		}

		//  In some ActionMaps a key of type Object is found (I have no idea why)
		//  which causes a ClassCastExcption when sorting so we will ignore it
		//  by converting that entry to the empty string

		for (int i = 0; i < keys.length; i++)
		{
			Object key = keys[i];

			if (key instanceof String)
				continue;
			else
				keys[i] = "";
		}

		Arrays.sort( keys );

		//  Create a new row in the model for every Action found

		for (int i = 0; i < keys.length; i++)
		{
			Object key = keys[i];

			if (key != "")
			{
				Object[] row = {key, "", "", ""};
				model.addRow( row );
			}
		}

		//  Now check each InputMap to see if a KeyStroke is bound the the Action

		updateModelForInputMap(model, 1, component.getInputMap(JComponent.WHEN_FOCUSED));
		updateModelForInputMap(model, 2, component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW));
		updateModelForInputMap(model, 3, component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
	}

	/*
	 *  The model is potentially update for each of the 3 different InputMaps
	 */
	private void updateModelForInputMap(TableModel model, int column, InputMap inputMap)
	{
		if (inputMap == null) return;

		KeyStroke[] keys = inputMap.allKeys();

		if (keys == null) return;

		//  The InputMap is keyed by KeyStroke, however we want to be able to
		//  access the action names that are bound to a KeyStroke so we will create
		//  a Hashtble that is keyed by action name.
		//  Note that multiple KeyStrokes can be bound to the same action name.

		Hashtable<Object, String> actions = new Hashtable<Object, String>(keys.length);

		for (int i = 0; i < keys.length; i++)
		{
			KeyStroke key = keys[i];
			Object actionName = inputMap.get( key );

			Object value = actions.get( actionName );

			if (value == null)
			{
				actions.put(actionName, key.toString().replace("pressed ", ""));
			}
			else
			{
				actions.put(actionName, value + ", " + key.toString().replace("pressed ", ""));
			}
		}

		//  Now we can update the model for those actions that have
		//  KeyStrokes mapped to them

		for (int i = 0; i < model.getRowCount(); i++)
		{
			String o = actions.get(model.getValueAt(i, 0));

			if (o != null)
			{
				model.setValueAt(o.toString(), i, column);
			}
		}
	}

	/*
	 *  Change the LAF and recreate the UIManagerDefaults so that the properties
	 *  of the new LAF are correctly displayed.
	 */
	class ChangeLookAndFeelAction extends AbstractAction
	{
		private String laf;

		protected ChangeLookAndFeelAction(String laf, String name)
		{
			this.laf = laf;
			putValue(Action.NAME, name);
			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
		}

		public void actionPerformed(ActionEvent e)
		{
			try
			{
				JMenuItem mi = (JMenuItem)e.getSource();
				JPopupMenu popup = (JPopupMenu)mi.getParent();
				JRootPane rootPane = SwingUtilities.getRootPane(popup.getInvoker());
				Component c = rootPane.getContentPane().getComponent(0);
				rootPane.getContentPane().remove(c);

				UIManager.setLookAndFeel(laf);
				KeyBindings bindings = new KeyBindings();
				rootPane.getContentPane().add( bindings.getContentPane() );
				SwingUtilities.updateComponentTreeUI( rootPane );
				rootPane.requestFocusInWindow();
			}
			catch (Exception ex)
			{
				System.out.println("Failed loading L&F: " + laf);
				System.out.println(ex);
			}
		}
	}

	/*
	 *	Close the frame
	 */
	class ExitAction extends AbstractAction
	{
		public ExitAction()
		{
			putValue(Action.NAME, "Exit");
			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
		}

		public void actionPerformed(ActionEvent e)
		{
			System.exit(0);
		}
	}


	/**
	 *  Dummy FileSystemView to get around the Security Exception when trying to
	 *  access the File System in Java Webstart.
	 */
	class DummyFileSystemView extends FileSystemView
	{
		@Override
		public File createNewFolder(File containingDir)
		{
			return null;
		}

		@Override
		public File getDefaultDirectory()
		{
			return null;
		}

		@Override
		public File getHomeDirectory()
		{
			return null;
		}
	}

	/*
	 *  Build a GUI using the content pane and menu bar of KeyBindings
	 */
	private static void createAndShowGUI()
	{
		KeyBindings application = new KeyBindings();

		JFrame.setDefaultLookAndFeelDecorated(true);
   		JFrame frame = new JFrame("Key Bindings");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar( application.getMenuBar() );
  		frame.getContentPane().add(application.getContentPane());
   		frame.pack();
		frame.setLocationRelativeTo( null );
   	   	frame.setVisible( true );
	}

	/**
	 * KeyBindings Main. Called only if we're an application, not an applet.
	 */
	public static void main(String[] args)
	{
//		UIManager.put("swing.boldMetal", Boolean.FALSE);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				createAndShowGUI();
			}
		});
	}
}
