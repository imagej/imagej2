//
// EditorFrame.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.script.editor;

import imagej.script.CompiledLanguage;
import imagej.script.ScriptService;
import imagej.util.AppUtils;
import imagej.util.Log;

import java.awt.Font;
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

@SuppressWarnings("serial")
public class EditorFrame extends JFrame implements ActionListener,
	ChangeListener
{

	protected JTabbedPane tabbed;
	protected JMenuItem newFile,
			open,
			save,
			saveas,
			compileAndRun /* TODO! , compile */,
			close, /* TODO! undo, redo, */
			cut,
			copy,
			paste,
			find,
			replace,
			selectAll,
			kill,
			gotoLine, /* TODO! makeJar,
								makeJarWithSource, removeUnusedImports, sortImports,
								removeTrailingWhitespace, */
			findNext,
			findPrevious,
			openHelp, /* TODO! addImport, */
			clearScreen,
			nextError,
			previousError,
			openHelpWithoutFrames,
			nextTab,
			previousTab,
			runSelection, /* TODO! extractSourceJar, toggleBookmark,
										listBookmarks, openSourceForClass, newPlugin, installMacro,
										openSourceForMenuItem, showDiff, commit, ijToFront, openMacroFunctions,*/
			decreaseFontSize,
			increaseFontSize,
			chooseFontSize/* TODO! , chooseTabSize,
										gitGrep, loadToolsJar, openInGitweb, replaceTabsWithSpaces,
										replaceSpacesWithTabs, toggleWhiteSpaceLabeling, zapGremlins */;
	protected RecentFilesMenuItem openRecent;
	protected JMenu /* TODO! gitMenu, */tabsMenu, fontSizeMenu, /* TODO! tabSizeMenu, */
	toolsMenu, runMenu/* TODO! , whiteSpaceMenu */;
	protected int tabsMenuTabsStart;
	protected Set<JMenuItem> tabsMenuItems;
	// TODO! protected FindAndReplaceDialog findDialog;
	protected JCheckBoxMenuItem autoSave/* TODO! , showDeprecation, wrapLines */;
	protected JTextArea errorScreen = new JTextArea();

	protected final String templateFolder = "templates/";
	protected final ScriptService scriptService;
	protected Map<ScriptEngineFactory, JRadioButtonMenuItem> languageMenuItems;

	protected int compileStartOffset;
	protected Position compileStartPosition;
	protected ErrorHandler errorHandler;

	public EditorFrame(final ScriptService scriptService, final File file) {
		super("Script Editor");
		this.scriptService = scriptService;

		// Initialize menu
		final int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		final int shift = ActionEvent.SHIFT_MASK;
		final JMenuBar mbar = new JMenuBar();
		setJMenuBar(mbar);

		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		newFile = addToMenu(fileMenu, "New", KeyEvent.VK_N, ctrl);
		newFile.setMnemonic(KeyEvent.VK_N);
		open = addToMenu(fileMenu, "Open...", KeyEvent.VK_O, ctrl);
		open.setMnemonic(KeyEvent.VK_O);
		openRecent = new RecentFilesMenuItem(this);
		openRecent.setMnemonic(KeyEvent.VK_R);
		fileMenu.add(openRecent);
		save = addToMenu(fileMenu, "Save", KeyEvent.VK_S, ctrl);
		save.setMnemonic(KeyEvent.VK_S);
		saveas = addToMenu(fileMenu, "Save as...", 0, 0);
		saveas.setMnemonic(KeyEvent.VK_A);
		fileMenu.addSeparator();
		// TODO! makeJar = addToMenu(file, "Export as .jar", 0, 0);
		// TODO! makeJar.setMnemonic(KeyEvent.VK_E);
		// TODO! makeJarWithSource = addToMenu(file, "Export as .jar (with source)",
		// 0, 0);
		// TODO! makeJarWithSource.setMnemonic(KeyEvent.VK_X);
		fileMenu.addSeparator();
		close = addToMenu(fileMenu, "Close", KeyEvent.VK_W, ctrl);

		mbar.add(fileMenu);

		final JMenu edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);
		// TODO! undo = addToMenu(edit, "Undo", KeyEvent.VK_Z, ctrl);
		// TODO! redo = addToMenu(edit, "Redo", KeyEvent.VK_Y, ctrl);
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
		// TODO! toggleBookmark = addToMenu(edit, "Toggle Bookmark", KeyEvent.VK_B,
		// ctrl);
		// TODO! toggleBookmark.setMnemonic(KeyEvent.VK_B);
		// TODO! listBookmarks = addToMenu(edit, "List Bookmarks", 0, 0);
		// TODO! listBookmarks.setMnemonic(KeyEvent.VK_O);
		edit.addSeparator();

		// Font adjustments
		decreaseFontSize =
			addToMenu(edit, "Decrease font size", KeyEvent.VK_MINUS, ctrl);
		decreaseFontSize.setMnemonic(KeyEvent.VK_D);
		increaseFontSize =
			addToMenu(edit, "Increase font size", KeyEvent.VK_PLUS, ctrl);
		increaseFontSize.setMnemonic(KeyEvent.VK_C);

		fontSizeMenu = new JMenu("Font sizes");
		fontSizeMenu.setMnemonic(KeyEvent.VK_Z);
		final boolean[] fontSizeShortcutUsed = new boolean[10];
		final ButtonGroup buttonGroup = new ButtonGroup();
		for (final int size : new int[] { 8, 10, 12, 16, 20, 28, 42 }) {
			final JRadioButtonMenuItem item =
				new JRadioButtonMenuItem("" + size + " pt");
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent event) {
					getEditorPane().setFontSize(size);
					updateTabAndFontSize(false);
				}
			});
			for (final char c : ("" + size).toCharArray()) {
				final int digit = c - '0';
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
		/* TODO! tabSizeMenu = new JMenu("Tab sizes");
		tabSizeMenu.setMnemonic(KeyEvent.VK_T);
		final ButtonGroup bg = new ButtonGroup();
		for (final int size : new int[] { 2, 4, 8 }) {
			final JRadioButtonMenuItem item =
				new JRadioButtonMenuItem("" + size, size == 8);
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent event) {
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
		*/

		edit.addSeparator();

		clearScreen = addToMenu(edit, "Clear output panel", 0, 0);
		clearScreen.setMnemonic(KeyEvent.VK_L);
		// edit.addSeparator();
		// autocomplete = addToMenu(edit, "Autocomplete", KeyEvent.VK_SPACE, ctrl);

		// TODO! zapGremlins = addToMenu(edit, "Zap Gremlins", 0, 0);

		// autocomplete.setMnemonic(KeyEvent.VK_A);
		edit.addSeparator();
		/* TODO!
		addImport = addToMenu(edit, "Add import...", 0, 0);
		addImport.setMnemonic(KeyEvent.VK_I);
		removeUnusedImports = addToMenu(edit, "Remove unused imports", 0, 0);
		removeUnusedImports.setMnemonic(KeyEvent.VK_U);
		sortImports = addToMenu(edit, "Sort imports", 0, 0);
		sortImports.setMnemonic(KeyEvent.VK_S);
		*/
		mbar.add(edit);

		/* TODO!
		whiteSpaceMenu = new JMenu("Whitespace");
		whiteSpaceMenu.setMnemonic(KeyEvent.VK_W);
		removeTrailingWhitespace =
			addToMenu(whiteSpaceMenu, "Remove trailing whitespace", 0, 0);
		removeTrailingWhitespace.setMnemonic(KeyEvent.VK_W);
		replaceTabsWithSpaces =
			addToMenu(whiteSpaceMenu, "Replace tabs with spaces", 0, 0);
		replaceTabsWithSpaces.setMnemonic(KeyEvent.VK_S);
		replaceSpacesWithTabs =
			addToMenu(whiteSpaceMenu, "Replace spaces with tabs", 0, 0);
		replaceSpacesWithTabs.setMnemonic(KeyEvent.VK_T);
		toggleWhiteSpaceLabeling = new JRadioButtonMenuItem("Label whitespace");
		toggleWhiteSpaceLabeling.setMnemonic(KeyEvent.VK_L);
		toggleWhiteSpaceLabeling.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO:
				// getTextComponent().setWhitespaceVisible(toggleWhiteSpaceLabeling.isSelected());
			}
		});
		whiteSpaceMenu.add(toggleWhiteSpaceLabeling);

		edit.add(whiteSpaceMenu);
		*/

		languageMenuItems =
			new LinkedHashMap<ScriptEngineFactory, JRadioButtonMenuItem>();
		final Set<Integer> usedShortcuts = new HashSet<Integer>();
		final JMenu languages = new JMenu("Language");
		languages.setMnemonic(KeyEvent.VK_L);
		final ButtonGroup group = new ButtonGroup();
		for (final ScriptEngineFactory language : scriptService.getLanguages()) {
			final String name = language.getLanguageName();

			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
			languageMenuItems.put(language, item);

			int shortcut = -1;
			for (final char ch : name.toCharArray()) {
				final int keyCode = KeyStroke.getKeyStroke(ch).getKeyCode();
				if (usedShortcuts.contains(keyCode)) continue;
				shortcut = keyCode;
				usedShortcuts.add(shortcut);
				break;
			}
			if (shortcut > 0) item.setMnemonic(shortcut);

			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					setLanguage(language);
				}
			});

			group.add(item);
			languages.add(item);
		}

		final JRadioButtonMenuItem item = new JRadioButtonMenuItem("None");
		languageMenuItems.put(null, item);
		item.setSelected(true);
		languages.add(item);

		mbar.add(languages);

		final JMenu templates = new JMenu("Templates");
		templates.setMnemonic(KeyEvent.VK_T);
		addTemplates(templates);
		mbar.add(templates);

		runMenu = new JMenu("Run");
		runMenu.setMnemonic(KeyEvent.VK_R);

		compileAndRun = addToMenu(runMenu, "Compile and Run", KeyEvent.VK_R, ctrl);
		compileAndRun.setMnemonic(KeyEvent.VK_R);

		runSelection =
			addToMenu(runMenu, "Run selected code", KeyEvent.VK_R, ctrl | shift);
		runSelection.setMnemonic(KeyEvent.VK_S);

		/* TODO!
		compile = addToMenu(runMenu, "Compile", KeyEvent.VK_C, ctrl | shift);
		compile.setMnemonic(KeyEvent.VK_C);
		*/
		autoSave = new JCheckBoxMenuItem("Auto-save before compiling");
		runMenu.add(autoSave);
		/* TODO!
		showDeprecation = new JCheckBoxMenuItem("Show deprecations");
		runMenu.add(showDeprecation);

		installMacro = addToMenu(runMenu, "Install Macro", KeyEvent.VK_I, ctrl);
		installMacro.setMnemonic(KeyEvent.VK_I);
		*/

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

		mbar.add(runMenu);

		toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_O);
		openHelpWithoutFrames =
			addToMenu(toolsMenu, "Open Help for Class...", 0, 0);
		openHelpWithoutFrames.setMnemonic(KeyEvent.VK_O);
		openHelp =
			addToMenu(toolsMenu, "Open Help for Class (with frames)...", 0, 0);
		openHelp.setMnemonic(KeyEvent.VK_P);
		/* TODO!
		openMacroFunctions =
			addToMenu(toolsMenu, "Open Help on Macro Functions...", 0, 0);
		openMacroFunctions.setMnemonic(KeyEvent.VK_H);
		extractSourceJar = addToMenu(toolsMenu, "Extract source .jar...", 0, 0);
		extractSourceJar.setMnemonic(KeyEvent.VK_E);
		newPlugin = addToMenu(toolsMenu, "Create new plugin...", 0, 0);
		newPlugin.setMnemonic(KeyEvent.VK_C);
		ijToFront = addToMenu(toolsMenu, "Focus on the main Fiji window", 0, 0);
		ijToFront.setMnemonic(KeyEvent.VK_F);
		openSourceForClass =
			addToMenu(toolsMenu, "Open .java file for class...", 0, 0);
		openSourceForClass.setMnemonic(KeyEvent.VK_J);
		openSourceForMenuItem =
			addToMenu(toolsMenu, "Open .java file for menu item...", 0, 0);
		openSourceForMenuItem.setMnemonic(KeyEvent.VK_M);
		mbar.add(toolsMenu);

		gitMenu = new JMenu("Git");
		gitMenu.setMnemonic(KeyEvent.VK_G);
		showDiff = addToMenu(gitMenu, "Show diff...", 0, 0);
		showDiff.setMnemonic(KeyEvent.VK_D);
		commit = addToMenu(gitMenu, "Commit...", 0, 0);
		commit.setMnemonic(KeyEvent.VK_C);
		gitGrep = addToMenu(gitMenu, "Grep...", 0, 0);
		gitGrep.setMnemonic(KeyEvent.VK_G);
		openInGitweb = addToMenu(gitMenu, "Open in gitweb", 0, 0);
		openInGitweb.setMnemonic(KeyEvent.VK_W);
		mbar.add(gitMenu);
		*/

		tabsMenu = new JMenu("Tabs");
		tabsMenu.setMnemonic(KeyEvent.VK_A);
		nextTab = addToMenu(tabsMenu, "Next Tab", KeyEvent.VK_PAGE_DOWN, ctrl);
		nextTab.setMnemonic(KeyEvent.VK_N);
		previousTab =
			addToMenu(tabsMenu, "Previous Tab", KeyEvent.VK_PAGE_UP, ctrl);
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
		getContentPane().setLayout(
			new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(tabbed);

		// for Eclipse and MS Visual Studio lovers
		addAccelerator(compileAndRun, KeyEvent.VK_F11, 0, true);
		addAccelerator(compileAndRun, KeyEvent.VK_F5, 0, true);
		addAccelerator(nextTab, KeyEvent.VK_PAGE_DOWN, ctrl, true);
		addAccelerator(previousTab, KeyEvent.VK_PAGE_UP, ctrl, true);

		addAccelerator(increaseFontSize, KeyEvent.VK_EQUALS, ctrl | shift, true);

		// make sure that the window is not closed by accident
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				while (tabbed.getTabCount() > 0) {
					if (!handleUnsavedChanges()) return;
					final int index = tabbed.getSelectedIndex();
					removeTab(index);
				}
				dispose();
			}

			@Override
			public void windowClosed(final WindowEvent e) {
				// TODO: WindowManager.removeWindow(EditorFrame.this);
			}
		});

		addWindowFocusListener(new WindowAdapter() {

			@Override
			public void windowGainedFocus(final WindowEvent e) {
				final EditorPane editorPane = getEditorPane();
				if (editorPane != null) editorPane.checkForOutsideChanges();
			}
		});

		final Font font = new Font("Courier", Font.PLAIN, 12);
		errorScreen.setFont(font);
		errorScreen.setEditable(false);
		errorScreen.setLineWrap(true);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		try {
			if (SwingUtilities.isEventDispatchThread()) pack();
			else SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					pack();
				}
			});
		}
		catch (final Exception ie) {}
		getToolkit().setDynamicLayout(true); // added to accomodate the autocomplete
		// part
		// TODO! findDialog = new FindAndReplaceDialog(this);

		setLocationRelativeTo(null); // center on screen

		open(file);

		final JTextComponent textComponent = getTextComponent();
		if (textComponent != null) textComponent.requestFocus();
	}

	/* TODO! still needed?
	public EditorFrame(String title, String text) {
		this(ImageJ.get(ScriptService.class), null);
		final EditorPane editorPane = getEditorPane();
		editorPane.textComponent.setText(text);
		editorPane.setLanguageByFileName(title);
		setFileName(title);
		setTitle();
	}

	/**
	 * Open a new editor to edit the given file, with a templateFile if the file does not exist yet
	 * /
	public EditorFrame(File file, File templateFile) {
		this(ImageJ.get(ScriptService.class), file.exists() ? file.getPath() : templateFile.getPath());
		if (!file.exists()) {
			final EditorPane editorPane = getEditorPane();
			try {
				editorPane.setFile(file.getAbsolutePath());
			} catch (IOException e) {
				handleException(e);
			}
			editorPane.setLanguageByFileName(file.getName());
			setTitle();
		}
	}
	*/

	final public JTextComponent getTextComponent() {
		final EditorPane pane = getEditorPane();
		return pane == null ? null : pane.textArea;
	}

	public EditorPane getEditorPane() {
		final Tab tab = getTab();
		return tab == null ? null : tab.editorPane;
	}

	public ScriptEngineFactory getCurrentLanguage() {
		return getEditorPane().currentLanguage;
	}

	public JMenuItem addToMenu(final JMenu menu, final String menuEntry,
		final int key, final int modifiers)
	{
		final JMenuItem item = new JMenuItem(menuEntry);
		menu.add(item);
		if (key != 0) item.setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
		item.addActionListener(this);
		return item;
	}

	protected static class AcceleratorTriplet {

		JMenuItem component;
		int key, modifiers;
	}

	protected List<AcceleratorTriplet> defaultAccelerators =
		new ArrayList<AcceleratorTriplet>();

	public void addAccelerator(final JMenuItem component, final int key,
		final int modifiers)
	{
		addAccelerator(component, key, modifiers, false);
	}

	public void addAccelerator(final JMenuItem component, final int key,
		final int modifiers, final boolean record)
	{
		if (record) {
			final AcceleratorTriplet triplet = new AcceleratorTriplet();
			triplet.component = component;
			triplet.key = key;
			triplet.modifiers = modifiers;
			defaultAccelerators.add(triplet);
		}

		final JTextComponent textComponent = getTextComponent();
		if (textComponent != null) addAccelerator(textComponent, component, key,
			modifiers);
	}

	public void addAccelerator(final JTextComponent textComponent,
		final JMenuItem component, final int key, final int modifiers)
	{
		textComponent.getInputMap().put(KeyStroke.getKeyStroke(key, modifiers),
			component);
		textComponent.getActionMap().put(component, new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!component.isEnabled()) return;
				final ActionEvent event = new ActionEvent(component, 0, "Accelerator");
				EditorFrame.this.actionPerformed(event);
			}
		});
	}

	public void addDefaultAccelerators(final JTextComponent textComponent) {
		for (final AcceleratorTriplet triplet : defaultAccelerators)
			addAccelerator(textComponent, triplet.component, triplet.key,
				triplet.modifiers);
	}

	protected JMenu getMenu(final JMenu root, final String menuItemPath,
		final boolean createIfNecessary)
	{
		final int gt = menuItemPath.indexOf('>');
		if (gt < 0) return root;

		final String menuLabel = menuItemPath.substring(0, gt);
		final String rest = menuItemPath.substring(gt + 1);
		for (int i = 0; i < root.getItemCount(); i++) {
			final JMenuItem item = root.getItem(i);
			if ((item instanceof JMenu) && menuLabel.equals(item.getText())) return getMenu(
				(JMenu) item, rest, createIfNecessary);
		}
		if (!createIfNecessary) return null;
		final JMenu subMenu = new JMenu(menuLabel);
		root.add(subMenu);
		return getMenu(subMenu, rest, createIfNecessary);
	}

	/**
	 * Initializes the template menu.
	 */
	protected void addTemplates(final JMenu templatesMenu) {
		String url = EditorFrame.class.getResource("EditorFrame.class").toString();
		final String classFilePath =
			"/" + getClass().getName().replace('.', '/') + ".class";
		if (!url.endsWith(classFilePath)) return;
		url =
			url.substring(0, url.length() - classFilePath.length() + 1) +
				templateFolder;

		final List<String> templates = new FileFunctions(this).getResourceList(url);
		Collections.sort(templates);
		for (final String template : templates) {
			final String path = template.replace('/', '>');
			final JMenu menu = getMenu(templatesMenu, path, true);

			String label =
				path.substring(path.lastIndexOf('>') + 1).replace('_', ' ');
			final int dot = label.lastIndexOf('.');
			if (dot > 0) label = label.substring(0, dot);
			final String templateURL = url + template;
			final JMenuItem item = new JMenuItem(label);
			menu.add(item);
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
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
	public void loadTemplate(final String url) {
		createNewDocument();

		try {
			// Load the template
			final InputStream in = new URL(url).openStream();
			getTextComponent().read(new BufferedReader(new InputStreamReader(in)),
				null);

			final int dot = url.lastIndexOf('.');
			if (dot > 0) {
				final ScriptEngineFactory language =
					scriptService.getByFileExtension(url.substring(dot + 1));
				if (language != null) setLanguage(language);
			}
		}
		catch (final Exception e) {
			Log.error(e);
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

	public boolean handleUnsavedChanges(final boolean beforeCompiling) {
		if (!fileChanged()) return true;

		if (beforeCompiling && autoSave.getState()) {
			save();
			return true;
		}

		switch (JOptionPane.showConfirmDialog(this, "Do you want to save changes?")) {
			case JOptionPane.NO_OPTION:
				return true;
			case JOptionPane.YES_OPTION:
				if (save()) return true;
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

			@Override
			public void run() {
				grabFocus(laterCount - 1);
			}
		});
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object source = e.getSource();
		if (source == newFile) createNewDocument();
		else if (source == open) {
			final EditorPane editorPane = getEditorPane();
			final File defaultDir =
				editorPane != null && editorPane.file != null ? editorPane.file
					.getParentFile() : AppUtils.getBaseDirectory();
			final File file =
				openWithDialog("Open...", defaultDir,
					new String[] { ".class", ".jar" }, false);
			if (file != null) new Thread() {

				@Override
				public void run() {
					open(file);
				}
			}.start();
			return;
		}
		else if (source == save) save();
		else if (source == saveas) saveAs();
		// TODO! else if (source == makeJar) makeJar(false);
		// TODO! else if (source == makeJarWithSource) makeJar(true);
		else if (source == compileAndRun) runText();
		// TODO! else if (source == compile) compile();
		else if (source == runSelection) runText(true);
		// TODO! else if (source == installMacro) installMacro();
		else if (source == nextError) new Thread() {

			@Override
			public void run() {
				nextError(true);
			}
		}.start();
		else if (source == previousError) {
			new Thread() {

				@Override
				public void run() {
					nextError(false);
				}
			}.start();
		}
		else if (source == kill) {
			// TODO! chooseTaskToKill();
		}
		else if (source == close) {
			if (tabbed.getTabCount() < 2) processWindowEvent(new WindowEvent(this,
				WindowEvent.WINDOW_CLOSING));
			else {
				if (!handleUnsavedChanges()) return;
				int index = tabbed.getSelectedIndex();
				removeTab(index);
				if (index > 0) index--;
				switchTo(index);
			}
		}
		else if (source == cut) getTextComponent().cut();
		else if (source == copy) getTextComponent().copy();
		else if (source == paste) getTextComponent().paste();
		// TODO!
		/*
		else if (source == undo)
			getTextComponent().undoLastAction();
		else if (source == redo)
			getTextComponent().redoLastAction();
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
		*/
		else if (source == selectAll) {
			getTextComponent().setCaretPosition(0);
			getTextComponent().moveCaretPosition(
				getTextComponent().getDocument().getLength());
		}
		else if (source == chooseFontSize) {
			final int fontSize =
				getNumber("Font size", getEditorPane().getFontSize(), -1);
			if (fontSize > 0) {
				getEditorPane().setFontSize(fontSize);
				updateTabAndFontSize(false);
			}
		}
		/* TODO!
		else if (source == chooseTabSize) {
			int tabSize = (int)getNumber("Tab size", getEditorPane().getTabSize(), -1);
			if (tabSize > 0) {
				getEditorPane().setTabSize(tabSize);
				updateTabAndFontSize(false);
			}
		}
		else if (source == addImport) addImport(null);
		else if (source == removeUnusedImports)
			new TokenFunctions(getTextComponent()).removeUnusedImports();
		else if (source == sortImports)
			new TokenFunctions(getTextComponent()).sortImports();
		else if (source == removeTrailingWhitespace)
			new TokenFunctions(getTextComponent()).removeTrailingWhitespace();
		else if (source == replaceTabsWithSpaces)
			getTextComponent().convertTabsToSpaces();
		else if (source == replaceSpacesWithTabs)
			getTextComponent().convertSpacesToTabs();
		*/
		else if (source == clearScreen) getTab().getScreen().setText("");
		// TODO! else if (source == zapGremlins) zapGremlins();
		// TODO! else if (source == openHelp) openHelp(null);
		// TODO! else if (source == openHelpWithoutFrames) openHelp(null, false);
		/* TODO!
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
					new FileFunctions(EditorFrame.this).showDiff(pane.file, pane.getGitDirectory());
				}
			}.start();
		}
		else if (source == commit) {
			new Thread() {
				public void run() {
					EditorPane pane = getEditorPane();
					new FileFunctions(EditorFrame.this).commit(pane.file, pane.getGitDirectory());
				}
			}.start();
		}
		else if (source == gitGrep) {
			String searchTerm = getTextComponent().getSelectedText();
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
		*/
		else if (source == increaseFontSize || source == decreaseFontSize) {
			getEditorPane().increaseFontSize(
				(float) (source == increaseFontSize ? 1.2 : 1 / 1.2));
			updateTabAndFontSize(false);
		}
		else if (source == nextTab) switchTabRelative(1);
		else if (source == previousTab) switchTabRelative(-1);
		else if (handleTabsMenu(source)) return;
	}

	private int getNumber(final String string, final float fontSize, final int i)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	protected boolean handleTabsMenu(final Object source) {
		if (!(source instanceof JMenuItem)) return false;
		final JMenuItem item = (JMenuItem) source;
		if (!tabsMenuItems.contains(item)) return false;
		for (int i = tabsMenuTabsStart; i < tabsMenu.getItemCount(); i++)
			if (tabsMenu.getItem(i) == item) {
				switchTo(i - tabsMenuTabsStart);
				return true;
			}
		return false;
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		final int index = tabbed.getSelectedIndex();
		if (index < 0) {
			setTitle("");
			return;
		}
		final EditorPane editorPane = getEditorPane(index);
		editorPane.requestFocus();
		setTitle();
		editorPane.checkForOutsideChanges();
		editorPane.updateLanguage();
		// TODO!
		// toggleWhiteSpaceLabeling.setSelected(((JTextComponent)editorPane).isWhitespaceVisible());
	}

	public EditorPane getEditorPane(final int index) {
		return getTab(index).editorPane;
	}

	/* TODO!
	public void findOrReplace(boolean replace) {
		findDialog.setLocationRelativeTo(this);

		// override search pattern only if
		// there is sth. selected
		String selection = getTextComponent().getSelectedText();
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

	*/

	public void gotoLine(final int line) throws BadLocationException {
		final EditorPane pane = getEditorPane();
		if (pane == null) throw new BadLocationException("No pane", line);
		pane.gotoLine(line);
	}

	public void toggleBookmark() {
		getEditorPane().toggleBookmark();
	}

	/* TODO!
	public void listBookmarks() {
		Vector<EditorPane.Bookmark> bookmarks =
			new Vector<EditorPane.Bookmark>();
		for (int i = 0; i < tabbed.getTabCount(); i++)
			getEditorPane(i).getBookmarks(i, bookmarks);
		BookmarkDialog dialog = new BookmarkDialog(this, bookmarks);
		dialog.show();
	}
	*/

	public boolean reload() {
		return reload("Reload the file?");
	}

	public boolean reload(final String message) {
		final File file = getEditorPane().file;
		if (file == null || !file.exists()) return true;

		final boolean modified = getEditorPane().fileChanged();
		final String[] options = { "Reload", "Do not reload" };
		if (modified) options[0] = "Reload (discarding changes)";
		switch (JOptionPane.showOptionDialog(this, message, "Reload",
			JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
			options[0])) {
			case 0:
				try {
					getEditorPane().open(file);
					return true;
				}
				catch (final IOException e) {
					error("Could not reload " + file.getPath());
				}
				break;
		}
		return false;
	}

	public Tab getTab() {
		final int index = tabbed.getSelectedIndex();
		if (index < 0) return null;
		return (Tab) tabbed.getComponentAt(index);
	}

	public Tab getTab(final int index) {
		return (Tab) tabbed.getComponentAt(index);
	}

	public static boolean isBinary(final File file) {
		if (file == null) return false;
		// heuristic: read the first up to 8000 bytes, and say that it is binary if
		// it contains a NUL
		try {
			final FileInputStream in = new FileInputStream(file);
			int left = 8000;
			final byte[] buffer = new byte[left];
			while (left > 0) {
				final int count = in.read(buffer, 0, left);
				if (count < 0) break;
				for (int i = 0; i < count; i++)
					if (buffer[i] == 0) {
						in.close();
						return true;
					}
				left -= count;
			}
			in.close();
			return false;
		}
		catch (final IOException e) {
			return false;
		}
	}

	/**
	 * Open a new tab with some content; the languageExtension is like ".java",
	 * ".py", etc.
	 */
	public Tab newTab(final String content, String language) {
		final Tab tab = open(null);
		if (null != language && language.length() > 0) {
			language = language.trim().toLowerCase();
			if ('.' != language.charAt(0)) language = "." + language;
			tab.editorPane.setLanguage(scriptService.getByName(language));
		}
		if (null != content) tab.editorPane.textArea.setText(content);
		return tab;
	}

	public Tab open(final File file) {
		if (isBinary(file)) {
			// TODO!
			throw new RuntimeException("TODO: open image using IJ2");
			// return null;
		}

		try {
			Tab tab = getTab();
			final boolean wasNew = tab != null && tab.editorPane.isNew();
			if (!wasNew) {
				tab = new Tab(this);
				addDefaultAccelerators(tab.editorPane.textArea);
			}
			synchronized (tab.editorPane) {
				tab.editorPane.open(file);
				if (wasNew) {
					final int index = tabbed.getSelectedIndex() + tabsMenuTabsStart;
					tabsMenu.getItem(index).setText(tab.editorPane.getFileName());
				}
				else {
					tabbed.addTab("", tab);
					switchTo(tabbed.getTabCount() - 1);
					tabsMenuItems.add(addToMenu(tabsMenu, tab.editorPane.getFileName(),
						0, 0));
				}
				setFileName(tab.editorPane.file);
				try {
					updateTabAndFontSize(true);
				}
				catch (final NullPointerException e) {
					/* ignore */
				}
			}
			if (file != null) openRecent.add(file.getAbsolutePath());

			return tab;
		}
		catch (final FileNotFoundException e) {
			Log.error(e);
			error("The file '" + file + "' was not found.");
		}
		catch (final Exception e) {
			Log.error(e);
			error("There was an error while opening '" + file + "': " + e);
		}
		return null;
	}

	public boolean saveAs() {
		throw new RuntimeException("TODO");
		/* TODO:
		final EditorPane editorPane = getEditorPane();
		final SaveDialog sd =
			new SaveDialog("Save as ", editorPane.file == null ? AppUtils.getBaseDirectory().getAbsolutePath()
				 : editorPane.file.getParentFile()
				.getAbsolutePath(), editorPane.getFileName(), "");
		grabFocus(2);
		final String name = sd.getFileName();
		if (name == null) return false;

		final String path = sd.getDirectory() + name;
		return saveAs(path, true);
		*/
	}

	public void saveAs(final String path) {
		saveAs(path, true);
	}

	public boolean saveAs(final String path, final boolean askBeforeReplacing) {
		final File file = new File(path);
		if (file.exists() &&
			askBeforeReplacing &&
			JOptionPane.showConfirmDialog(this, "Do you want to replace " + path +
				"?", "Replace " + path + "?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return false;
		if (!write(file)) return false;
		setFileName(file);
		openRecent.add(path);
		return true;
	}

	public boolean save() {
		final File file = getEditorPane().file;
		if (file == null) return saveAs();
		if (!write(file)) return false;
		setTitle();
		return true;
	}

	public boolean write(final File file) {
		try {
			getEditorPane().write(file);
			return true;
		}
		catch (final IOException e) {
			Log.error(e);
			error("Could not save " + file.getName());
			return false;
		}
	}

	/* TODO!
	public boolean makeJar(final boolean includeSources) {
		final File file = getEditorPane().file;
		final ScriptEngineFactory currentLanguage = getCurrentLanguage();
		if ((file == null || currentLanguage instanceof CompiledLanguage) &&
			!handleUnsavedChanges(true)) return false;

		String name = getEditorPane().getFileName();
		if (name.endsWith(currentLanguage.extension)) name =
			name.substring(0, name.length() - currentLanguage.extension.length());
		if (name.indexOf('_') < 0) name += "_";
		name += ".jar";
		final SaveDialog sd = new SaveDialog("Export ", name, ".jar");
		grabFocus(2);
		name = sd.getFileName();
		if (name == null) return false;

		final String path = sd.getDirectory() + name;
		if (new File(path).exists() &&
			JOptionPane.showConfirmDialog(this, "Do you want to replace " + path +
				"?", "Replace " + path + "?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return false;
		try {
			makeJar(path, includeSources);
			return true;
		}
		catch (final IOException e) {
			Log.error(e);
			error("Could not write " + path + ": " + e.getMessage());
			return false;
		}
	}

	public void makeJar(final String path, final boolean includeSources)
		throws IOException
	{
		final List<String> paths = new ArrayList<String>();
		final List<String> names = new ArrayList<String>();
		File tmpDir = null;
		final File file = getEditorPane().file;
		String sourceName = null;
		final ScriptEngineFactory currentLanguage = getCurrentLanguage();
		if (!(currentLanguage.interpreterClass == Refresh_Javas.class)) sourceName =
			file.getName();
		if (!currentLanguage.menuLabel.equals("None")) try {
			tmpDir = File.createTempFile("tmp", "");
			tmpDir.delete();
			tmpDir.mkdir();

			String sourcePath;
			Refresh_Javas java;
			if (sourceName == null) {
				sourcePath = file.getAbsolutePath();
				java = (Refresh_Javas) currentLanguage.newInterpreter();
			}
			else {
				// this is a script, we need to generate a Java wrapper
				final RefreshScripts interpreter = currentLanguage.newInterpreter();
				sourcePath = generateScriptWrapper(tmpDir, sourceName, interpreter);
				java = (Refresh_Javas) Languages.get(".java").newInterpreter();
			}
			java.showDeprecation(showDeprecation.getState());
			java.compile(sourcePath, tmpDir.getAbsolutePath());
			getClasses(tmpDir, paths, names);
			if (includeSources) {
				String name = java.getPackageName(sourcePath);
				name =
					(name == null ? "" : name.replace('.', '/') + "/") + file.getName();
				sourceName = name;
			}
		}
		catch (final Exception e) {
			Log.error(e);
			if (e instanceof IOException) throw (IOException) e;
			throw new IOException(e.getMessage());
		}

		final OutputStream out = new FileOutputStream(path);
		final JarOutputStream jar = new JarOutputStream(out);

		if (sourceName != null) writeJarEntry(jar, sourceName, getTextComponent()
			.getText().getBytes());
		for (int i = 0; i < paths.size(); i++)
			writeJarEntry(jar, names.get(i), readFile(paths.get(i)));

		jar.close();

		if (tmpDir != null) deleteRecursively(tmpDir);
	}
	*/

	void setLanguage(final ScriptEngineFactory language) {
		getEditorPane().setLanguage(language);
		updateTabAndFontSize(true);
	}

	void updateLanguageMenu(final ScriptEngineFactory language) {
		final JRadioButtonMenuItem button = this.languageMenuItems.get(language);
		if (!button.isSelected()) button.setSelected(true);

		final boolean isRunnable = language != null;
		final boolean isCompileable =
			isRunnable && language instanceof CompiledLanguage;
		runMenu.setVisible(isRunnable);
		compileAndRun.setText(isCompileable ? "Compile and Run" : "Run");
		compileAndRun.setEnabled(isRunnable);
		runSelection.setVisible(isRunnable && !isCompileable);
		// TODO! compile.setVisible(isCompileable);
		autoSave.setVisible(isCompileable);
		// TODO! showDeprecation.setVisible(isCompileable);
		// TODO! makeJarWithSource.setVisible(isCompileable);

		/* TODO!
		final boolean isJava = language.getLanguageName().equals("Java");
		addImport.setVisible(isJava);
		removeUnusedImports.setVisible(isJava);
		sortImports.setVisible(isJava);
		openSourceForMenuItem.setVisible(isJava);
		*/

		final boolean isMacro = language.getLanguageName().equals("ImageJ Macro");
		/* TODO!
		installMacro.setVisible(isMacro);
		openMacroFunctions.setVisible(isMacro);
		openSourceForClass.setVisible(!isMacro);
		*/

		openHelp.setVisible(!isMacro && isRunnable);
		openHelpWithoutFrames.setVisible(!isMacro && isRunnable);
		nextError.setVisible(!isMacro && isRunnable);
		previousError.setVisible(!isMacro && isRunnable);

		if (getEditorPane() == null) return;
		// TODO! final boolean isInGit = getEditorPane().getGitDirectory() != null;
		// TODO! gitMenu.setVisible(isInGit);

		updateTabAndFontSize(false);
	}

	protected void updateTabAndFontSize(final boolean setByLanguage) {
		final EditorPane pane = getEditorPane();
		if (setByLanguage) pane.setTabSize(pane.currentLanguage.getLanguageName()
			.equals("Python") ? 4 : 8);
		/* TODO!
		final int tabSize = pane.getTabSize();
		boolean defaultSize = false;
		for (int i = 0; i < tabSizeMenu.getItemCount(); i++) {
			final JMenuItem item = tabSizeMenu.getItem(i);
			if (item == chooseTabSize) {
				item.setSelected(!defaultSize);
				item.setLabel("Other" + (defaultSize ? "" : " (" + tabSize + ")") +
					"...");
			}
			else if (tabSize == Integer.parseInt(item.getLabel())) {
				item.setSelected(true);
				defaultSize = true;
			}
		}
		final int fontSize = (int) pane.getFontSize();
		defaultSize = false;
		for (int i = 0; i < fontSizeMenu.getItemCount(); i++) {
			final JMenuItem item = fontSizeMenu.getItem(i);
			if (item == chooseFontSize) {
				item.setSelected(!defaultSize);
				item.setLabel("Other" + (defaultSize ? "" : " (" + fontSize + ")") +
					"...");
				continue;
			}
			String label = item.getLabel();
			if (label.endsWith(" pt")) label = label.substring(0, label.length() - 3);
			if (fontSize == Integer.parseInt(label)) {
				item.setSelected(true);
				defaultSize = true;
			}
		}
		wrapLines.setState(pane.getLineWrap());
		*/
	}

	public void setFileName(final String baseName) {
		getEditorPane().setFileName(baseName);
	}

	public void setFileName(final File file) {
		getEditorPane().setFileName(file);
	}

	synchronized void setTitle() {
		final Tab tab = getTab();
		if (null == tab || null == tab.editorPane) return;
		final boolean fileChanged = tab.editorPane.fileChanged();
		final String fileName = tab.editorPane.getFileName();
		final String title =
			(fileChanged ? "*" : "") + fileName +
				(executingTasks.isEmpty() ? "" : " (Running)");
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				setTitle(title); // to the main window
				// final int index = tabbed.getSelectedIndex();
				// Update all tabs: could have changed
				for (int i = 0; i < tabbed.getTabCount(); i++)
					tabbed.setTitleAt(i, ((Tab) tabbed.getComponentAt(i)).getTitle());
			}
		});
	}

	@Override
	public synchronized void setTitle(final String title) {
		super.setTitle(title);
		final int index = tabsMenuTabsStart + tabbed.getSelectedIndex();
		if (index < tabsMenu.getItemCount()) {
			final JMenuItem item = tabsMenu.getItem(index);
			if (item != null) item.setText(title);
		}
	}

	/** Using a Vector to benefit from all its methods being synchronzed. */
	final ArrayList<Executer> executingTasks = new ArrayList<Executer>();

	/**
	 * Query the list of running scripts and provide a dialog to choose one and
	 * kill it.
	 */
	/* TODO!
	public void chooseTaskToKill() {
		final Executer[] executers = executingTasks.toArray(new Executer[0]);
		if (0 == executers.length) {
			error("\nNo tasks running!\n");
			return;
		}

		final String[] names = new String[executers.length];
		for (int i = 0; i < names.length; i++)
			names[i] = executers[i].getName();

		final GenericDialog gd = new GenericDialog("Kill");
		gd.addChoice("Running scripts: ", names, names[names.length - 1]);
		gd.addCheckbox("Kill all", false);
		gd.showDialog();
		if (gd.wasCanceled()) return;

		if (gd.getNextBoolean()) {
			// Kill all
			for (int i = 0; i < tabbed.getTabCount(); i++)
				((Tab) tabbed.getComponentAt(i)).kill();
		}
		else {
			// Kill selected only
			final Executer ex = executers[gd.getNextChoiceIndex()];
			for (int i = 0; i < tabbed.getTabCount(); i++) {
				final Tab tab = (Tab) tabbed.getComponentAt(i);
				if (ex == tab.executer) {
					tab.kill();
					break;
				}
			}
		}
	}
	*/

	/**
	 * Run the text in the textComponent without compiling it, only if it's not
	 * java.
	 */
	public void runText() {
		runText(false);
	}

	public void runText(final boolean selectionOnly) {
		final ScriptEngineFactory currentLanguage = getCurrentLanguage();
		if (currentLanguage == null) {
			error("Select a language first!");
			// TODO guess the language, if possible.
			return;
		}
		if (currentLanguage instanceof CompiledLanguage) {
			if (selectionOnly) {
				error("Cannot run selection of compiled language!");
				return;
			}
			if (handleUnsavedChanges(true)) runScript();
			return;
		}
		markCompileStart();

		try {
			final Tab tab = getTab();
			tab.showOutput();
			tab.execute(currentLanguage, selectionOnly);
		}
		catch (final Throwable t) {
			Log.error(t);
		}
	}

	public void runScript() {
		final ScriptEngineFactory factory = getCurrentLanguage();
		if (factory == null) {
			error("There is no interpreter for this language");
			return;
		}

		final ScriptEngine engine = factory.getScriptEngine();

		if (factory instanceof CompiledLanguage) getTab().showErrors();
		else getTab().showOutput();

		markCompileStart();
		final JTextAreaWriter writer = new JTextAreaWriter(getTab().screen);
		final JTextAreaWriter errorWriter = new JTextAreaWriter(errorScreen);
		final File file = getEditorPane().file;
		scriptService.initialize(engine, file.getPath(), writer, errorWriter);

		new Executer(this, writer, errorWriter) {

			@Override
			public void execute() {
				try {
					engine.eval(new FileReader(file));
				}
				catch (final FileNotFoundException e) {
					handleException(e);
				}
				catch (final ScriptException e) {
					handleException(e);
				}
				writer.flush();
				errorWriter.flush();
				markCompileEnd();
			}
		};
	}

	public void compile() {
		if (!handleUnsavedChanges(true)) return;

		/* TODO!
		final ScriptEngine interpreter = getCurrentLanguage().getScriptEngine();
		final JTextAreaWriter writer = new JTextAreaWriter(getTab().screen);
		final JTextAreaWriter errorWriter = new JTextAreaWriter(errorScreen);
		getTab().showErrors();
		if (interpreter instanceof Refresh_Javas) {
			final Refresh_Javas java = (Refresh_Javas) interpreter;
			final File file = getEditorPane().file;
			final String sourcePath = file.getAbsolutePath();
			java.showDeprecation(showDeprecation.getState());
			markCompileStart();
			new Thread() {

				@Override
				public void run() {
					java.compileAndRun(sourcePath, true);
					errorScreen.insert("Compilation finished.\n", errorScreen
						.getDocument().getLength());
					markCompileEnd();
				}
			}.start();
		}
		*/
	}

	public String getSelectedTextOrAsk(final String label) {
		String selection = getTextComponent().getSelectedText();
		if (selection == null || selection.indexOf('\n') >= 0) {
			selection =
				JOptionPane.showInputDialog(this, label + ":", label + "...",
					JOptionPane.QUESTION_MESSAGE);
			if (selection == null) return null;
		}
		return selection;
	}

	protected static void append(final JTextArea textArea, final String text) {
		final int length = textArea.getDocument().getLength();
		textArea.insert(text, length);
		textArea.setCaretPosition(length);
	}

	public void markCompileStart() {
		errorHandler = null;

		final String started =
			"Started " + getEditorPane().getFileName() + " at " + new Date() + "\n";
		final int offset = errorScreen.getDocument().getLength();
		append(errorScreen, started);
		append(getTab().screen, started);
		compileStartOffset = errorScreen.getDocument().getLength();
		try {
			compileStartPosition = errorScreen.getDocument().createPosition(offset);
		}
		catch (final BadLocationException e) {
			handleException(e);
		}
	}

	public void markCompileEnd() {
		if (errorHandler == null) {
			errorHandler =
				new ErrorHandler(getCurrentLanguage(), errorScreen,
					compileStartPosition.getOffset());
			if (errorHandler.getErrorCount() > 0) getTab().showErrors();
		}
		if (compileStartOffset != errorScreen.getDocument().getLength()) getTab()
			.showErrors();
		if (getTab().showingErrors) try {
			errorHandler.scrollToVisible(compileStartOffset);
		}
		catch (final BadLocationException e) {
			// ignore
		}
	}

	/* TODO!
	public void installMacro() {
		new MacroFunctions().installMacro(getTitle(), getEditorPane().getText());
	}
	*/

	public boolean nextError(final boolean forward) {
		if (errorHandler != null && errorHandler.nextError(forward)) try {
			File file = new File(errorHandler.getPath());
			if (!file.isAbsolute()) file = getFileForBasename(file.getName());
			errorHandler.markLine();
			switchTo(file, errorHandler.getLine());
			getTab().showErrors();
			errorScreen.invalidate();
			return true;
		}
		catch (final Exception e) {
			handleException(e);
		}
		return false;
	}

	public void switchTo(final String path, final int lineNumber)
		throws IOException
	{
		switchTo(new File(path).getCanonicalFile(), lineNumber);
	}

	public void switchTo(final File file, final int lineNumber) {
		if (!editorPaneContainsFile(getEditorPane(), file)) switchTo(file);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					gotoLine(lineNumber);
				}
				catch (final BadLocationException e) {
					// ignore
				}
			}
		});
	}

	public void switchTo(final File file) {
		for (int i = 0; i < tabbed.getTabCount(); i++)
			if (editorPaneContainsFile(getEditorPane(i), file)) {
				switchTo(i);
				return;
			}
		open(file);
	}

	public void switchTo(final int index) {
		if (index == tabbed.getSelectedIndex()) return;
		tabbed.setSelectedIndex(index);
	}

	protected void switchTabRelative(final int delta) {
		int index = tabbed.getSelectedIndex();
		final int count = tabbed.getTabCount();
		index = ((index + delta) % count);
		if (index < 0) index += count;
		switchTo(index);
	}

	protected void removeTab(int index) {
		tabbed.remove(index);
		index += tabsMenuTabsStart;
		tabsMenuItems.remove(tabsMenu.getItem(index));
		tabsMenu.remove(index);
	}

	boolean editorPaneContainsFile(final EditorPane editorPane, final File file) {
		try {
			return file != null && editorPane != null && editorPane.file != null &&
				file.getCanonicalFile().equals(editorPane.file.getCanonicalFile());
		}
		catch (final IOException e) {
			return false;
		}
	}

	public File getFile() {
		return getEditorPane().file;
	}

	public File getFileForBasename(final String baseName) {
		File file = getFile();
		if (file != null && file.getName().equals(baseName)) return file;
		for (int i = 0; i < tabbed.getTabCount(); i++) {
			file = getEditorPane(i).file;
			if (file != null && file.getName().equals(baseName)) return file;
		}
		return null;
	}

	/* todo!
	public void addImport(String className) {
		if (className == null) className = getSelectedClassNameOrAsk();
		if (className != null) new TokenFunctions(getTextComponent())
			.addImport(className.trim());
	}

	public void openHelp(final String className) {
		openHelp(className, true);
	}

	public void openHelp(String className, final boolean withFrames) {
		if (className == null) className = getSelectedClassNameOrAsk();
		if (className != null) getEditorPane().getClassNameFunctions()
			.openHelpForClass(className, withFrames);
	}

	public void extractSourceJar() {
		final String path =
			openWithDialog("Open...", null, new String[] { ".jar" }, true);
		if (path != null) extractSourceJar(path);
	}

	public void extractSourceJar(final String path) {
		try {
			final FileFunctions functions = new FileFunctions(this);
			final List<String> paths = functions.extractSourceJar(path);
			for (final String file : paths)
				if (!functions.isBinaryFile(file)) {
					open(file);
					final EditorPane pane = getEditorPane();
					new TokenFunctions(pane).removeTrailingWhitespace();
					if (pane.fileChanged()) save();
				}
		}
		catch (final IOException e) {
			error("There was a problem opening " + path + ": " + e.getMessage());
		}
	}
	*/

	/* extensionMustMatch == false means extension must not match */
	protected File openWithDialog(final String title, final File defaultDir,
		final String[] extensions, final boolean extensionMustMatch)
	{
		final JFileChooser dialog = new JFileChooser();
		dialog.setDialogTitle(title);
		if (defaultDir != null) dialog.setCurrentDirectory(defaultDir);
		if (extensions != null) {
			final FileFilter fileFilter = new FileFilter() {

				@Override
				public boolean accept(final File file) {
					final String name = file.getName();
					for (final String extension : extensions)
						if (name.endsWith(extension)) return extensionMustMatch;
					return !extensionMustMatch;
				}

				@Override
				public String getDescription() {
					final StringBuilder builder = new StringBuilder();
					String separator = extensionMustMatch ? "Only " : "No ";
					for (final String extension : extensions) {
						builder.append(separator).append(extension);
						separator = ", ";
					}
					builder.append(" files");
					return builder.toString();
				}
			};
			dialog.addChoosableFileFilter(fileFilter);
			dialog.setFileFilter(fileFilter);
		}
		if (dialog.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return null;
		return dialog.getSelectedFile();
	}

	/**
	 * Write a message to the output screen
	 * 
	 * @param message The text to write
	 */
	public void write(String message) {
		final Tab tab = getTab();
		if (!message.endsWith("\n")) message += "\n";
		tab.screen.insert(message, tab.screen.getDocument().getLength());
	}

	public void writeError(String message) {
		final Tab tab = getTab();
		tab.showErrors();
		if (!message.endsWith("\n")) message += "\n";
		errorScreen.insert(message, errorScreen.getDocument().getLength());
	}

	protected void error(final String message) {
		JOptionPane.showMessageDialog(this, message);
	}

	void handleException(final Throwable e) {
		// TODO! hand off to ExceptionHandler
		Log.error(e);
	}

	/**
	 * Removes invalid characters, shows a dialog.
	 * 
	 * @return The amount of invalid characters found.
	 */
	public int zapGremlins() {
		final int count = getEditorPane().zapGremlins();
		final String msg =
			count > 0 ? "Zap Gremlins converted " + count +
				" invalid characters to spaces" : "No invalid characters found!";
		JOptionPane.showMessageDialog(this, msg);
		return count;
	}

	public void gotoBookmark(final Bookmark bookmark) {
		// TODO Auto-generated method stub
		throw new RuntimeException("TODO");
	}

}
