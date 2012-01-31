package fiji.scripting;

import common.RefreshScripts;

import fiji.scripting.java.Refresh_Javas;

import ij.IJ;
import ij.WindowManager;

import ij.gui.GenericDialog;

import ij.io.SaveDialog;

import ij.plugin.BrowserLauncher;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import java.util.zip.ZipException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class TextEditor extends JFrame implements ActionListener,
	       ChangeListener {
	protected JTabbedPane tabbed;
	protected JMenuItem newFile, open, save, saveas, compileAndRun, compile, debug, close,
		  undo, redo, cut, copy, paste, find, replace, selectAll,
		  autocomplete, resume, terminate, kill, gotoLine,
		  makeJar, makeJarWithSource, removeUnusedImports,
		  sortImports, removeTrailingWhitespace, findNext, findPrevious,
		  openHelp, addImport, clearScreen, nextError, previousError,
		  openHelpWithoutFrames, nextTab, previousTab,
		  runSelection, extractSourceJar, toggleBookmark,
		  listBookmarks, openSourceForClass, newPlugin, installMacro,
		  openSourceForMenuItem, showDiff, commit, ijToFront,
		  openMacroFunctions, decreaseFontSize, increaseFontSize,
		  chooseFontSize, chooseTabSize, gitGrep, loadToolsJar, openInGitweb,
		  replaceTabsWithSpaces, replaceSpacesWithTabs, toggleWhiteSpaceLabeling,
		  zapGremlins;
	protected RecentFilesMenuItem openRecent;
	protected JMenu gitMenu, tabsMenu, fontSizeMenu, tabSizeMenu, toolsMenu, runMenu,
		  whiteSpaceMenu;
	protected int tabsMenuTabsStart;
	protected Set<JMenuItem> tabsMenuItems;
	protected FindAndReplaceDialog findDialog;
	protected JCheckBoxMenuItem autoSave, showDeprecation, wrapLines;
	protected JTextArea errorScreen = new JTextArea();

	protected final String templateFolder = "templates/";
	protected Languages.Language[] availableLanguages = Languages.getInstance().languages;

	protected int compileStartOffset;
	protected Position compileStartPosition;
	protected ErrorHandler errorHandler;

	public TextEditor(String path) {
		super("Script Editor");
		WindowManager.addWindow(this);

		// Initialize menu
		int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		int shift = ActionEvent.SHIFT_MASK;
		JMenuBar mbar = new JMenuBar();
		setJMenuBar(mbar);

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		newFile = addToMenu(file, "New",  KeyEvent.VK_N, ctrl);
		newFile.setMnemonic(KeyEvent.VK_N);
		open = addToMenu(file, "Open...",  KeyEvent.VK_O, ctrl);
		open.setMnemonic(KeyEvent.VK_O);
		openRecent = new RecentFilesMenuItem(this);
		openRecent.setMnemonic(KeyEvent.VK_R);
		file.add(openRecent);
		save = addToMenu(file, "Save", KeyEvent.VK_S, ctrl);
		save.setMnemonic(KeyEvent.VK_S);
		saveas = addToMenu(file, "Save as...", 0, 0);
		saveas.setMnemonic(KeyEvent.VK_A);
		file.addSeparator();
		makeJar = addToMenu(file, "Export as .jar", 0, 0);
		makeJar.setMnemonic(KeyEvent.VK_E);
		makeJarWithSource = addToMenu(file, "Export as .jar (with source)", 0, 0);
		makeJarWithSource.setMnemonic(KeyEvent.VK_X);
		file.addSeparator();
		close = addToMenu(file, "Close", KeyEvent.VK_W, ctrl);

		mbar.add(file);

		JMenu edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);
		undo = addToMenu(edit, "Undo", KeyEvent.VK_Z, ctrl);
		redo = addToMenu(edit, "Redo", KeyEvent.VK_Y, ctrl);
		edit.addSeparator();
		selectAll = addToMenu(edit, "Select All", KeyEvent.VK_A, ctrl);
		cut = addToMenu(edit, "Cut", KeyEvent.VK_X, ctrl);
		copy = addToMenu(edit, "Copy", KeyEvent.VK_C, ctrl);
		paste = addToMenu(edit, "Paste", KeyEvent.VK_V, ctrl);
		edit.addSeparator();
		find = addToMenu(edit, "Find...", KeyEvent.VK_F, ctrl);
		find.setMnemonic(KeyEvent.VK_F);
		findNext = addToMenu(edit, "Find Next", KeyEvent.VK_F3, 0);
		findNext.setMnemonic(KeyEvent.VK_N);
		findPrevious = addToMenu(edit, "Find Previous", KeyEvent.VK_F3, shift);
		findPrevious.setMnemonic(KeyEvent.VK_P);
		replace = addToMenu(edit, "Find and Replace...", KeyEvent.VK_H, ctrl);
		gotoLine = addToMenu(edit, "Goto line...", KeyEvent.VK_G, ctrl);
		gotoLine.setMnemonic(KeyEvent.VK_G);
		toggleBookmark = addToMenu(edit, "Toggle Bookmark", KeyEvent.VK_B, ctrl);
		toggleBookmark.setMnemonic(KeyEvent.VK_B);
		listBookmarks = addToMenu(edit, "List Bookmarks", 0, 0);
		listBookmarks.setMnemonic(KeyEvent.VK_O);
		edit.addSeparator();

		// Font adjustments
		decreaseFontSize = addToMenu(edit, "Decrease font size", KeyEvent.VK_MINUS, ctrl);
		decreaseFontSize.setMnemonic(KeyEvent.VK_D);
		increaseFontSize = addToMenu(edit, "Increase font size", KeyEvent.VK_PLUS, ctrl);
		increaseFontSize.setMnemonic(KeyEvent.VK_C);

		fontSizeMenu = new JMenu("Font sizes");
		fontSizeMenu.setMnemonic(KeyEvent.VK_Z);
		boolean[] fontSizeShortcutUsed = new boolean[10];
		ButtonGroup buttonGroup = new ButtonGroup();
		for (final int size : new int[] { 8, 10, 12, 16, 20, 28, 42 } ) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem("" + size + " pt");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					getEditorPane().setFontSize(size);
					updateTabAndFontSize(false);
				}
			});
			for (char c : ("" + size).toCharArray()) {
				int digit = c - '0';
				if (!fontSizeShortcutUsed[digit]) {
					item.setMnemonic(KeyEvent.VK_0 + digit);
					fontSizeShortcutUsed[digit] = true;
					break;
				}
			}
			buttonGroup.add(item);
			fontSizeMenu.add(item);
		}
		chooseFontSize = new JRadioButtonMenuItem("Other...", false);
		chooseFontSize.setMnemonic(KeyEvent.VK_O);
		chooseFontSize.addActionListener(this);
		buttonGroup.add(chooseFontSize);
		fontSizeMenu.add(chooseFontSize);
		edit.add(fontSizeMenu);

		// Add tab size adjusting menu
		tabSizeMenu = new JMenu("Tab sizes");
		tabSizeMenu.setMnemonic(KeyEvent.VK_T);
		ButtonGroup bg = new ButtonGroup();
		for (final int size : new int[] { 2, 4, 8 }) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem("" + size, size == 8);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					getEditorPane().setTabSize(size);
					updateTabAndFontSize(false);
				}
			});
			item.setMnemonic(KeyEvent.VK_0 + (size % 10));
			bg.add(item);
			tabSizeMenu.add(item);
		}
		chooseTabSize = new JRadioButtonMenuItem("Other...", false);
		chooseTabSize.setMnemonic(KeyEvent.VK_O);
		chooseTabSize.addActionListener(this);
		bg.add(chooseTabSize);
		tabSizeMenu.add(chooseTabSize);
		edit.add(tabSizeMenu);

		wrapLines = new JCheckBoxMenuItem("Wrap lines");
		wrapLines.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				getEditorPane().setLineWrap(wrapLines.getState());
			}
		});
		edit.add(wrapLines);
		edit.addSeparator();

		clearScreen = addToMenu(edit, "Clear output panel", 0, 0);
		clearScreen.setMnemonic(KeyEvent.VK_L);
		//edit.addSeparator();
		//autocomplete = addToMenu(edit, "Autocomplete", KeyEvent.VK_SPACE, ctrl);

		zapGremlins = addToMenu(edit, "Zap Gremlins", 0, 0);

		//autocomplete.setMnemonic(KeyEvent.VK_A);
		edit.addSeparator();
		addImport = addToMenu(edit, "Add import...", 0, 0);
		addImport.setMnemonic(KeyEvent.VK_I);
		removeUnusedImports = addToMenu(edit, "Remove unused imports", 0, 0);
		removeUnusedImports.setMnemonic(KeyEvent.VK_U);
		sortImports = addToMenu(edit, "Sort imports", 0, 0);
		sortImports.setMnemonic(KeyEvent.VK_S);
		mbar.add(edit);

		whiteSpaceMenu = new JMenu("Whitespace");
		whiteSpaceMenu.setMnemonic(KeyEvent.VK_W);
		removeTrailingWhitespace = addToMenu(whiteSpaceMenu, "Remove trailing whitespace", 0, 0);
		removeTrailingWhitespace.setMnemonic(KeyEvent.VK_W);
		replaceTabsWithSpaces = addToMenu(whiteSpaceMenu, "Replace tabs with spaces", 0, 0);
		replaceTabsWithSpaces.setMnemonic(KeyEvent.VK_S);
		replaceSpacesWithTabs = addToMenu(whiteSpaceMenu, "Replace spaces with tabs", 0, 0);
		replaceSpacesWithTabs.setMnemonic(KeyEvent.VK_T);
		toggleWhiteSpaceLabeling = new JRadioButtonMenuItem("Label whitespace");
		toggleWhiteSpaceLabeling.setMnemonic(KeyEvent.VK_L);
		toggleWhiteSpaceLabeling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getTextArea().setWhitespaceVisible(toggleWhiteSpaceLabeling.isSelected());
			}
		});
		whiteSpaceMenu.add(toggleWhiteSpaceLabeling);

		edit.add(whiteSpaceMenu);


		JMenu languages = new JMenu("Language");
		languages.setMnemonic(KeyEvent.VK_L);
		ButtonGroup group = new ButtonGroup();
		for (final Languages.Language language :
		                Languages.getInstance().languages) {
			JRadioButtonMenuItem item =
			        new JRadioButtonMenuItem(language.menuLabel);
			if (language.shortCut != 0)
				item.setMnemonic(language.shortCut);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setLanguage(language);
				}
			});

			group.add(item);
			languages.add(item);
			language.setMenuItem(item);
		}
		mbar.add(languages);

		JMenu templates = new JMenu("Templates");
		templates.setMnemonic(KeyEvent.VK_T);
		addTemplates(templates);
		mbar.add(templates);

		runMenu = new JMenu("Run");
		runMenu.setMnemonic(KeyEvent.VK_R);

		compileAndRun = addToMenu(runMenu, "Compile and Run",
				KeyEvent.VK_R, ctrl);
		compileAndRun.setMnemonic(KeyEvent.VK_R);

		runSelection = addToMenu(runMenu, "Run selected code",
				KeyEvent.VK_R, ctrl | shift);
		runSelection.setMnemonic(KeyEvent.VK_S);

		compile = addToMenu(runMenu, "Compile",
				KeyEvent.VK_C, ctrl | shift);
		compile.setMnemonic(KeyEvent.VK_C);
		autoSave = new JCheckBoxMenuItem("Auto-save before compiling");
		runMenu.add(autoSave);
		showDeprecation = new JCheckBoxMenuItem("Show deprecations");
		runMenu.add(showDeprecation);

		installMacro = addToMenu(runMenu, "Install Macro",
				KeyEvent.VK_I, ctrl);
		installMacro.setMnemonic(KeyEvent.VK_I);

		runMenu.addSeparator();
		nextError = addToMenu(runMenu, "Next Error", KeyEvent.VK_F4, 0);
		nextError.setMnemonic(KeyEvent.VK_N);
		previousError = addToMenu(runMenu, "Previous Error", KeyEvent.VK_F4, shift);
		previousError.setMnemonic(KeyEvent.VK_P);

		runMenu.addSeparator();

		kill = addToMenu(runMenu, "Kill running script...", 0, 0);
		kill.setMnemonic(KeyEvent.VK_K);
		kill.setEnabled(false);

		runMenu.addSeparator();

		debug = addToMenu(runMenu, "Start Debugging", KeyEvent.VK_D, ctrl);
		debug.setMnemonic(KeyEvent.VK_D);
		resume = addToMenu(runMenu, "Resume", 0, 0);
		resume.setMnemonic(KeyEvent.VK_R);
		terminate = addToMenu(runMenu, "Terminate", 0, 0);
		terminate.setMnemonic(KeyEvent.VK_T);
		mbar.add(runMenu);

		toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_O);
		openHelpWithoutFrames = addToMenu(toolsMenu,
			"Open Help for Class...", 0, 0);
		openHelpWithoutFrames.setMnemonic(KeyEvent.VK_O);
		openHelp = addToMenu(toolsMenu,
			"Open Help for Class (with frames)...", 0, 0);
		openHelp.setMnemonic(KeyEvent.VK_P);
		openMacroFunctions = addToMenu(toolsMenu,
			"Open Help on Macro Functions...", 0, 0);
		openMacroFunctions.setMnemonic(KeyEvent.VK_H);
		extractSourceJar = addToMenu(toolsMenu,
			"Extract source .jar...", 0, 0);
		extractSourceJar.setMnemonic(KeyEvent.VK_E);
		newPlugin = addToMenu(toolsMenu,
			"Create new plugin...", 0, 0);
		newPlugin.setMnemonic(KeyEvent.VK_C);
		ijToFront = addToMenu(toolsMenu,
			"Focus on the main Fiji window", 0, 0);
		ijToFront.setMnemonic(KeyEvent.VK_F);
		openSourceForClass = addToMenu(toolsMenu,
			"Open .java file for class...", 0, 0);
		openSourceForClass.setMnemonic(KeyEvent.VK_J);
		openSourceForMenuItem = addToMenu(toolsMenu,
			"Open .java file for menu item...", 0, 0);
		openSourceForMenuItem.setMnemonic(KeyEvent.VK_M);
		mbar.add(toolsMenu);

		gitMenu = new JMenu("Git");
		gitMenu.setMnemonic(KeyEvent.VK_G);
		showDiff = addToMenu(gitMenu,
			"Show diff...", 0, 0);
		showDiff.setMnemonic(KeyEvent.VK_D);
		commit = addToMenu(gitMenu,
			"Commit...", 0, 0);
		commit.setMnemonic(KeyEvent.VK_C);
		gitGrep = addToMenu(gitMenu,
			"Grep...", 0, 0);
		gitGrep.setMnemonic(KeyEvent.VK_G);
		openInGitweb = addToMenu(gitMenu,
			"Open in gitweb", 0, 0);
		openInGitweb.setMnemonic(KeyEvent.VK_W);
		mbar.add(gitMenu);

		tabsMenu = new JMenu("Tabs");
		tabsMenu.setMnemonic(KeyEvent.VK_A);
		nextTab = addToMenu(tabsMenu, "Next Tab",
				KeyEvent.VK_PAGE_DOWN, ctrl);
		nextTab.setMnemonic(KeyEvent.VK_N);
		previousTab = addToMenu(tabsMenu, "Previous Tab",
				KeyEvent.VK_PAGE_UP, ctrl);
		previousTab.setMnemonic(KeyEvent.VK_P);
		tabsMenu.addSeparator();
		tabsMenuTabsStart = tabsMenu.getItemCount();
		tabsMenuItems = new HashSet<JMenuItem>();
		mbar.add(tabsMenu);

		// Add the editor and output area
		tabbed = new JTabbedPane();
		tabbed.addChangeListener(this);
		open(null); // make sure the editor pane is added

		tabbed.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(tabbed);

		// for Eclipse and MS Visual Studio lovers
		addAccelerator(compileAndRun, KeyEvent.VK_F11, 0, true);
		addAccelerator(compileAndRun, KeyEvent.VK_F5, 0, true);
		addAccelerator(debug, KeyEvent.VK_F11, ctrl, true);
		addAccelerator(debug, KeyEvent.VK_F5, shift, true);
		addAccelerator(nextTab, KeyEvent.VK_PAGE_DOWN, ctrl, true);
		addAccelerator(previousTab, KeyEvent.VK_PAGE_UP, ctrl, true);

		addAccelerator(increaseFontSize, KeyEvent.VK_EQUALS, ctrl | shift, true);

		// make sure that the window is not closed by accident
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				while (tabbed.getTabCount() > 0) {
					if (!handleUnsavedChanges())
						return;
					int index = tabbed.getSelectedIndex();
					removeTab(index);
				}
				dispose();
			}

			public void windowClosed(WindowEvent e) {
				WindowManager.removeWindow(TextEditor.this);
			}
		});

		addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				final EditorPane editorPane = getEditorPane();
				if (editorPane != null)
					editorPane.checkForOutsideChanges();
			}
		});

		Font font = new Font("Courier", Font.PLAIN, 12);
		errorScreen.setFont(font);
		errorScreen.setEditable(false);
		errorScreen.setLineWrap(true);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		try {
			if (SwingUtilities.isEventDispatchThread())
				pack();
			else
				SwingUtilities.invokeAndWait(new Runnable() { public void run() {
					pack();
				}});
		} catch (Exception ie) {}
		getToolkit().setDynamicLayout(true);            //added to accomodate the autocomplete part
		findDialog = new FindAndReplaceDialog(this);

		setLocationRelativeTo(null); // center on screen

		open(path);

		final EditorPane editorPane = getEditorPane();
		if (editorPane != null)
			editorPane.requestFocus();
	}

	public TextEditor(String title, String text) {
		this(null);
		final EditorPane editorPane = getEditorPane();
		editorPane.setText(text);
		editorPane.setLanguageByFileName(title);
		setFileName(title);
		setTitle();
	}

	/**
	 * Open a new editor to edit the given file, with a templateFile if the file does not exist yet
	 */
	public TextEditor(File file, File templateFile) {
		this(file.exists() ? file.getPath() : templateFile.getPath());
		if (!file.exists()) {
			final EditorPane editorPane = getEditorPane();
			try {
				editorPane.setFile(file.getAbsolutePath());
			} catch (IOException e) {
				IJ.handleException(e);
			}
			editorPane.setLanguageByFileName(file.getName());
			setTitle();
		}
	}

	final public RSyntaxTextArea getTextArea() {
		return getEditorPane();
	}

	public EditorPane getEditorPane() {
		Tab tab = getTab();
		return tab == null ? null : tab.editorPane;
	}

	public Languages.Language getCurrentLanguage() {
		return getEditorPane().currentLanguage;
	}

	public JMenuItem addToMenu(JMenu menu, String menuEntry,
			int key, int modifiers) {
		JMenuItem item = new JMenuItem(menuEntry);
		menu.add(item);
		if (key != 0)
			item.setAccelerator(KeyStroke.getKeyStroke(key,
						modifiers));
		item.addActionListener(this);
		return item;
	}

	protected static class AcceleratorTriplet {
		JMenuItem component;
		int key, modifiers;
	}

	protected List<AcceleratorTriplet> defaultAccelerators =
		new ArrayList<AcceleratorTriplet>();

	public void addAccelerator(final JMenuItem component,
			int key, int modifiers) {
		addAccelerator(component, key, modifiers, false);
	}

	public void addAccelerator(final JMenuItem component, int key, int modifiers, boolean record) {
		if (record) {
			AcceleratorTriplet triplet = new AcceleratorTriplet();
			triplet.component = component;
			triplet.key = key;
			triplet.modifiers = modifiers;
			defaultAccelerators.add(triplet);
		}

		RSyntaxTextArea textArea = getTextArea();
		if (textArea != null)
			addAccelerator(textArea, component, key, modifiers);
	}

	public void addAccelerator(RSyntaxTextArea textArea, final JMenuItem component, int key, int modifiers) {
		textArea.getInputMap().put(KeyStroke.getKeyStroke(key,
					modifiers), component);
		textArea.getActionMap().put(component,
				new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (!component.isEnabled())
					return;
				ActionEvent event = new ActionEvent(component,
					0, "Accelerator");
				TextEditor.this.actionPerformed(event);
			}
		});
	}

	public void addDefaultAccelerators(RSyntaxTextArea textArea) {
		for (AcceleratorTriplet triplet : defaultAccelerators)
			addAccelerator(textArea, triplet.component, triplet.key, triplet.modifiers);
	}

	protected JMenu getMenu(JMenu root, String menuItemPath, boolean createIfNecessary) {
		int gt = menuItemPath.indexOf('>');
		if (gt < 0)
			return root;

		String menuLabel = menuItemPath.substring(0, gt);
		String rest = menuItemPath.substring(gt + 1);
		for (int i = 0; i < root.getItemCount(); i++) {
			JMenuItem item = root.getItem(i);
			if ((item instanceof JMenu) &&
					menuLabel.equals(item.getLabel()))
				return getMenu((JMenu)item, rest, createIfNecessary);
		}
		if (!createIfNecessary)
			return null;
		JMenu subMenu = new JMenu(menuLabel);
		root.add(subMenu);
		return getMenu(subMenu, rest, createIfNecessary);
	}

	/**
	 * Initializes the template menu.
	 */
	protected void addTemplates(JMenu templatesMenu) {
		String url = TextEditor.class.getResource("TextEditor.class").toString();
		String classFilePath = "/" + getClass().getName().replace('.', '/') + ".class";
		if (!url.endsWith(classFilePath))
			return;
		url = url.substring(0, url.length() - classFilePath.length() + 1) + templateFolder;

		List<String> templates = new FileFunctions(this).getResourceList(url);
		Collections.sort(templates);
		for (String template : templates) {
			String path = template.replace('/', '>');
			JMenu menu = getMenu(templatesMenu, path, true);

			String label = path.substring(path.lastIndexOf('>') + 1).replace('_', ' ');
			int dot = label.lastIndexOf('.');
			if (dot > 0)
				label = label.substring(0, dot);
			final String templateURL = url + template;
			JMenuItem item = new JMenuItem(label);
			menu.add(item);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loadTemplate(templateURL);
				}
			});
		}
	}

	/**
	 * Loads a template file from the given resource
	 *
	 * @param url The resource to load.
	 */
	public void loadTemplate(String url) {
		createNewDocument();

		try {
			// Load the template
			InputStream in = new URL(url).openStream();
			getTextArea().read(new BufferedReader(new InputStreamReader(in)), null);

			int dot = url.lastIndexOf('.');
			if (dot > 0) {
				Languages.Language language = Languages.get(url.substring(dot));
				if (language != null)
					setLanguage(language);
			}
		} catch (Exception e) {
			e.printStackTrace();
			error("The template '" + url + "' was not found.");
		}
	}

	public void createNewDocument() {
		open(null);
	}

	public boolean fileChanged() {
		return getEditorPane().fileChanged();
	}

	public boolean handleUnsavedChanges() {
		return handleUnsavedChanges(false);
	}

	public boolean handleUnsavedChanges(boolean beforeCompiling) {
		if (!fileChanged())
			return true;

		if (beforeCompiling && autoSave.getState()) {
			save();
			return true;
		}

		switch (JOptionPane.showConfirmDialog(this,
				"Do you want to save changes?")) {
		case JOptionPane.NO_OPTION:
			return true;
		case JOptionPane.YES_OPTION:
			if (save())
				return true;
		}

		return false;
	}

	protected void grabFocus() {
		toFront();
	}

	protected void grabFocus(final int laterCount) {
		if (laterCount == 0) {
			grabFocus();
			return;
		}

		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				grabFocus(laterCount - 1);
			}
		});
	}

	public void actionPerformed(ActionEvent ae) {
		final Object source = ae.getSource();
		if (source == newFile)
			createNewDocument();
		else if (source == open) {
			final EditorPane editorPane = getEditorPane();
			String defaultDir =
				editorPane != null && editorPane.file != null ?
				editorPane.file.getParent() :
				System.getProperty("ij.dir");
			final String path = openWithDialog("Open...", defaultDir, new String[] {
				".class", ".jar"
			}, false);
			if (path != null)
				new Thread() {
					public void run() {
						open(path);
					}
				}.start();
			return;
		}
		else if (source == save)
			save();
		else if (source == saveas)
			saveAs();
		else if (source == makeJar)
			makeJar(false);
		else if (source == makeJarWithSource)
			makeJar(true);
		else if (source == compileAndRun)
			runText();
		else if (source == compile)
			compile();
		else if (source == runSelection)
			runText(true);
		else if (source == installMacro)
			installMacro();
		else if (source == nextError)
			new Thread() {
				public void run() {
					nextError(true);
				}
			}.start();
		else if (source == previousError)
			new Thread() {
				public void run() {
					nextError(false);
				}
			}.start();
		else if (source == debug) {
			try {
				new Script_Editor().addToolsJarToClassPath();
				getEditorPane().startDebugging();
			} catch (Exception e) {
				error("No debug support for this language");
			}
		}
		else if (source == kill)
			chooseTaskToKill();
		else if (source == close)
			if (tabbed.getTabCount() < 2)
				processWindowEvent(new WindowEvent(this,
						WindowEvent.WINDOW_CLOSING));
			else {
				if (!handleUnsavedChanges())
					return;
				int index = tabbed.getSelectedIndex();
				removeTab(index);
				if (index > 0)
					index--;
				switchTo(index);
			}
		else if (source == cut)
			getTextArea().cut();
		else if (source == copy)
			getTextArea().copy();
		else if (source == paste)
			getTextArea().paste();
		else if (source == undo)
			getTextArea().undoLastAction();
		else if (source == redo)
			getTextArea().redoLastAction();
		else if (source == find)
			findOrReplace(false);
		else if (source == findNext)
			findDialog.searchOrReplace(false);
		else if (source == findPrevious)
			findDialog.searchOrReplace(false, false);
		else if (source == replace)
			findOrReplace(true);
		else if (source == gotoLine)
			gotoLine();
		else if (source == toggleBookmark)
			toggleBookmark();
		else if (source == listBookmarks)
			listBookmarks();
		else if (source == selectAll) {
			getTextArea().setCaretPosition(0);
			getTextArea().moveCaretPosition(getTextArea().getDocument().getLength());
		}
		else if (source == chooseFontSize) {
			int fontSize = (int)IJ.getNumber("Font size", getEditorPane().getFontSize());
			if (fontSize != IJ.CANCELED) {
				getEditorPane().setFontSize(fontSize);
				updateTabAndFontSize(false);
			}
		}
		else if (source == chooseTabSize) {
			int tabSize = (int)IJ.getNumber("Tab size", getEditorPane().getTabSize());
			if (tabSize != IJ.CANCELED) {
				getEditorPane().setTabSize(tabSize);
				updateTabAndFontSize(false);
			}
		}
		else if (source == addImport)
			addImport(null);
		else if (source == removeUnusedImports)
			new TokenFunctions(getTextArea()).removeUnusedImports();
		else if (source == sortImports)
			new TokenFunctions(getTextArea()).sortImports();
		else if (source == removeTrailingWhitespace)
			new TokenFunctions(getTextArea()).removeTrailingWhitespace();
		else if (source == replaceTabsWithSpaces)
			getTextArea().convertTabsToSpaces();
		else if (source == replaceSpacesWithTabs)
			getTextArea().convertSpacesToTabs();
		else if (source == clearScreen)
			getTab().getScreen().setText("");
		else if (source == zapGremlins)
			zapGremlins();
		else if (source == autocomplete) {
			try {
				getEditorPane().autocomp.doCompletion();
			} catch (Exception e) {}
		}
		else if (source == resume)
			getEditorPane().resume();
		else if (source == terminate) {
			getEditorPane().terminate();
		}
		else if (source == openHelp)
			openHelp(null);
		else if (source == openHelpWithoutFrames)
			openHelp(null, false);
		else if (source == openMacroFunctions)
			new MacroFunctions().openHelp(getTextArea().getSelectedText());
		else if (source == extractSourceJar)
			extractSourceJar();
		else if (source == openSourceForClass) {
			String className = getSelectedClassNameOrAsk();
			if (className != null) try {
				String path = new FileFunctions(this).getSourcePath(className);
				if (path != null)
					open(path);
				else {
					String url = new FileFunctions(this).getSourceURL(className);
					new BrowserLauncher().run(url);
				}
			} catch (ClassNotFoundException e) {
				error("Could not open source for class " + className);
			}
		}
		else if (source == openSourceForMenuItem)
			new OpenSourceForMenuItem().run(null);
		else if (source == showDiff) {
			new Thread() {
				public void run() {
					EditorPane pane = getEditorPane();
					new FileFunctions(TextEditor.this).showDiff(pane.file, pane.getGitDirectory());
				}
			}.start();
		}
		else if (source == commit) {
			new Thread() {
				public void run() {
					EditorPane pane = getEditorPane();
					new FileFunctions(TextEditor.this).commit(pane.file, pane.getGitDirectory());
				}
			}.start();
		}
		else if (source == gitGrep) {
			String searchTerm = getTextArea().getSelectedText();
			File searchRoot = getEditorPane().file;
			if (searchRoot == null)
				error("File was not yet saved; no location known!");
			searchRoot = searchRoot.getParentFile();

			GenericDialog gd = new GenericDialog("Grep options");
			gd.addStringField("Search_term", searchTerm == null ? "" : searchTerm, 20);
			gd.addMessage("This search will be performed in\n\n\t" + searchRoot);
			gd.showDialog();
			grabFocus(2);
			if (!gd.wasCanceled())
				new FileFunctions(this).gitGrep(gd.getNextString(), searchRoot);
		}
		else if (source == openInGitweb) {
			EditorPane editorPane = getEditorPane();
			new FileFunctions(this).openInGitweb(editorPane.file, editorPane.gitDirectory, editorPane.getCaretLineNumber() + 1);
		}
		else if (source == newPlugin)
			new FileFunctions(this).newPlugin();
		else if (source == ijToFront)
			IJ.getInstance().toFront();
		else if (source == increaseFontSize || source == decreaseFontSize) {
			getEditorPane().increaseFontSize((float)(source == increaseFontSize ? 1.2 : 1 / 1.2));
			updateTabAndFontSize(false);
		}
		else if (source == nextTab)
			switchTabRelative(1);
		else if (source == previousTab)
			switchTabRelative(-1);
		else if (source == loadToolsJar)
			new Script_Editor().addToolsJarToClassPath();
		else if (handleTabsMenu(source))
			return;
	}

	public void installDebugSupportMenuItem() {
		loadToolsJar = addToMenu(toolsMenu, "Load debugging support via internet", 0, 0);
		loadToolsJar.setVisible(false);
	}

	protected boolean handleTabsMenu(Object source) {
		if (!(source instanceof JMenuItem))
			return false;
		JMenuItem item = (JMenuItem)source;
		if (!tabsMenuItems.contains(item))
			return false;
		for (int i = tabsMenuTabsStart;
				i < tabsMenu.getItemCount(); i++)
			if (tabsMenu.getItem(i) == item) {
				switchTo(i - tabsMenuTabsStart);
				return true;
			}
		return false;
	}

	public void stateChanged(ChangeEvent e) {
		int index = tabbed.getSelectedIndex();
		if (index < 0) {
			setTitle("");
			return;
		}
		final EditorPane editorPane = getEditorPane(index);
		editorPane.requestFocus();
		setTitle();
		editorPane.checkForOutsideChanges();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				editorPane.setLanguageByFileName(editorPane.getFileName());
				toggleWhiteSpaceLabeling.setSelected(((RSyntaxTextArea)editorPane).isWhitespaceVisible());
			}
		});
	}

	public EditorPane getEditorPane(int index) {
		return getTab(index).editorPane;
	}

	public void findOrReplace(boolean replace) {
		findDialog.setLocationRelativeTo(this);

		// override search pattern only if
		// there is sth. selected
		String selection = getTextArea().getSelectedText();
		if (selection != null)
			findDialog.setSearchPattern(selection);

		findDialog.show(replace);
	}

	public void gotoLine() {
		String line = JOptionPane.showInputDialog(this, "Line:",
			"Goto line...", JOptionPane.QUESTION_MESSAGE);
		if (line == null)
			return;
		try {
			gotoLine(Integer.parseInt(line));
		} catch (BadLocationException e) {
			error("Line number out of range: " + line);
		} catch (NumberFormatException e) {
			error("Invalid line number: " + line);
		}
	}

	public void gotoLine(int line) throws BadLocationException {
		getTextArea().setCaretPosition(getTextArea().getLineStartOffset(line-1));
	}

	public void toggleBookmark() {
		getEditorPane().toggleBookmark();
	}

	public void listBookmarks() {
		Vector<EditorPane.Bookmark> bookmarks =
			new Vector<EditorPane.Bookmark>();
		for (int i = 0; i < tabbed.getTabCount(); i++)
			getEditorPane(i).getBookmarks(i, bookmarks);
		BookmarkDialog dialog = new BookmarkDialog(this, bookmarks);
		dialog.show();
	}

	public boolean reload() {
		return reload("Reload the file?");
	}

	public boolean reload(String message) {
		File file = getEditorPane().file;
		if (file == null || !file.exists())
			return true;

		boolean modified = getEditorPane().fileChanged();
		String[] options = { "Reload", "Do not reload" };
		if (modified)
			options[0] = "Reload (discarding changes)";
		switch (JOptionPane.showOptionDialog(this, message, "Reload",
			JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
			null, options, options[0])) {
		case 0:
			try {
				getEditorPane().setFile(file.getPath());
				return true;
			} catch (IOException e) {
				error("Could not reload " + file.getPath());
			}
			break;
		}
		return false;
	}

	public Tab getTab() {
		int index = tabbed.getSelectedIndex();
		if (index < 0)
			return null;
		return (Tab)tabbed.getComponentAt(index);
	}

	public Tab getTab(int index) {
		return (Tab)tabbed.getComponentAt(index);
	}

	public class Tab extends JSplitPane {

		protected final EditorPane editorPane = new EditorPane(TextEditor.this);
		protected final JTextArea screen = new JTextArea();
		protected final JScrollPane scroll;
		protected boolean showingErrors;
		private Executer executer;
		private final JButton runit, killit, toggleErrors;

		public Tab() {
			super(JSplitPane.VERTICAL_SPLIT);
			super.setResizeWeight(350.0 / 430.0);

			screen.setEditable(false);
			screen.setLineWrap(true);
			screen.setFont(new Font("Courier", Font.PLAIN, 12));

			JPanel bottom = new JPanel();
			bottom.setLayout(new GridBagLayout());
			GridBagConstraints bc = new GridBagConstraints();

			bc.gridx = 0;
			bc.gridy = 0;
			bc.weightx = 0;
			bc.weighty = 0;
			bc.anchor = GridBagConstraints.NORTHWEST;
			bc.fill = GridBagConstraints.NONE;
			runit = new JButton("Run");
			runit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) { runText(); }
			});
			bottom.add(runit, bc);

			bc.gridx = 1;
			killit = new JButton("Kill");
			killit.setEnabled(false);
			killit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					kill();
				}
			});
			bottom.add(killit, bc);

			bc.gridx = 2;
			bc.fill = GridBagConstraints.HORIZONTAL;
			bc.weightx = 1;
			bottom.add(new JPanel(), bc);

			bc.gridx = 3;
			bc.fill = GridBagConstraints.NONE;
			bc.weightx = 0;
			bc.anchor = GridBagConstraints.NORTHEAST;
			toggleErrors = new JButton("Show Errors");
			toggleErrors.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					toggleErrors();
				}
			});
			bottom.add(toggleErrors, bc);

			bc.gridx = 4;
			bc.fill = GridBagConstraints.NONE;
			bc.weightx = 0;
			bc.anchor = GridBagConstraints.NORTHEAST;
			JButton clear = new JButton("Clear");
			clear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (showingErrors)
						errorScreen.setText("");
					else
						screen.setText("");
				}
			});
			bottom.add(clear, bc);

			bc.gridx = 0;
			bc.gridy = 1;
			bc.anchor = GridBagConstraints.NORTHWEST;
			bc.fill = GridBagConstraints.BOTH;
			bc.weightx = 1;
			bc.weighty = 1;
			bc.gridwidth = 5;
			screen.setEditable(false);
			screen.setLineWrap(true);
			Font font = new Font("Courier", Font.PLAIN, 12);
			screen.setFont(font);
			scroll = new JScrollPane(screen);
			scroll.setPreferredSize(new Dimension(600, 80));
			bottom.add(scroll, bc);

			super.setTopComponent(editorPane.embedWithScrollbars());
			super.setBottomComponent(bottom);
		}

		/** Invoke in the context of the event dispatch thread. */
		private void prepare() {
			editorPane.setEditable(false);
			runit.setEnabled(false);
			killit.setEnabled(true);
		}

		private void restore() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					editorPane.setEditable(true);
					runit.setEnabled(true);
					killit.setEnabled(false);
					executer = null;
				}
			});
		}

		public void toggleErrors() {
			showingErrors = !showingErrors;
			if (showingErrors) {
				toggleErrors.setLabel("Show Output");
				scroll.setViewportView(errorScreen);
			}
			else {
				toggleErrors.setLabel("Show Errors");
				scroll.setViewportView(screen);
			}
		}

		public void showErrors() {
			if (!showingErrors)
				toggleErrors();
			else if (scroll.getViewport().getView() == null)
				scroll.setViewportView(errorScreen);
		}

		public void showOutput() {
			if (showingErrors)
				toggleErrors();
		}

		public JTextArea getScreen() {
			return showingErrors ? errorScreen: screen;
		}

		boolean isExecuting() {
			return null != executer;
		}

		String getTitle() {
			return (editorPane.fileChanged() ? "*" : "")
				+ editorPane.getFileName()
				+ (isExecuting() ? " (Running)" : "");
		}

		/** Invoke in the context of the event dispatch thread. */
		private void execute(final Languages.Language language,
				final boolean selectionOnly) throws IOException {
			prepare();
			final JTextAreaOutputStream output =
				new JTextAreaOutputStream(this.screen);
			final JTextAreaOutputStream errors =
				new JTextAreaOutputStream(errorScreen);
			final RefreshScripts interpreter =
				language.newInterpreter();
			interpreter.setOutputStreams(output, errors);
			// Pipe current text into the runScript:
			final PipedInputStream pi = new PipedInputStream();
			final PipedOutputStream po = new PipedOutputStream(pi);
			// The Executer creates a Thread that
			// does the reading from PipedInputStream
			this.executer = new TextEditor.Executer(output, errors) {
				public void execute() {
					try {
						interpreter.runScript(pi,
							editorPane.getFileName());
						output.flush();
						errors.flush();
						markCompileEnd();
					} finally {
						restore();
					}
				}
			};
			// Write into PipedOutputStream
			// from another Thread
			try {
				final String text;
				if (selectionOnly) {
					String selected = editorPane.getSelectedText();
					if (selected == null) {
						error("Selection required!");
						text = null;
					}
					else
						text = selected + "\n"; // Ensure code blocks are terminated
				} else {
					text = editorPane.getText();
				}
				new Thread() {
					{ setPriority(Thread.NORM_PRIORITY); }
					public void run() {
						PrintWriter pw = new PrintWriter(po);
						pw.write(text);
						pw.flush(); // will lock and wait in some cases
						try { po.close(); }
						catch (Throwable tt) { tt.printStackTrace(); }
					}
				}.start();
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				// Re-enable when all text to send has been sent
				editorPane.setEditable(true);
			}
		}

		protected void kill() {
			if (null == executer) return;
			// Graceful attempt:
			executer.interrupt();
			// Give it 3 seconds. Then, stop it.
			final long now = System.currentTimeMillis();
			new Thread() {
				{ setPriority(Thread.NORM_PRIORITY); }
				public void run() {
					while (System.currentTimeMillis() - now < 3000)
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					if (null != executer) executer.obliterate();
					restore();

				}
			}.start();
		}
	}

	public static boolean isBinary(String path) {
		if (path == null)
			return false;
		// heuristic: read the first up to 8000 bytes, and say that it is binary if it contains a NUL
		try {
			FileInputStream in = new FileInputStream(path);
			int left = 8000;
			byte[] buffer = new byte[left];
			while (left > 0) {
				int count = in.read(buffer, 0, left);
				if (count < 0)
					break;
				for (int i = 0; i < count; i++)
					if (buffer[i] == 0) {
						in.close();
						return true;
					}
				left -= count;
			}
			in.close();
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	/** Open a new tab with some content; the languageExtension is like ".java", ".py", etc. */
	public Tab newTab(String content, String language) {
		Tab tab = open("");
		if (null != language && language.length() > 0) {
			language = language.trim().toLowerCase();
			if ('.' != language.charAt(0)) language = "." + language;
			tab.editorPane.setLanguage(Languages.getInstance().map.get(language));
		}
		if (null != content) tab.editorPane.setText(content);
		return tab;
	}

	public Tab open(String path) {
		if (path != null && path.startsWith("class:")) try {
			path = new FileFunctions(this).getSourcePath(path.substring(6));
			if (path == null)
				return null;
		} catch (ClassNotFoundException e) {
			error("Could not find " + path);
		}

		if (isBinary(path)) {
			IJ.open(path);
			return null;
		}

		/*
		 * We need to remove RSyntaxTextArea's cached key, input and
		 * action map if there is even the slightest chance that a new
		 * TextArea is instantiated from a different class loader than
		 * the map's actions.
		 *
		 * Otherwise the instanceof check will pretend that the new text
		 * area is not an instance of RTextArea, and as a consequence,
		 * no keyboard input will be possible.
		 */
		JTextComponent.removeKeymap("RTextAreaKeymap");
		UIManager.put("RSyntaxTextAreaUI.actionMap", null);
		UIManager.put("RSyntaxTextAreaUI.inputMap", null);

		try {
			Tab tab = getTab();
			boolean wasNew = tab != null && tab.editorPane.isNew();
			if (!wasNew) {
				tab = new Tab();
				addDefaultAccelerators(tab.editorPane);
			}
			synchronized(tab.editorPane) {
				tab.editorPane.setFile("".equals(path) ? null : path);
				if (wasNew) {
					int index = tabbed.getSelectedIndex()
						+ tabsMenuTabsStart;
					tabsMenu.getItem(index)
						.setText(tab.editorPane.getFileName());
				}
				else {
					tabbed.addTab("", tab);
					switchTo(tabbed.getTabCount() - 1);
					tabsMenuItems.add(addToMenu(tabsMenu,
						tab.editorPane.getFileName(), 0, 0));
				}
				setFileName(tab.editorPane.file);
				try {
					updateTabAndFontSize(true);
				} catch (NullPointerException e) {
					/* ignore */
				}
			}
			if (path != null && !"".equals(path))
				openRecent.add(path);

			return tab;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			error("The file '" + path + "' was not found.");
		} catch (Exception e) {
			e.printStackTrace();
			error("There was an error while opening '" + path + "': " + e);
		}
		return null;
	}

	public boolean saveAs() {
		EditorPane editorPane = getEditorPane();
		SaveDialog sd = new SaveDialog("Save as ",
				editorPane.file == null ?
				System.getProperty("ij.dir") :
				editorPane.file.getParentFile().getAbsolutePath(),
				editorPane.getFileName() , "");
		grabFocus(2);
		String name = sd.getFileName();
		if (name == null)
			return false;

		String path = sd.getDirectory() + name;
		return saveAs(path, true);
	}

	public void saveAs(String path) {
		saveAs(path, true);
	}

	public boolean saveAs(String path, boolean askBeforeReplacing) {
		File file = new File(path);
		if (file.exists() && askBeforeReplacing &&
				JOptionPane.showConfirmDialog(this,
					"Do you want to replace " + path + "?",
					"Replace " + path + "?",
					JOptionPane.YES_NO_OPTION)
				!= JOptionPane.YES_OPTION)
			return false;
		if (!write(file))
			return false;
		setFileName(file);
		openRecent.add(path);
		return true;
	}

	public boolean save() {
		File file = getEditorPane().file;
		if (file == null)
			return saveAs();
		if (!write(file))
			return false;
		setTitle();
		return true;
	}

	public boolean write(File file) {
		try {
			getEditorPane().write(file);
			return true;
		} catch (IOException e) {
			error("Could not save " + file.getName());
			e.printStackTrace();
			return false;
		}
	}

	public boolean makeJar(boolean includeSources) {
		File file = getEditorPane().file;
		Languages.Language currentLanguage = getCurrentLanguage();
		if ((file == null || currentLanguage.isCompileable())
				&& !handleUnsavedChanges(true))
			return false;

		String name = getEditorPane().getFileName();
		if (name.endsWith(currentLanguage.extension))
			name = name.substring(0, name.length()
				- currentLanguage.extension.length());
		if (name.indexOf('_') < 0)
			name += "_";
		name += ".jar";
		SaveDialog sd = new SaveDialog("Export ", name, ".jar");
		grabFocus(2);
		name = sd.getFileName();
		if (name == null)
			return false;

		String path = sd.getDirectory() + name;
		if (new File(path).exists() &&
				JOptionPane.showConfirmDialog(this,
					"Do you want to replace " + path + "?",
					"Replace " + path + "?",
					JOptionPane.YES_NO_OPTION)
				!= JOptionPane.YES_OPTION)
			return false;
		try {
			makeJar(path, includeSources);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			error("Could not write " + path
					+ ": " + e.getMessage());
			return false;
		}
	}

	public void makeJar(String path, boolean includeSources)
			throws IOException {
		List<String> paths = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		File tmpDir = null, file = getEditorPane().file;
		String sourceName = null;
		Languages.Language currentLanguage = getCurrentLanguage();
		if (!(currentLanguage.interpreterClass == Refresh_Javas.class))
			sourceName = file.getName();
		if (!currentLanguage.menuLabel.equals("None")) try {
			tmpDir = File.createTempFile("tmp", "");
			tmpDir.delete();
			tmpDir.mkdir();

			String sourcePath;
			Refresh_Javas java;
			if (sourceName == null) {
	 			sourcePath = file.getAbsolutePath();
				java = (Refresh_Javas)currentLanguage.newInterpreter();
			}
			else {
				// this is a script, we need to generate a Java wrapper
				RefreshScripts interpreter = currentLanguage.newInterpreter();
				sourcePath = generateScriptWrapper(tmpDir, sourceName, interpreter);
				java = (Refresh_Javas)Languages.get(".java").newInterpreter();
			}
			java.showDeprecation(showDeprecation.getState());
			java.compile(sourcePath, tmpDir.getAbsolutePath());
			getClasses(tmpDir, paths, names);
			if (includeSources) {
				String name = java.getPackageName(sourcePath);
				name = (name == null ? "" :
						name.replace('.', '/') + "/")
					+ file.getName();
				sourceName = name;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof IOException)
				throw (IOException)e;
			throw new IOException(e.getMessage());
		}

		OutputStream out = new FileOutputStream(path);
		JarOutputStream jar = new JarOutputStream(out);

		if (sourceName != null)
			writeJarEntry(jar, sourceName,
					getTextArea().getText().getBytes());
		for (int i = 0; i < paths.size(); i++)
			writeJarEntry(jar, names.get(i),
					readFile(paths.get(i)));

		jar.close();

		if (tmpDir != null)
			deleteRecursively(tmpDir);
	}

	protected final static String scriptWrapper =
		"import ij.IJ;\n" +
		"\n" +
		"import ij.plugin.PlugIn;\n" +
		"\n" +
		"public class CLASS_NAME implements PlugIn {\n" +
		"\tpublic void run(String arg) {\n" +
		"\t\ttry {\n" +
		"\t\t\tnew INTERPRETER().runScript(getClass()\n" +
		"\t\t\t\t.getResource(\"SCRIPT_NAME\").openStream());\n" +
		"\t\t} catch (Exception e) {\n" +
		"\t\t\tIJ.handleException(e);\n" +
		"\t\t}\n" +
		"\t}\n" +
		"}\n";
	protected String generateScriptWrapper(File outputDirectory, String scriptName, RefreshScripts interpreter)
			throws FileNotFoundException, IOException {
		String className = scriptName;
		int dot = className.indexOf('.');
		if (dot >= 0)
			className = className.substring(0, dot);
		if (className.indexOf('_') < 0)
			className += "_";
		String code = scriptWrapper.replace("CLASS_NAME", className)
			.replace("SCRIPT_NAME", scriptName)
			.replace("INTERPRETER", interpreter.getClass().getName());
		File output = new File(outputDirectory, className + ".java");
		OutputStream out = new FileOutputStream(output);
		out.write(code.getBytes());
		out.close();
		return output.getAbsolutePath();
	}

	static void getClasses(File directory,
			List<String> paths, List<String> names) {
		getClasses(directory, paths, names, "");
	}

	static void getClasses(File directory,
			List<String> paths, List<String> names, String prefix) {
		if (!prefix.equals(""))
			prefix += "/";
		for (File file : directory.listFiles())
			if (file.isDirectory())
				getClasses(file, paths, names,
						prefix + file.getName());
			else {
				paths.add(file.getAbsolutePath());
				names.add(prefix + file.getName());
			}
	}

	static void writeJarEntry(JarOutputStream out, String name,
			byte[] buf) throws IOException {
		try {
			JarEntry entry = new JarEntry(name);
			out.putNextEntry(entry);
			out.write(buf, 0, buf.length);
			out.closeEntry();
		} catch (ZipException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	static byte[] readFile(String fileName) throws IOException {
		File file = new File(fileName);
		InputStream in = new FileInputStream(file);
		byte[] buffer = new byte[(int)file.length()];
		in.read(buffer);
		in.close();
		return buffer;
	}

	static void deleteRecursively(File directory) {
		for (File file : directory.listFiles())
			if (file.isDirectory())
				deleteRecursively(file);
			else
				file.delete();
		directory.delete();
	}

	void setLanguage(Languages.Language language) {
		getEditorPane().setLanguage(language);
		updateTabAndFontSize(true);
	}

	void updateLanguageMenu(Languages.Language language) {
		if (!language.getMenuItem().isSelected())
			language.getMenuItem().setSelected(true);

		runMenu.setVisible(language.isRunnable());
		compileAndRun.setLabel(language.isCompileable() ?
			"Compile and Run" : "Run");
		compileAndRun.setEnabled(language.isRunnable());
		runSelection.setVisible(language.isRunnable() &&
				!language.isCompileable());
		compile.setVisible(language.isCompileable());
		autoSave.setVisible(language.isCompileable());
		showDeprecation.setVisible(language.isCompileable());
		makeJarWithSource.setVisible(language.isCompileable());

		debug.setVisible(language.isDebuggable());
		if (loadToolsJar != null)
			loadToolsJar.setVisible(language.isDebuggable());
		debug.setVisible(language.isDebuggable());
		resume.setVisible(language.isDebuggable());
		terminate.setVisible(language.isDebuggable());

		boolean isJava = language.menuLabel.equals("Java");
		addImport.setVisible(isJava);
		removeUnusedImports.setVisible(isJava);
		sortImports.setVisible(isJava);
		openSourceForMenuItem.setVisible(isJava);

		boolean isMacro = language.menuLabel.equals("ImageJ Macro");
		installMacro.setVisible(isMacro);
		openMacroFunctions.setVisible(isMacro);
		openSourceForClass.setVisible(!isMacro);

		openHelp.setVisible(!isMacro && language.isRunnable());
		openHelpWithoutFrames.setVisible(!isMacro && language.isRunnable());
		nextError.setVisible(!isMacro && language.isRunnable());
		previousError.setVisible(!isMacro && language.isRunnable());

		if (getEditorPane() == null)
			return;
		boolean isInGit = getEditorPane().getGitDirectory() != null;
		gitMenu.setVisible(isInGit);

		updateTabAndFontSize(false);
	}

	protected void updateTabAndFontSize(boolean setByLanguage) {
		EditorPane pane = getEditorPane();
		if (setByLanguage)
			pane.setTabSize(pane.currentLanguage.menuLabel.equals("Python") ? 4 : 8);
		int tabSize = pane.getTabSize();
		boolean defaultSize = false;
		for (int i = 0; i < tabSizeMenu.getItemCount(); i++) {
			JMenuItem item = tabSizeMenu.getItem(i);
			if (item == chooseTabSize) {
				item.setSelected(!defaultSize);
				item.setLabel("Other" + (defaultSize ? "" : " (" + tabSize + ")") + "...");
			}
			else if (tabSize == Integer.parseInt(item.getLabel())) {
				item.setSelected(true);
				defaultSize = true;
			}
		}
		int fontSize = (int)pane.getFontSize();
		defaultSize = false;
		for (int i = 0; i < fontSizeMenu.getItemCount(); i++) {
			JMenuItem item = fontSizeMenu.getItem(i);
			if (item == chooseFontSize) {
				item.setSelected(!defaultSize);
				item.setLabel("Other" + (defaultSize ? "" : " (" + fontSize + ")") + "...");
				continue;
			}
			String label = item.getLabel();
			if (label.endsWith(" pt"))
				label = label.substring(0, label.length() - 3);
			if (fontSize == Integer.parseInt(label)) {
				item.setSelected(true);
				defaultSize = true;
			}
		}
		wrapLines.setState(pane.getLineWrap());
	}

	public void setFileName(String baseName) {
		getEditorPane().setFileName(baseName);
	}

	public void setFileName(File file) {
		getEditorPane().setFileName(file);
	}

	synchronized void setTitle() {
		final Tab tab = getTab();
		if (null == tab || null == tab.editorPane)
			return;
		final boolean fileChanged = tab.editorPane.fileChanged();
		final String fileName = tab.editorPane.getFileName();
		final String title = (fileChanged ? "*" : "") + fileName
			+ (executingTasks.isEmpty() ? "" : " (Running)");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setTitle(title); // to the main window
				int index = tabbed.getSelectedIndex();
				// Update all tabs: could have changed
				for (int i=0; i<tabbed.getTabCount(); i++)
					tabbed.setTitleAt(i,
						((Tab)tabbed.getComponentAt(i)).getTitle());
			}
		});
	}

	public synchronized void setTitle(String title) {
		super.setTitle(title);
		int index = tabsMenuTabsStart + tabbed.getSelectedIndex();
		if (index < tabsMenu.getItemCount()) {
			JMenuItem item = tabsMenu.getItem(index);
			if (item != null)
				item.setLabel(title);
		}
	}

	/** Using a Vector to benefit from all its methods being synchronzed. */
	private ArrayList<Executer> executingTasks = new ArrayList<Executer>();

	/** Generic Thread that keeps a starting time stamp,
	 *  sets the priority to normal and starts itself. */
	private abstract class Executer extends ThreadGroup {
		JTextAreaOutputStream output, errors;
		Executer(final JTextAreaOutputStream output, final JTextAreaOutputStream errors) {
			super("Script Editor Run :: " + new Date().toString());
			this.output = output;
			this.errors = errors;
			// Store itself for later
			executingTasks.add(this);
			setTitle();
			// Enable kill menu
			kill.setEnabled(true);
			// Fork a task, as a part of this ThreadGroup
			new Thread(this, getName()) {
				{
					setPriority(Thread.NORM_PRIORITY);
					start();
				}
				public void run() {
					try {
						execute();
						// Wait until any children threads die:
						int activeCount = getThreadGroup().activeCount();
						while (activeCount > 1) {
							if (isInterrupted()) break;
							try {
								Thread.sleep(500);
								List<Thread> ts = getAllThreads();
								activeCount = ts.size();
								if (activeCount <= 1) break;
								if (IJ.debugMode)
									System.err.println("Waiting for " + ts.size() + " threads to die");
								int count_zSelector = 0;
								for (Thread t : ts) {
									if (t.getName().equals("zSelector")) {
										count_zSelector++;
									}
									if (IJ.debugMode)
										System.err.println("THREAD: " + t.getName());
								}
								if (activeCount == count_zSelector + 1) {
									// Do not wait on the stack slice selector thread.
									break;
								}
							} catch (InterruptedException ie) {}
						}
					} catch (Throwable t) {
						IJ.handleException(t);
					} finally {
						executingTasks.remove(Executer.this);
						try {
							if (null != output)
								output.shutdown();
							if (null != errors)
								errors.shutdown();
						} catch (Exception e) {
							IJ.handleException(e);
						}
						// Leave kill menu item enabled if other tasks are running
						kill.setEnabled(executingTasks.size() > 0);
						setTitle();
					}
				}
			};
		}

		/** The method to extend, that will do the actual work. */
		abstract void execute();

		/** Fetch a list of all threads from all thread subgroups, recursively. */
		List<Thread> getAllThreads() {
			ArrayList<Thread> threads = new ArrayList<Thread>();
			// From all subgroups:
			ThreadGroup[] tgs = new ThreadGroup[activeGroupCount() * 2 + 100];
			this.enumerate(tgs, true);
			for (ThreadGroup tg : tgs) {
				if (null == tg) continue;
				Thread[] ts = new Thread[tg.activeCount() * 2 + 100];
				tg.enumerate(ts);
				for (Thread t : ts) {
					if (null == t) continue;
					threads.add(t);
				}
			}
			// And from this group:
			Thread[] ts = new Thread[activeCount() * 2 + 100];
			this.enumerate(ts);
			for (Thread t : ts) {
				if (null == t) continue;
				threads.add(t);
			}
			return threads;
		}

		/** Totally destroy/stop all threads in this and all recursive thread subgroups. Will remove itself from the executingTasks list. */
		void obliterate() {
			try {
				// Stop printing to the screen
				if (null != output)
					output.shutdownNow();
				if (null != errors)
					errors.shutdownNow();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (Thread thread : getAllThreads()) {
				try {
					thread.interrupt();
					Thread.yield(); // give it a chance
					thread.stop();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			executingTasks.remove(this);
			setTitle();
		}
	}

	/** Query the list of running scripts and provide a dialog to choose one and kill it. */
	public void chooseTaskToKill() {
		Executer[] executers =
			executingTasks.toArray(new Executer[0]);
		if (0 == executers.length) {
			error("\nNo tasks running!\n");
			return;
		}

		String[] names = new String[executers.length];
		for (int i = 0; i < names.length; i++)
			names[i] = executers[i].getName();

		GenericDialog gd = new GenericDialog("Kill");
		gd.addChoice("Running scripts: ",
				names, names[names.length - 1]);
		gd.addCheckbox("Kill all", false);
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		if (gd.getNextBoolean()) {
			// Kill all
			for (int i=0; i<tabbed.getTabCount(); i++)
				((Tab)tabbed.getComponentAt(i)).kill();
		} else {
			// Kill selected only
			Executer ex = executers[gd.getNextChoiceIndex()];
			for (int i=0; i<tabbed.getTabCount(); i++) {
				Tab tab = (Tab)tabbed.getComponentAt(i);
				if (ex == tab.executer) {
					tab.kill();
					break;
				}
			}
		}
	}

	/** Run the text in the textArea without compiling it, only if it's not java. */
	public void runText() {
		runText(false);
	}

	public void runText(final boolean selectionOnly) {
		final Languages.Language currentLanguage = getCurrentLanguage();
		if (currentLanguage.isCompileable()) {
			if (selectionOnly) {
				error("Cannot run selection of compiled language!");
				return;
			}
			if (handleUnsavedChanges(true))
				runScript();
			return;
		}
		if (!currentLanguage.isRunnable()) {
			error("Select a language first!");
			// TODO guess the language, if possible.
			return;
		}
		markCompileStart();

		try {
			Tab tab = getTab();
			tab.showOutput();
			tab.execute(currentLanguage, selectionOnly);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void runScript() {
		final RefreshScripts interpreter =
			getCurrentLanguage().newInterpreter();

		if (interpreter == null) {
			error("There is no interpreter for this language");
			return;
		}

		if (getCurrentLanguage().isCompileable())
			getTab().showErrors();
		else
			getTab().showOutput();

		markCompileStart();
		final JTextAreaOutputStream output = new JTextAreaOutputStream(getTab().screen);
		final JTextAreaOutputStream errors = new JTextAreaOutputStream(errorScreen);
		interpreter.setOutputStreams(output, errors);

		final File file = getEditorPane().file;
		new TextEditor.Executer(output, errors) {
			public void execute() {
				interpreter.runScript(file.getPath());
				output.flush();
				errors.flush();
				markCompileEnd();
			}
		};
	}

	public void compile() {
		if (!handleUnsavedChanges(true))
			return;

		final RefreshScripts interpreter =
			getCurrentLanguage().newInterpreter();
		final JTextAreaOutputStream output = new JTextAreaOutputStream(getTab().screen);
		final JTextAreaOutputStream errors = new JTextAreaOutputStream(errorScreen);
		interpreter.setOutputStreams(output, errors);
		getTab().showErrors();
		if (interpreter instanceof Refresh_Javas) {
			final Refresh_Javas java = (Refresh_Javas)interpreter;
			final File file = getEditorPane().file;
			final String sourcePath = file.getAbsolutePath();
			java.showDeprecation(showDeprecation.getState());
			markCompileStart();
			new Thread() {
				public void run() {
					java.compileAndRun(sourcePath, true);
					errorScreen.insert("Compilation finished.\n", errorScreen.getDocument().getLength());
					markCompileEnd();
				}
			}.start();
		}
	}

	public String getSelectedTextOrAsk(String label) {
		String selection = getTextArea().getSelectedText();
		if (selection == null || selection.indexOf('\n') >= 0) {
			selection = JOptionPane.showInputDialog(this,
				label + ":", label + "...",
				JOptionPane.QUESTION_MESSAGE);
			if (selection == null)
				return null;
		}
		return selection;
	}

	public String getSelectedClassNameOrAsk() {
		String className = getSelectedTextOrAsk("Class name");
		if (className != null)
			className = className.trim();
		if (className != null && className.indexOf('.') < 0)
			className = getEditorPane().getClassNameFunctions().getFullName(className);
		return className;
	}

	protected static void append(JTextArea textArea, String text) {
		int length = textArea.getDocument().getLength();
		textArea.insert(text, length);
		textArea.setCaretPosition(length);
	}

	public void markCompileStart() {
		errorHandler = null;

		String started = "Started " + getEditorPane().getFileName() + " at " + new Date() + "\n";
		int offset = errorScreen.getDocument().getLength();
		append(errorScreen, started);
		append(getTab().screen, started);
		compileStartOffset = errorScreen.getDocument().getLength();
		try {
			compileStartPosition = errorScreen.getDocument().createPosition(offset);
		} catch (BadLocationException e) {
			handleException(e);
		}
		ExceptionHandler.addThread(Thread.currentThread(), this);
	}

	public void markCompileEnd() {
		if (errorHandler == null) {
			errorHandler = new ErrorHandler(getCurrentLanguage(),
				errorScreen, compileStartPosition.getOffset());
			if (errorHandler.getErrorCount() > 0)
				getTab().showErrors();
		}
		if (compileStartOffset != errorScreen.getDocument().getLength())
			getTab().showErrors();
		if (getTab().showingErrors) try {
			errorHandler.scrollToVisible(compileStartOffset);
		} catch (BadLocationException e) {
			// ignore
		}
	}

	public void installMacro() {
		new MacroFunctions().installMacro(getTitle(), getEditorPane().getText());
	}

	public boolean nextError(boolean forward) {
		if (errorHandler != null && errorHandler.nextError(forward)) try {
			File file = new File(errorHandler.getPath());
			if (!file.isAbsolute())
				file = getFileForBasename(file.getName());
			errorHandler.markLine();
			switchTo(file, errorHandler.getLine());
			getTab().showErrors();
			errorScreen.invalidate();
			return true;
		} catch (Exception e) {
			IJ.handleException(e);
		}
		return false;
	}

	public void switchTo(String path, int lineNumber) throws IOException {
		switchTo(new File(path).getCanonicalFile(), lineNumber);
	}

	public void switchTo(File file, final int lineNumber) {
		if (!editorPaneContainsFile(getEditorPane(), file))
			switchTo(file);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					gotoLine(lineNumber);
				} catch (BadLocationException e) {
					// ignore
				}
			}
		});
	}

	public void switchTo(File file) {
		for (int i = 0; i < tabbed.getTabCount(); i++)
			if (editorPaneContainsFile(getEditorPane(i), file)) {
				switchTo(i);
				return;
			}
		open(file.getPath());
	}

	public void switchTo(int index) {
		if (index == tabbed.getSelectedIndex())
			return;
		tabbed.setSelectedIndex(index);
	}

	protected void switchTabRelative(int delta) {
		int index = tabbed.getSelectedIndex();
		int count = tabbed.getTabCount();
		index = ((index + delta) % count);
		if (index < 0)
			index += count;
		switchTo(index);
	}

	protected void removeTab(int index) {
		tabbed.remove(index);
		index += tabsMenuTabsStart;
		tabsMenuItems.remove(tabsMenu.getItem(index));
		tabsMenu.remove(index);
	}

	boolean editorPaneContainsFile(EditorPane editorPane, File file) {
		try {
			return file != null && editorPane != null &&
				editorPane.file != null &&
				file.getCanonicalFile()
				.equals(editorPane.file.getCanonicalFile());
		} catch (IOException e) {
			return false;
		}
	}

	public File getFile() {
		return getEditorPane().file;
	}

	public File getFileForBasename(String baseName) {
		File file = getFile();
		if (file != null && file.getName().equals(baseName))
			return file;
		for (int i = 0; i < tabbed.getTabCount(); i++) {
			file = getEditorPane(i).file;
			if (file != null && file.getName().equals(baseName))
				return file;
		}
		return null;
	}

	public void addImport(String className) {
		if (className == null)
			className = getSelectedClassNameOrAsk();
		if (className != null)
			new TokenFunctions(getTextArea()).addImport(className.trim());
	}

	public void openHelp(String className) {
		openHelp(className, true);
	}

	public void openHelp(String className, boolean withFrames) {
		if (className == null)
			className = getSelectedClassNameOrAsk();
		if (className != null)
			getEditorPane().getClassNameFunctions().openHelpForClass(className, withFrames);
	}

	public void extractSourceJar() {
		String path = openWithDialog("Open...", null, new String[] {
			".jar"
		}, true);
		if (path != null)
			extractSourceJar(path);
	}

	public void extractSourceJar(String path) {
		try {
			FileFunctions functions = new FileFunctions(this);
			List<String> paths = functions.extractSourceJar(path);
			for (String file : paths)
				if (!functions.isBinaryFile(file)) {
					open(file);
					EditorPane pane = getEditorPane();
					new TokenFunctions(pane).removeTrailingWhitespace();
					if (pane.fileChanged())
						save();
				}
		} catch (IOException e) {
			error("There was a problem opening " + path
				+ ": " + e.getMessage());
		}
	}

	/* extensionMustMatch == false means extension must not match */
	protected String openWithDialog(final String title, final String directory,
			final String[] extensions, final boolean extensionMustMatch) {
		FileDialog dialog = new FileDialog(this, title);
		if (directory != null)
			dialog.setDirectory(directory);
		if (extensions != null)
			dialog.setFilenameFilter(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					for (String extension : extensions)
						if (name.endsWith(extension))
							return extensionMustMatch;
					return !extensionMustMatch;
				}
			});
		dialog.setVisible(true);
		String dir = dialog.getDirectory();
		String name = dialog.getFile();
		if (dir == null || name == null)
			return null;
		return new File(dir, name).getAbsolutePath();
	}

	/**
	 * Write a message to the output screen
	 *
	 * @param message The text to write
	 */
	public void write(String message) {
		Tab tab = getTab();
		if (!message.endsWith("\n"))
			message += "\n";
		tab.screen.insert(message, tab.screen.getDocument().getLength());
	}

	public void writeError(String message) {
		Tab tab = getTab();
		tab.showErrors();
		if (!message.endsWith("\n"))
			message += "\n";
		errorScreen.insert(message, errorScreen.getDocument().getLength());
	}

	protected void error(String message) {
		JOptionPane.showMessageDialog(this, message);
	}

	void handleException(Throwable e) {
		ij.IJ.handleException(e);
	}

	/** Removes invalid characters, shows a dialog.
	 * @return The amount of invalid characters found. */
	public int zapGremlins() {
		int count = getEditorPane().zapGremlins();
		String msg = count > 0 ? "Zap Gremlins converted " + count + " invalid characters to spaces" : "No invalid characters found!";
		JOptionPane.showMessageDialog(this, msg);
		return count;
	}
}
