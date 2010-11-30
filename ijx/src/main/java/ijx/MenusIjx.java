package ijx;

import ijx.process.ImageProcessor;
import ijx.util.StringSorter;
import ijx.app.IjxApplication;

import ijx.gui.IjxWindow;
import ijx.plugin.MacroInstaller;
import java.util.*;
import java.io.*;
import java.applet.Applet;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.zip.*;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

/**
This class installs and updates ImageJ's menus. Note that menu labels,
even in submenus, must be unique. This is because ImageJ uses a single
hash table for all menu labels. If you look closely, you will see that
File->Import->Text Image... and File->Save As->Text Image... do not use
the same label. One of the labels has an extra space.

@see ImageJ
 */
public class MenusIjx {
    public static final char PLUGINS_MENU = 'p';
    public static final char IMPORT_MENU = 'i';
    public static final char SAVE_AS_MENU = 's';
    public static final char SHORTCUTS_MENU = 'h'; // 'h'=hotkey
    public static final char ABOUT_MENU = 'a';
    public static final char FILTERS_MENU = 'f';
    public static final char TOOLS_MENU = 't';
    public static final char UTILITIES_MENU = 'u';

    public static final int NORMAL_RETURN = 0;
    public static final int COMMAND_IN_USE = -1;
    public static final int INVALID_SHORTCUT = -2;
    public static final int SHORTCUT_IN_USE = -3;
    public static final int NOT_INSTALLED = -4;
    public static final int COMMAND_NOT_FOUND = -5;
    public static final int MAX_OPEN_RECENT_ITEMS = 15;
    static final int RGB_STACK = 10, HSB_STACK = 11;
    //
    public static final int WINDOW_MENU_ITEMS = 5; // fixed items at top of Window menu
    //
    private static IjxTopComponent topComponent;
    private static IjxApplication ijApp;
    //
    private static JMenuBar mbar;
    private static JMenu pluginsMenu, importMenu, saveAsMenu, shortcutsMenu, aboutMenu, filtersMenu, toolsMenu, utilitiesMenu, macrosMenu, optionsMenu;
    public static JMenu window, openRecentMenu;
    private static JCheckBoxMenuItem gray8Item, gray16Item, gray32Item, color256Item, colorRGBItem, RGBStackItem,
            HSBStackItem;
    private static JPopupMenu popup;
    //
    static boolean jnlp; // true when using Java WebStart
    private static Applet applet;
    //private static Hashtable demoImagesTable = new Hashtable();
    //
    private static String pluginsPath, macrosPath;
    public int nPlugins, nMacros;
    private static Hashtable pluginsTable;
    private static Hashtable shortcuts = new Hashtable();
    private static Hashtable macroShortcuts;
    private static Vector<String> pluginsPrefs = new Vector<String>(); // commands saved in IJ_Prefs
    static int windowMenuItems2; // non-image windows listed in Window menu + separator
    private static String error;
    private String jarError;
    private String pluginError;
    private boolean isJarErrorHeading;
    private boolean installingJars, duplicateCommand;
    private static Vector jarFiles;  // JAR files in plugins folder with "_" in their name
    private static Vector macroFiles;  // Macros and scripts in the plugins folder
    private static Hashtable menusTable; // Submenus of Plugins menu
    private int userPluginsIndex; // First user plugin or submenu in Plugins menu
    private int importCount, saveAsCount, toolsCount, optionsCount;
    private boolean addSorted;
    //




    public MenusIjx(IjxTopComponent tc, IjxApplication _ijApp, Applet appletInstance) {
        topComponent = tc;
        ijApp = _ijApp;
        applet = appletInstance;
    }

    public String addMenuBar() {
        error = null;
        pluginsTable = new Hashtable();

        JMenu file = new JMenu("File");
        addSubMenu(file, "New");
        addPlugInItem(file, "Open...", "ijx.core.Commands(\"open\")", KeyEvent.VK_O, false);
        addPlugInItem(file, "Open Next", "ijx.plugin.NextImageOpener", KeyEvent.VK_O, true);
        addSubMenu(file, "Open Samples");
        addOpenRecentSubMenu(file);
        importMenu = addSubMenu(file, "Import");
        file.addSeparator();
        addPlugInItem(file, "Close", "ijx.core.Commands(\"close\")", KeyEvent.VK_W, false);
        addPlugInItem(file, "Save", "ijx.core.Commands(\"save\")", KeyEvent.VK_S, false);
        saveAsMenu = addSubMenu(file, "Save As");
        addPlugInItem(file, "Revert", "ijx.core.Commands(\"revert\")", KeyEvent.VK_R, false);
        file.addSeparator();
        addPlugInItem(file, "Page Setup...", "ijx.plugin.filter.Printer(\"setup\")", 0, false);
        addPlugInItem(file, "Print...", "ijx.plugin.filter.Printer(\"print\")", KeyEvent.VK_P, false);
        file.addSeparator();
        addPlugInItem(file, "Quit", "ijx.core.Commands(\"quit\")", 0, false);

        JMenu edit = new JMenu("Edit");
        addPlugInItem(edit, "Undo", "ijx.core.Commands(\"undo\")", KeyEvent.VK_Z, false);
        edit.addSeparator();
        addPlugInItem(edit, "Cut", "ijx.core.Clipboard(\"cut\")", KeyEvent.VK_X, false);
        addPlugInItem(edit, "Copy", "ijx.core.Clipboard(\"copy\")", KeyEvent.VK_C, false);
        addPlugInItem(edit, "Copy to System", "ijx.core.Clipboard(\"scopy\")", 0, false);
        addPlugInItem(edit, "Paste", "ijx.core.Clipboard(\"paste\")", KeyEvent.VK_V, false);
        addPlugInItem(edit, "Paste Control...", "ijx.plugin.frame.PasteController", 0, false);
        edit.addSeparator();
        addPlugInItem(edit, "Clear", "ijx.plugin.filter.Filler(\"clear\")", 0, false);
        addPlugInItem(edit, "Clear Outside", "ijx.plugin.filter.Filler(\"outside\")", 0, false);
        addPlugInItem(edit, "Fill", "ijx.plugin.filter.Filler(\"fill\")", KeyEvent.VK_F, false);
        addPlugInItem(edit, "Draw", "ijx.plugin.filter.Filler(\"draw\")", KeyEvent.VK_D, false);
        addPlugInItem(edit, "Invert", "ijx.plugin.filter.Filters(\"invert\")", KeyEvent.VK_I, true);
        edit.addSeparator();
        addSubMenu(edit, "Selection");
        optionsMenu = addSubMenu(edit, "Options");

        JMenu image = new JMenu("Image");
        JMenu imageType = new JMenu("Type");
        gray8Item = addCheckboxItem(imageType, "8-bit", "ijx.plugin.Converter(\"8-bit\")");
        gray16Item = addCheckboxItem(imageType, "16-bit", "ijx.plugin.Converter(\"16-bit\")");
        gray32Item = addCheckboxItem(imageType, "32-bit", "ijx.plugin.Converter(\"32-bit\")");
        color256Item = addCheckboxItem(imageType, "8-bit Color", "ijx.plugin.Converter(\"8-bit Color\")");
        colorRGBItem = addCheckboxItem(imageType, "RGB Color", "ijx.plugin.Converter(\"RGB Color\")");
        imageType.add(new JMenuItem("-"));
        RGBStackItem = addCheckboxItem(imageType, "RGB Stack", "ijx.plugin.Converter(\"RGB Stack\")");
        HSBStackItem = addCheckboxItem(imageType, "HSB Stack", "ijx.plugin.Converter(\"HSB Stack\")");
        image.add(imageType);

        image.addSeparator();
        addSubMenu(image, "Adjust");
        addPlugInItem(image, "Show Info...", "ijx.plugin.filter.Info", KeyEvent.VK_I, false);
        addPlugInItem(image, "Properties...", "ijx.plugin.filter.ImageProperties", KeyEvent.VK_P, true);
        //addSubMenu(image, "Benchmarks");
        addSubMenu(image, "Color");
        addSubMenu(image, "Stacks");
        addSubMenu(image, "Hyperstacks");
        image.addSeparator();
        addPlugInItem(image, "Crop", "ijx.plugin.filter.Resizer(\"crop\")", KeyEvent.VK_X, true);
        addPlugInItem(image, "Duplicate...", "ijx.plugin.filter.Duplicater", KeyEvent.VK_D, true);
        addPlugInItem(image, "Rename...", "ijx.plugin.SimpleCommands(\"rename\")", 0, false);
        addPlugInItem(image, "Scale...", "ijx.plugin.Scaler", KeyEvent.VK_E, false);
        addPlugInItem(image, "Translate...", "ijx.plugin.filter.Translator", 0, false);
        addSubMenu(image, "Rotate");
        addSubMenu(image, "Zoom");
        image.addSeparator();
        addSubMenu(image, "Lookup Tables");

        JMenu process = new JMenu("Process");
        addPlugInItem(process, "Smooth", "ijx.plugin.filter.Filters(\"smooth\")", KeyEvent.VK_S, true);
        addPlugInItem(process, "Sharpen", "ijx.plugin.filter.Filters(\"sharpen\")", 0, false);
        addPlugInItem(process, "Find Edges", "ijx.plugin.filter.Filters(\"edge\")", 0, false);
        addPlugInItem(process, "Enhance Contrast", "ijx.plugin.ContrastEnhancer", 0, false);
        addSubMenu(process, "Noise");
        addSubMenu(process, "Shadows");
        addSubMenu(process, "Binary");
        addSubMenu(process, "Math");
        addSubMenu(process, "FFT");
        filtersMenu = addSubMenu(process, "Filters");
        process.addSeparator();
        addPlugInItem(process, "Image Calculator...", "ijx.plugin.ImageCalculator", 0, false);
        addPlugInItem(process, "Subtract Background...", "ijx.plugin.filter.BackgroundSubtracter", 0, false);
        addItem(process, "Repeat Command", KeyEvent.VK_R, true);

        JMenu analyze = new JMenu("Analyze");
        addPlugInItem(analyze, "Measure", "ijx.plugin.filter.Analyzer", KeyEvent.VK_M, false);
        addPlugInItem(analyze, "Analyze Particles...", "ijx.plugin.filter.ParticleAnalyzer", 0, false);
        addPlugInItem(analyze, "Summarize", "ijx.plugin.filter.Analyzer(\"sum\")", 0, false);
        addPlugInItem(analyze, "Distribution...", "ijx.plugin.Distribution", 0, false);
        addPlugInItem(analyze, "Label", "ijx.plugin.filter.Filler(\"label\")", 0, false);
        addPlugInItem(analyze, "Clear Results", "ijx.plugin.filter.Analyzer(\"clear\")", 0, false);
        addPlugInItem(analyze, "Set Measurements...", "ijx.plugin.filter.Analyzer(\"set\")", 0, false);
        analyze.addSeparator();
        addPlugInItem(analyze, "Set Scale...", "ijx.plugin.filter.ScaleDialog", 0, false);
        addPlugInItem(analyze, "Calibrate...", "ijx.plugin.filter.Calibrator", 0, false);
        addPlugInItem(analyze, "Histogram", "ijx.plugin.Histogram", KeyEvent.VK_H, false);
        addPlugInItem(analyze, "Plot Profile", "ijx.plugin.filter.Profiler(\"plot\")", KeyEvent.VK_K, false);
        addPlugInItem(analyze, "Surface Plot...", "ijx.plugin.SurfacePlotter", 0, false);
        addSubMenu(analyze, "Gels");
        toolsMenu = addSubMenu(analyze, "Tools");

        window = new JMenu("Window");
        addPlugInItem(window, "Show All", "ijx.plugin.WindowOrganizer(\"show\")", KeyEvent.VK_F, true);
        addPlugInItem(window, "Put Behind [tab]", "ijx.core.Commands(\"tab\")", 0, false);
        addPlugInItem(window, "Cascade", "ijx.plugin.WindowOrganizer(\"cascade\")", 0, false);
        addPlugInItem(window, "Tile", "ijx.plugin.WindowOrganizer(\"tile\")", 0, false);
        window.addSeparator();

        JMenu help = new JMenu("Help");
        addPlugInItem(help, "ImageJ Website...", "ijx.plugin.BrowserLauncher", 0, false);
        addPlugInItem(help, "ImageJ News...", "ijx.plugin.BrowserLauncher(\"" + IJ.URL + "/notes.html\")", 0, false);
        addPlugInItem(help, "Documentation...", "ijx.plugin.BrowserLauncher(\"" + IJ.URL + "/docs\")", 0, false);
        addPlugInItem(help, "Installation...", "ijx.plugin.SimpleCommands(\"install\")", 0, false);
        addPlugInItem(help, "Search Website...", "ijx.plugin.BrowserLauncher(\"" + IJ.URL + "/search.html\")", 0, false);
        addPlugInItem(help, "List Archives...", "ijx.plugin.BrowserLauncher(\"https://list.nih.gov/archives/imagej.html\")", 0, false);
        help.addSeparator();
        addPlugInItem(help, "Resources...", "ijx.plugin.BrowserLauncher(\"" + IJ.URL + "/developer/index.html\")", 0, false);
        addPlugInItem(help, "Plugins...", "ijx.plugin.BrowserLauncher(\"" + IJ.URL + "/plugins\")", 0, false);
        addPlugInItem(help, "Macros...", "ijx.plugin.BrowserLauncher(\"" + IJ.URL + "/macros/\")", 0, false);
        addPlugInItem(help, "Macro Functions...", "ijx.plugin.BrowserLauncher(\"" + IJ.URL + "/developer/macro/functions.html\")", 0, false);
        help.addSeparator();
        addPlugInItem(help, "Update ImageJ...", "ijx.plugin.ImageJ_Updater", 0, false);
        addPlugInItem(help, "Update Menus", "ijx.plugin.ImageJ_Updater(\"menus\")", 0, false);
        help.addSeparator();
        aboutMenu = addSubMenu(help, "About Plugins");
        addPlugInItem(help, "About ImageJ...", "ijx.plugin.AboutBox", 0, false);

        addPluginsMenu();
        if (applet == null) {
            installPluginsFromIJ_Prefs();
        }

        mbar = new JMenuBar();
        if (fontSize != 0) {
            mbar.setFont(getFont());
        }
        mbar.add(file);
        mbar.add(edit);
        mbar.add(image);
        mbar.add(process);
        mbar.add(analyze);
        mbar.add(pluginsMenu);
        mbar.add(window);
//    mbar.setHelpMenu(help);

        if (topComponent != null) {
            topComponent.setMenuBar(mbar);
        }

        if (pluginError != null) {
            error = error != null ? error += "\n" + pluginError : pluginError;
        }
        if (jarError != null) {
            error = error != null ? error += "\n" + jarError : jarError;
        }
        return error;
    }

    void addItem(JMenu menu, String label, int shortcut, boolean shift) {
        if (menu == null) {
            return;
        }
        JMenuItem item;
        if (shortcut == 0) {
            item = new JMenuItem(label);
        } else {
            if (shift) {
                item = new JMenuItem(label);
                //, new MenuShortcut(shortcut, true));
                item.setAccelerator(KeyStroke.getKeyStroke(shortcut,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
                shortcuts.put(new Integer(shortcut + 200), label);
            } else {
                item = new JMenuItem(label);
                //, new MenuShortcut(shortcut, true));
                item.setAccelerator(KeyStroke.getKeyStroke(shortcut,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

                //item = new JMenuItem(label, new MenuShortcut(shortcut));
                shortcuts.put(new Integer(shortcut), label);
            }
        }
        if (addSorted) {
            if (menu == pluginsMenu) {
                addItemSorted(menu, item, userPluginsIndex);
            } else {
                addOrdered(menu, item);
            }
        } else {
            menu.add(item);
        }
        item.addActionListener(ijApp);
    }

    void addPlugInItem(JMenu menu, String label, String className, int shortcut, boolean shift) {
        pluginsTable.put(label, className);
        nPlugins++;
        addItem(menu, label, shortcut, shift);
    }

    JCheckBoxMenuItem addCheckboxItem(JMenu menu, String label, String className) {
        pluginsTable.put(label, className);
        nPlugins++;
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(label);
        menu.add(item);
        item.addItemListener(ijApp);
        item.setState(false);
        return item;
    }

    JMenu addSubMenu(JMenu menu, String name) {
        String value;
        String key = name.toLowerCase(Locale.US);
        int index;
        JMenu submenu = new JMenu(name.replace('_', ' '));
        index = key.indexOf(' ');
        if (index > 0) {
            key = key.substring(0, index);
        }
        for (int count = 1; count < 100; count++) {
            value = Prefs.getString(key + (count / 10) % 10 + count % 10);
            if (value == null) {
                break;
            }
            if (count == 1) {
                menu.add(submenu);
            }
            if (value.equals("-")) {
                submenu.addSeparator();
            } else {
                addPluginItem(submenu, value);
            }
        }
        if (name.equals("Lookup Tables") && applet == null) {
            addLuts(submenu);
        }
        return submenu;
    }

    void addLuts(JMenu submenu) {
        String path = Prefs.getHomeDir() + File.separator;
        File f = new File(path + "luts");
        String[] list = null;
        if (applet == null && f.exists() && f.isDirectory()) {
            list = f.list();
        }
        if (list == null) {
            return;
        }
        if (IJ.isLinux()) {
            StringSorter.sort(list);
        }
        submenu.addSeparator();
        for (int i = 0; i < list.length; i++) {
            String name = list[i];
            if (name.endsWith(".lut")) {
                name = name.substring(0, name.length() - 4);
                JMenuItem item = new JMenuItem(name);
                submenu.add(item);
                item.addActionListener(ijApp);
                nPlugins++;
            }
        }
    }

    void addPluginItem(JMenu submenu, String s) {
        if (s.startsWith("\"-\"")) {
            // add menu separator if command="-"
            addSeparator(submenu);
            return;
        }
        int lastComma = s.lastIndexOf(',');
        if (lastComma <= 0) {
            return;
        }
        String command = s.substring(1, lastComma - 1);
        int keyCode = 0;
        boolean shift = false;
        if (command.endsWith("]")) {
            int openBracket = command.lastIndexOf('[');
            if (openBracket > 0) {
                String shortcut = command.substring(openBracket + 1, command.length() - 1);
                keyCode = convertShortcutToCode(shortcut);
                boolean functionKey = keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12;
                if (keyCode > 0 && !functionKey) {
                    command = command.substring(0, openBracket);
                }
                //IJ.write(command+": "+shortcut);
            }
        }
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
            shortcuts.put(new Integer(keyCode), command);
            keyCode = 0;
        } else if (keyCode >= 265 && keyCode <= 290) {
            keyCode -= 200;
            shift = true;
        }
        addItem(submenu, command, keyCode, shift);
        while (s.charAt(lastComma + 1) == ' ' && lastComma + 2 < s.length()) {
            lastComma++; // remove leading spaces
        }
        String className = s.substring(lastComma + 1, s.length());
        //IJ.log(command+"  "+className);
        if (installingJars) {
            duplicateCommand = pluginsTable.get(command) != null;
        }
        pluginsTable.put(command, className);
        nPlugins++;
    }

    void checkForDuplicate(String command) {
        if (pluginsTable.get(command) != null) {
        }
    }

    void addPluginsMenu() {
        String value, label, className;
        int index;
        pluginsMenu = new JMenu("Plugins");
        for (int count = 1; count < 100; count++) {
            value = Prefs.getString("plug-in" + (count / 10) % 10 + count % 10);
            if (value == null) {
                break;
            }
            char firstChar = value.charAt(0);
            if (firstChar == '-') {
                pluginsMenu.addSeparator();
            } else if (firstChar == '>') {
                String submenu = value.substring(2, value.length() - 1);
                JMenu menu = addSubMenu(pluginsMenu, submenu);
                if (submenu.equals("Shortcuts")) {
                    shortcutsMenu = menu;
                } else if (submenu.equals("Utilities")) {
                    utilitiesMenu = menu;
                } else if (submenu.equals("Macros")) {
                    macrosMenu = menu;
                }
            } else {
                addPluginItem(pluginsMenu, value);
            }
        }
        userPluginsIndex = pluginsMenu.getItemCount();
        if (userPluginsIndex < 0) {
            userPluginsIndex = 0;
        }
    }

    /** Install plugins using "pluginxx=" keys in IJ_Prefs.txt.
    Plugins not listed in IJ_Prefs are added to the end
    of the Plugins menu. */
    void installPluginsFromIJ_Prefs() {
        String value, className;
        char menuCode;
        JMenu menu;
        String[] plugins = getPlugins();
        String[] plugins2 = null;
        Hashtable skipList = new Hashtable();
        for (int index = 0; index < 100; index++) {
            value = Prefs.getString("plugin" + (index / 10) % 10 + index % 10);
            if (value == null) {
                break;
            }
            menuCode = value.charAt(0);
            switch (menuCode) {
                case PLUGINS_MENU:
                default:
                    menu = pluginsMenu;
                    break;
                case IMPORT_MENU:
                    menu = importMenu;
                    break;
                case SAVE_AS_MENU:
                    menu = saveAsMenu;
                    break;
                case SHORTCUTS_MENU:
                    menu = shortcutsMenu;
                    break;
                case ABOUT_MENU:
                    menu = aboutMenu;
                    break;
                case FILTERS_MENU:
                    menu = filtersMenu;
                    break;
                case TOOLS_MENU:
                    menu = toolsMenu;
                    break;
                case UTILITIES_MENU:
                    menu = utilitiesMenu;
                    break;
            }
            String prefsValue = value;
            value = value.substring(2, value.length()); //remove menu code and coma
            className = value.substring(value.lastIndexOf(',') + 1, value.length());
            boolean found = className.startsWith("ijx.");
            if (!found && plugins != null) { // does this plugin exist?
                if (plugins2 == null) {
                    plugins2 = getStrippedPlugins(plugins);
                }
                for (int i = 0; i < plugins2.length; i++) {
                    if (className.startsWith(plugins2[i])) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                addPluginItem(menu, value);
                pluginsPrefs.addElement(prefsValue);
                if (className.endsWith("\")")) { // remove any argument
                    int argStart = className.lastIndexOf("(\"");
                    if (argStart > 0) {
                        className = className.substring(0, argStart);
                    }
                }
                skipList.put(className, "");
            }
        }
        if (plugins != null) {
            for (int i = 0; i < plugins.length; i++) {
                if (!skipList.containsKey(plugins[i])) {
                    installUserPlugin(plugins[i]);
                }
            }
        }
        installJarPlugins();
        installMacros();
    }

    /** Inserts 'item' into 'menu' in alphanumeric order. */
    void addOrdered(JMenu menu, JMenuItem item) {
        if (menu == pluginsMenu) {
            menu.add(item);
            return;
        }
        String label = item.getLabel();
        for (int i = 0; i < menu.getItemCount(); i++) {
            if (label.compareTo(menu.getItem(i).getLabel()) < 0) {
                menu.insert(item, i);
                return;
            }
        }
        menu.add(item);
    }

    /** Install plugins located in JAR files. */
    void installJarPlugins() {
        if (jarFiles == null) {
            return;
        }
        installingJars = true;
        for (int i = 0; i < jarFiles.size(); i++) {
            isJarErrorHeading = false;
            String jar = (String) jarFiles.elementAt(i);
            InputStream is = getConfigurationFile(jar);
            if (is == null) {
                continue;
            }
            int maxEntries = 100;
            String[] entries = new String[maxEntries];
            int nEntries = 0, nEntries2 = 0;
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));
            try {
                while (true) {
                    String s = lnr.readLine();
                    if (s == null || nEntries == maxEntries - 1) {
                        break;
                    }
                    if (s.length() >= 3 && !s.startsWith("#")) {
                        entries[nEntries++] = s;
                        if (s.startsWith("Plugins>")) {
                            nEntries2++;
                        }
                    }
                }
            } catch (IOException e) {
            } finally {
                try {
                    if (lnr != null) {
                        lnr.close();
                    }
                } catch (IOException e) {
                }
            }
            for (int j = 0; j < nEntries; j++) {
                String s = entries[j];
                if (nEntries2 <= 3) {
                    if (s.startsWith("Plugins>")) {
                        int firstComma = s.indexOf(',');
                        int firstQuote = s.indexOf('"');
                        boolean pluginsDir = (new File(jar)).getParent().endsWith("plugins");
                        if (firstComma > 8 && firstQuote > firstComma && !pluginsDir) {
                            String submenuName = s.substring(8, firstComma);
                            String prefix = "";
                            // "Extended Depth of Field" and "Particle Detector & Tracker" plugins
                            if (submenuName.startsWith("Extend") || submenuName.startsWith("Particle D")) {
                                prefix = submenuName + ": ";
                            }
                            //IJ.log(nEntries+" "+nEntries2+" "+jar+" "+s+"  "+submenuName);
                            s = "Plugins, \"" + prefix + s.substring(firstQuote + 1, s.length());
                        }
                    }
                }
                installJarPlugin(jar, s);
            }
        }
    }

    /** Install a plugin located in a JAR file. */
    void installJarPlugin(String jar, String s) {
        addSorted = false;
        JMenu menu;
        if (s.startsWith("Plugins>")) {
            int firstComma = s.indexOf(',');
            if (firstComma == -1 || firstComma <= 8) {
                menu = null;
            } else {
                String name = s.substring(8, firstComma);
                menu = getPluginsSubmenu(name);
            }
        } else if (s.startsWith("\"") || s.startsWith("Plugins")) {
            String name = getSubmenuName(jar);
            if (name != null) {
                menu = getPluginsSubmenu(name);
            } else {
                menu = pluginsMenu;
            }
            addSorted = true;
        } else if (s.startsWith("File>Import")) {
            menu = importMenu;
            if (importCount == 0) {
                addSeparator(menu);
            }
            importCount++;
        } else if (s.startsWith("File>Save")) {
            menu = saveAsMenu;
            if (saveAsCount == 0) {
                addSeparator(menu);
            }
            saveAsCount++;
        } else if (s.startsWith("Analyze>Tools")) {
            menu = toolsMenu;
            if (toolsCount == 0) {
                addSeparator(menu);
            }
            toolsCount++;
        } else if (s.startsWith("Help>About")) {
            menu = aboutMenu;
        } else if (s.startsWith("Edit>Options")) {
            menu = optionsMenu;
            if (optionsCount == 0) {
                addSeparator(menu);
            }
            optionsCount++;
        } else {
            if (jarError == null) {
                jarError = "";
            }
            addJarErrorHeading(jar);
            jarError += "    Invalid menu: " + s + "\n";
            return;
        }
        int firstQuote = s.indexOf('"');
        if (firstQuote == -1) {
            return;
        }
        s = s.substring(firstQuote, s.length()); // remove menu
        if (menu != null) {
            addPluginItem(menu, s);
            addSorted = false;
        }
        if (duplicateCommand) {
            if (jarError == null) {
                jarError = "";
            }
            addJarErrorHeading(jar);
            jarError += "    Duplicate command: " + s + "\n";
        }
        duplicateCommand = false;
    }

    void addJarErrorHeading(String jar) {
        if (!isJarErrorHeading) {
            if (!jarError.equals("")) {
                jarError += " \n";
            }
            jarError += "Plugin configuration error: " + jar + "\n";
            isJarErrorHeading = true;
        }
    }

    JMenu getPluginsSubmenu(String submenuName) {
        if (menusTable != null) {
            JMenu menu = (JMenu) menusTable.get(submenuName);
            if (menu != null) {
                return menu;
            }
        }
        JMenu menu = new JMenu(submenuName);
        //pluginsMenu.add(menu);
        addItemSorted(pluginsMenu, menu, userPluginsIndex);
        if (menusTable == null) {
            menusTable = new Hashtable();
        }
        menusTable.put(submenuName, menu);
        //IJ.log("getPluginsSubmenu: "+submenuName);
        return menu;
    }

    String getSubmenuName(String jarPath) {
        //IJ.log("getSubmenuName: \n"+jarPath+"\n"+pluginsPath);
        if (jarPath.startsWith(pluginsPath)) {
            jarPath = jarPath.substring(pluginsPath.length() - 1);
        }
        int index = jarPath.lastIndexOf(File.separatorChar);
        if (index < 0) {
            return null;
        }
        String name = jarPath.substring(0, index);
        index = name.lastIndexOf(File.separatorChar);
        if (index < 0) {
            return null;
        }
        name = name.substring(index + 1);
        if (name.equals("plugins")) {
            return null;
        }
        return name;
    }

    void addItemSorted(JMenu menu, JMenuItem item, int startingIndex) {
        String itemLabel = item.getLabel();
        int count = menu.getItemCount();
        boolean inserted = false;
        for (int i = startingIndex; i < count; i++) {
            JMenuItem mi = menu.getItem(i);
            String label = mi.getLabel();
            //IJ.log(i+ "  "+itemLabel+"  "+label + "  "+(itemLabel.compareTo(label)));
            if (itemLabel.compareTo(label) < 0) {
                menu.insert(item, i);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            menu.add(item);
        }
    }

    void addSeparator(JMenu menu) {
        menu.addSeparator();
    }

// <editor-fold defaultstate="collapsed" desc=" Config File ">
    /** Opens the configuration file ("plugins.txt") from a JAR file and returns it as an InputStream. */
    InputStream getConfigurationFile(String jar) {
        try {
            ZipFile jarFile = new ZipFile(jar);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.getName().endsWith("plugins.config")) {
                    return jarFile.getInputStream(entry);
                }
            }
        } catch (Exception e) {
        }
        return autoGenerateConfigFile(jar);
    }

    /** Creates a configuration file for JAR/ZIP files that do not have one. */
    InputStream autoGenerateConfigFile(String jar) {
        StringBuffer sb = null;
        try {
            ZipFile jarFile = new ZipFile(jar);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && name.indexOf("_") > 0 && name.indexOf("$") == -1 && name.indexOf("/_") == -1 && !name.startsWith("_")) {
                    if (sb == null) {
                        sb = new StringBuffer();
                    }
                    String className = name.substring(0, name.length() - 6);
                    int slashIndex = className.lastIndexOf('/');
                    String plugins = "Plugins";
                    if (slashIndex >= 0) {
                        plugins += ">" + className.substring(0, slashIndex).replace('/', '>').replace('_', ' ');
                        name = className.substring(slashIndex + 1);
                    } else {
                        name = className;
                    }
                    name = name.replace('_', ' ');
                    className = className.replace('/', '.');
                    sb.append(plugins + ", \"" + name + "\", " + className + "\n");
                }
            }
        } catch (Exception e) {
        }
        //IJ.log(""+(sb!=null?sb.toString():"null"));
        if (sb == null) {
            return null;
        } else {
            return new ByteArrayInputStream(sb.toString().getBytes());
        }
    }

    // </editor-fold>
    /** Returns a list of the plugins with directory names removed. */
    String[] getStrippedPlugins(String[] plugins) {
        String[] plugins2 = new String[plugins.length];
        int slashPos;
        for (int i = 0; i < plugins2.length; i++) {
            plugins2[i] = plugins[i];
            slashPos = plugins2[i].lastIndexOf('/');
            if (slashPos >= 0) {
                plugins2[i] = plugins[i].substring(slashPos + 1, plugins2[i].length());
            }
        }
        return plugins2;
    }

    /** Looks for plugins and jar files in a subdirectory of the plugins directory. */
    static void checkSubdirectory(String path, String dir, Vector v) {
        if (dir.endsWith(".java")) {
            return;
        }
        File f = new File(path, dir);
        if (!f.isDirectory()) {
            return;
        }
        String[] list = f.list();
        if (list == null) {
            return;
        }
        dir += "/";
        int classCount = 0, otherCount = 0;
        String className = null;
        for (int i = 0; i < list.length; i++) {
            String name = list[i];
            boolean hasUnderscore = name.indexOf('_') >= 0;
            if (hasUnderscore && name.endsWith(".class") && name.indexOf('$') < 0) {
                name = name.substring(0, name.length() - 6); // remove ".class"
                v.addElement(dir + name);
                classCount++;
                className = name;
                //IJ.write("File: "+f+"/"+name);
            } else if (hasUnderscore && (name.endsWith(".jar") || name.endsWith(".zip"))) {
                if (jarFiles == null) {
                    jarFiles = new Vector();
                }
                jarFiles.addElement(f.getPath() + File.separator + name);
                otherCount++;
            } else if ((hasUnderscore && name.endsWith(".txt")) || name.endsWith(".ijm") || name.endsWith(".js")) {
                if (macroFiles == null) {
                    macroFiles = new Vector();
                }
                macroFiles.addElement(dir + name);
                otherCount++;
            }
        }
        if (Prefs.moveToMisc && classCount == 1 && otherCount == 0 && dir.indexOf("_") == -1) {
            v.remove(dir + className);
            v.addElement("Misc:" + dir + className);
        }
    }
    static String submenuName;
    static JMenu submenu;

    /** Installs a plugin in the Plugins menu using the class name,
    with underscores replaced by spaces, as the command. */
    void installUserPlugin(String className) {
        JMenu menu = pluginsMenu;
        int slashIndex = className.indexOf('/');
        String command = className;
        if (slashIndex > 0) {
            String dir = className.substring(0, slashIndex);
            if (Prefs.moveToMisc && dir.startsWith("Misc:")) {
                dir = "Miscellaneous";
                className = className.substring(5, className.length());
                slashIndex -= 5;
            }
            command = className.substring(slashIndex + 1, className.length());
            //className = className.replace('/', '.');
            if (submenu == null || !submenuName.equals(dir)) {
                submenuName = dir;
                submenu = new JMenu(submenuName);
                pluginsMenu.add(submenu);
                if (menusTable == null) {
                    menusTable = new Hashtable();
                }
                menusTable.put(submenuName, submenu);
            }
            menu = submenu;
            //IJ.write(dir + "  " + className);
        }
        command = command.replace('_', ' ');
        command.trim();
        if (pluginsTable.get(command) != null) // duplicate command?
        {
            command = command + " Plugin";
        }
        JMenuItem item = new JMenuItem(command);
        menu.add(item);
        item.addActionListener(ijApp);
        pluginsTable.put(command, className.replace('/', '.'));
        nPlugins++;
    }

    public void installPopupMenu(IjxApplication ijApp) {
        String s;
        int count = 0;
        JMenuItem mi;
        popup = new JPopupMenu("");
        if (fontSize != 0) {
            popup.setFont(getFont());
        }

        while (true) {
            count++;
            s = Prefs.getString("popup" + (count / 10) % 10 + count % 10);
            if (s == null) {
                break;
            }
            if (s.equals("-")) {
                popup.addSeparator();
            } else if (!s.equals("")) {
                mi = new JMenuItem(s);
                mi.addActionListener(ijApp);
                popup.add(mi);
            }
        }
    }

//    static boolean isColorLut(IjxImagePlus imp) {
//        ImageProcessor ip = imp.getProcessor();
//        IndexColorModel cm = (IndexColorModel) ip.getColorModel();
//        if (cm == null) {
//            return false;
//        }
//        int mapSize = cm.getMapSize();
//        byte[] reds = new byte[mapSize];
//        byte[] greens = new byte[mapSize];
//        byte[] blues = new byte[mapSize];
//        cm.getReds(reds);
//        cm.getGreens(greens);
//        cm.getBlues(blues);
//        boolean isColor = false;
//        for (int i = 0; i < mapSize; i++) {
//            if ((reds[i] != greens[i]) || (greens[i] != blues[i])) {
//                isColor = true;
//                break;
//            }
//        }
//        return isColor;
//    }

// <editor-fold defaultstate="collapsed" desc=" Macro Installation ">
    /** Installs macro files that are located in the plugins folder and have a "_" in their name. */
    void installMacros() {
        if (macroFiles == null) {
            return;
        }
        for (int i = 0; i < macroFiles.size(); i++) {
            String name = (String) macroFiles.elementAt(i);
            installMacro(name);
        }
    }

    /** Installs a macro in the Plugins menu, or submenu, with
    with underscores in the file name replaced by spaces. */
    void installMacro(String name) {
        JMenu menu = pluginsMenu;
        String dir = null;
        int slashIndex = name.indexOf('/');
        if (slashIndex > 0) {
            dir = name.substring(0, slashIndex);
            name = name.substring(slashIndex + 1, name.length());
            menu = getPluginsSubmenu(dir);
        }
        String command = name.replace('_', ' ');
        if (command.endsWith(".js")) {
            command = command.substring(0, command.length() - 3); //remove ".js"
        } else {
            command = command.substring(0, command.length() - 4); //remove ".txt" or ".ijm"
        }
        command.trim();
        if (pluginsTable.get(command) != null) // duplicate command?
        {
            command = command + " Macro";
        }
        JMenuItem item = new JMenuItem(command);
        addOrdered(menu, item);
        item.addActionListener(ijApp);
        String path = (dir != null ? dir + File.separator : "") + name;
        pluginsTable.put(command, "ijx.plugin.Macro_Runner(\"" + path + "\")");
        nMacros++;
    }

    public void installStartupMacroSet() {
        if (applet != null) {
            String docBase = "" + applet.getDocumentBase();
            if (!docBase.endsWith("/")) {
                int index = docBase.lastIndexOf("/");
                if (index != -1) {
                    docBase = docBase.substring(0, index + 1);
                }
            }
            IJ.runPlugIn("ijx.plugin.URLOpener", docBase + "StartupMacros.txt");
            return;
        }
        if (macrosPath == null) {
            (new MacroInstaller()).installFromIJJar("/macros/StartupMacros.txt");
            return;
        }
        String path = macrosPath + "StartupMacros.txt";
        File f = new File(path);
        if (!f.exists()) {
            path = macrosPath + "StartupMacros.ijm";
            f = new File(path);
            if (!f.exists()) {
                (new MacroInstaller()).installFromIJJar("/macros/StartupMacros.txt");
                return;
            }
        }
        String libraryPath = macrosPath + "Library.txt";
        f = new File(libraryPath);
        boolean isLibrary = f.exists();
        try {
            MacroInstaller mi = new MacroInstaller();
            if (isLibrary) {
                mi.installLibrary(libraryPath);
            }
            mi.installFile(path);
            nMacros += mi.getMacroCount();
        } catch (Exception e) {
        }
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Accessors ">
    public static JMenuBar getMenuBar() {
        return mbar;
    }

    public static JMenu getMacrosMenu() {
        return macrosMenu;
    }

    /** Returns the path to the user plugins directory or
    null if the plugins directory was not found. */
    public static String getPlugInsPath() {
        return pluginsPath;
    }

    /** Returns the path to the macros directory or
    null if the macros directory was not found. */
    public static String getMacrosPath() {
        return macrosPath;
    }

    /** Returns a list of the plugins in the plugins menu. */
    public static synchronized String[] getPlugins() {
        String homeDir = Prefs.getHomeDir();
        if (homeDir == null) {
            return null;
        }
        if (homeDir.endsWith("plugins")) {
            pluginsPath = homeDir + Prefs.separator;
        } else {
            String property = System.getProperty("plugins.dir");
            if (property != null && (property.endsWith("/") || property.endsWith("\\"))) {
                property = property.substring(0, property.length() - 1);
            }
            String pluginsDir = property;
            if (pluginsDir == null) {
                pluginsDir = homeDir;
            } else if (pluginsDir.equals("user.home")) {
                pluginsDir = System.getProperty("user.home");
                if (!(new File(pluginsDir + Prefs.separator + "plugins")).isDirectory()) {
                    pluginsDir = pluginsDir + Prefs.separator + "ImageJ";
                }
                property = null;
                // needed to run plugins when ImageJ launched using Java WebStart
                if (applet == null) {
                    System.setSecurityManager(null);
                }
                jnlp = true;
            }
            pluginsPath = pluginsDir + Prefs.separator + "plugins" + Prefs.separator;
            if (property != null && !(new File(pluginsPath)).isDirectory()) {
                pluginsPath = pluginsDir + Prefs.separator;
            }
            macrosPath = pluginsDir + Prefs.separator + "macros" + Prefs.separator;
        }
        File f = macrosPath != null ? new File(macrosPath) : null;
        if (f != null && !f.isDirectory()) {
            macrosPath = null;
        }
        f = pluginsPath != null ? new File(pluginsPath) : null;
        if (f == null || (f != null && !f.isDirectory())) {
            //error = "Plugins folder not found at "+pluginsPath;
            pluginsPath = null;
            return null;
        }
        String[] list = f.list();
        if (list == null) {
            return null;
        }
        Vector v = new Vector();
        jarFiles = null;
        macroFiles = null;
        for (int i = 0; i < list.length; i++) {
            String name = list[i];
            boolean isClassFile = name.endsWith(".class");
            boolean hasUnderscore = name.indexOf('_') >= 0;
            if (hasUnderscore && isClassFile && name.indexOf('$') < 0) {
                name = name.substring(0, name.length() - 6); // remove ".class"
                v.addElement(name);
            } else if (hasUnderscore && (name.endsWith(".jar") || name.endsWith(".zip"))) {
                if (jarFiles == null) {
                    jarFiles = new Vector();
                }
                jarFiles.addElement(pluginsPath + name);
            } else if ((hasUnderscore && name.endsWith(".txt")) || name.endsWith(".ijm") || name.endsWith(".js")) {
                if (macroFiles == null) {
                    macroFiles = new Vector();
                }
                macroFiles.addElement(name);
            } else {
                if (!isClassFile) {
                    checkSubdirectory(pluginsPath, name, v);
                }
            }
        }
        list = new String[v.size()];
        v.copyInto((String[]) list);
        StringSorter.sort(list);
        return list;
    }

    /** Returns the hashtable that associates commands with plugins. */
    public static Hashtable getCommands() {
        return pluginsTable;
    }

    /** Returns the hashtable that associates shortcuts with commands. The keys
    in the hashtable are Integer keycodes, or keycode+200 for uppercase. */
    public static Hashtable getShortcuts() {
        return shortcuts;
    }

    /** Returns the hashtable that associates keyboard shortcuts with macros. The keys
    in the hashtable are Integer keycodes, or keycode+200 for uppercase. */
    public static Hashtable getMacroShortcuts() {
        if (macroShortcuts == null) {
            macroShortcuts = new Hashtable();
        }
        return macroShortcuts;
    }

    public static JPopupMenu getPopupMenu() {
        return popup;
    }

    public static boolean commandInUse(String command) {
        if (pluginsTable.get(command) != null) {
            return true;
        } else {
            return false;
        }
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Public Install/Remove Plugin ">
    /** Adds a plugin based command to the end of a specified menu.
     * This is called by ij.plugin.Installer.
     * @param plugin			the plugin (e.g. "Inverter_", "Inverter_("arg")")
     * @param menuCode		PLUGINS_MENU, IMPORT_MENU, SAVE_AS_MENU or HOT_KEYS
     * @param command		the menu item label (set to "" to uninstall)
     * @param shortcut		the keyboard shortcut (e.g. "y", "Y", "F1")
     * @param ij				ImageJ (the action listener)
     *
     * @return				returns an error code(NORMAL_RETURN,COMMAND_IN_USE_ERROR, etc.)
     */

    public static int installPlugin(String plugin, char menuCode, String command, String shortcut, IjxApplication ij) {
        if (command.equals("")) { //uninstall
            //Object o = pluginsPrefs.remove(plugin);
            //if (o==null)
            //	return NOT_INSTALLED;
            //else
            return NORMAL_RETURN;
        }

        if (commandInUse(command)) {
            return COMMAND_IN_USE;
        }
        if (!validShortcut(shortcut)) {
            return INVALID_SHORTCUT;
        }
        if (shortcutInUse(shortcut)) {
            return SHORTCUT_IN_USE;
        }

        JMenu menu;
        switch (menuCode) {
            case PLUGINS_MENU:
                menu = pluginsMenu;
                break;
            case IMPORT_MENU:
                menu = importMenu;
                break;
            case SAVE_AS_MENU:
                menu = saveAsMenu;
                break;
            case SHORTCUTS_MENU:
                menu = shortcutsMenu;
                break;
            case ABOUT_MENU:
                menu = aboutMenu;
                break;
            case FILTERS_MENU:
                menu = filtersMenu;
                break;
            case TOOLS_MENU:
                menu = toolsMenu;
                break;
            case UTILITIES_MENU:
                menu = utilitiesMenu;
                break;
            default:
                return 0;
        }
        int code = convertShortcutToCode(shortcut);
        JMenuItem item;
        boolean functionKey = code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F12;
        if (code == 0) {
            item = new JMenuItem(command);
        } else if (functionKey) {
            command += " [F" + (code - KeyEvent.VK_F1 + 1) + "]";
            shortcuts.put(new Integer(code), command);
            item = new JMenuItem(command);
        } else {
            shortcuts.put(new Integer(code), command);
            int keyCode = code;
            boolean shift = false;
            if (keyCode >= 265 && keyCode <= 290) {
                keyCode -= 200;
                shift = true;
            }
            //item = new JMenuItem(command, new MenuShortcut(keyCode, shift));
            // ?? shift ???
            item = new JMenuItem(command);
            //, new MenuShortcut(shortcut, true));
            item.setAccelerator(KeyStroke.getKeyStroke(keyCode,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        }
        menu.add(item);
        item.addActionListener(ij);
        pluginsTable.put(command, plugin);
        shortcut = code > 0 && !functionKey ? "[" + shortcut + "]" : "";
        //IJ.write("installPlugin: "+menuCode+",\""+command+shortcut+"\","+plugin);
        pluginsPrefs.addElement(menuCode + ",\"" + command + shortcut + "\"," + plugin);
        return NORMAL_RETURN;
    }

    /** Deletes a command installed by installPlugin. */
    public static int uninstallPlugin(String command) {
        boolean found = false;
        for (Enumeration en = pluginsPrefs.elements(); en.hasMoreElements();) {
            String cmd = (String) en.nextElement();
            if (cmd.indexOf(command) > 0) {
                pluginsPrefs.removeElement((Object) cmd);
                found = true;
                break;
            }
        }
        if (found) {
            return NORMAL_RETURN;
        } else {
            return COMMAND_NOT_FOUND;
        }

    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Shortcut... ">
    public static int convertShortcutToCode(String shortcut) {
        int code = 0;
        int len = shortcut.length();
        if (len == 2 && shortcut.charAt(0) == 'F') {
            code = KeyEvent.VK_F1 + (int) shortcut.charAt(1) - 49;
            if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F9) {
                return code;
            } else {
                return 0;
            }
        }
        if (len == 3 && shortcut.charAt(0) == 'F') {
            code = KeyEvent.VK_F10 + (int) shortcut.charAt(2) - 48;
            if (code >= KeyEvent.VK_F10 && code <= KeyEvent.VK_F12) {
                return code;
            } else {
                return 0;
            }
        }
        if (len == 2 && shortcut.charAt(0) == 'N') { // numeric keypad
            code = KeyEvent.VK_NUMPAD0 + (int) shortcut.charAt(1) - 48;
            if (code >= KeyEvent.VK_NUMPAD0 && code <= KeyEvent.VK_NUMPAD9) {
                return code;
            }
            switch (shortcut.charAt(1)) {
                case '/':
                    return KeyEvent.VK_DIVIDE;
                case '*':
                    return KeyEvent.VK_MULTIPLY;
                case '-':
                    return KeyEvent.VK_SUBTRACT;
                case '+':
                    return KeyEvent.VK_ADD;
                case '.':
                    return KeyEvent.VK_DECIMAL;
                default:
                    return 0;
            }
        }
        if (len != 1) {
            return 0;
        }
        int c = (int) shortcut.charAt(0);
        if (c >= 65 && c <= 90) //A-Z
        {
            code = KeyEvent.VK_A + c - 65 + 200;
        } else if (c >= 97 && c <= 122) //a-z
        {
            code = KeyEvent.VK_A + c - 97;
        } else if (c >= 48 && c <= 57) //0-9
        {
            code = KeyEvent.VK_0 + c - 48;
        } else {
            switch (c) {
                case 43:
                    code = KeyEvent.VK_PLUS;
                    break;
                case 45:
                    code = KeyEvent.VK_MINUS;
                    break;
                //case 92: code = KeyEvent.VK_BACK_SLASH; break;
                default:
                    return 0;
            }
        }
        return code;
    }

    static boolean validShortcut(String shortcut) {
        int len = shortcut.length();
        if (shortcut.equals("")) {
            return true;
        } else if (len == 1) {
            return true;
        } else if (shortcut.startsWith("F") && (len == 2 || len == 3)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean shortcutInUse(String shortcut) {
        int code = convertShortcutToCode(shortcut);
        if (shortcuts.get(new Integer(code)) != null) {
            return true;
        } else {
            return false;
        }
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Font ">

    private static int defaultFontSize = IJ.isWindows() ? 14 : 0;
    private static int fontSize = Prefs.getInt(Prefs.MENU_SIZE, defaultFontSize);
    private static Font menuFont;
    
    /** Set the size (in points) used for the fonts in ImageJ menus.
    Set the size to 0 to use the Java default size. */
    public static void setFontSize(int size) {
        if (size < 9 && size != 0) {
            size = 9;
        }
        if (size > 24) {
            size = 24;
        }
        fontSize = size;
    }

    /** Returns the size (in points) used for the fonts in ImageJ menus. Returns
    0 if the default font size is being used or if this is a Macintosh. */
    public static int getFontSize() {
        return IJ.isMacintosh() ? 0 : fontSize;
    }

    public static Font getFont() {
        if (menuFont == null) {
            menuFont = new Font("SanSerif", Font.PLAIN, fontSize == 0 ? 12 : fontSize);
        }
        return menuFont;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Updating Menus ">
    /** Returns the hashtable that associates menu names with menus. */
    //public static Hashtable getMenus() {
    //	return menusTable;
    //}
    /** Inserts one item (a non-image window) into the Window menu. */
    static synchronized void insertWindowMenuItem(IjxWindow win) {
        if (ijApp == null || win == null) {
            return;
        }
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(win.getTitle());
        item.addItemListener(ijApp);
        int index = WINDOW_MENU_ITEMS + windowMenuItems2;
        if (windowMenuItems2 >= 2) {
            index--;
        }
        window.insert(item, index);
        windowMenuItems2++;
        if (windowMenuItems2 == 1) {
            window.insertSeparator(WINDOW_MENU_ITEMS + windowMenuItems2);
            windowMenuItems2++;
        }
        //IJ.write("insertWindowMenuItem: "+windowMenuItems2);
    }

    /** Adds one image to the end of the Window menu. */
    static synchronized void addWindowMenuItem(IjxImagePlus imp) {
        //IJ.log("addWindowMenuItem: "+imp);
        if (ijApp == null) {
            return;
        }
        String name = imp.getTitle();
        int size = (imp.getWidth() * imp.getHeight() * imp.getStackSize()) / 1024;
        switch (imp.getType()) {
            case IjxImagePlus.GRAY32:
            case IjxImagePlus.COLOR_RGB: // 32-bit
                size *= 4;
                break;
            case IjxImagePlus.GRAY16:  // 16-bit
                size *= 2;
                break;
            default: // 8-bit
                ;
        }
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name + " " + size + "K");
        window.add(item);
        item.addItemListener(ijApp);
    }

    /** Removes the specified item from the Window menu. */
    static synchronized void removeWindowMenuItem(int index) {
        //IJ.log("removeWindowMenuItem: "+index+" "+windowMenuItems2+" "+window.getItemCount());
        if (ijApp == null) {
            return;
        }
        try {
            if (index >= 0 && index < window.getItemCount()) {
                window.remove(WINDOW_MENU_ITEMS + index);
                if (index < windowMenuItems2) {
                    windowMenuItems2--;
                    if (windowMenuItems2 == 1) {
                        window.remove(WINDOW_MENU_ITEMS);
                        windowMenuItems2 = 0;
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /** Changes the name of an item in the Window menu. */
    public static synchronized void updateWindowMenuItem(String oldLabel, String newLabel) {
        if (oldLabel.equals(newLabel)) {
            return;
        }
        int first = WINDOW_MENU_ITEMS;
        int last = window.getItemCount() - 1;
        //IJ.write("updateWindowMenuItem: "+" "+first+" "+last+" "+oldLabel+" "+newLabel);
        try {  // workaround for Linux/Java 5.0/bug
            for (int i = first; i <= last; i++) {
                JMenuItem item = window.getItem(i);
                //IJ.write(i+" "+item.getLabel()+" "+newLabel);
                String label = item.getLabel();
                if (item != null && label.startsWith(oldLabel)) {
                    if (label.endsWith("K")) {
                        int index = label.lastIndexOf(' ');
                        if (index > -1) {
                            newLabel += label.substring(index, label.length());
                        }
                    }
                    item.setLabel(newLabel);
                    return;
                }
            }
        } catch (NullPointerException e) {
        }
    }

    /** Updates the Image/Type and Window menus. */
    public static void updateMenus() {
        if (ijApp == null) {
            return;
        }
        gray8Item.setState(false);
        gray16Item.setState(false);
        gray32Item.setState(false);
        color256Item.setState(false);
        colorRGBItem.setState(false);
        RGBStackItem.setState(false);
        HSBStackItem.setState(false);
        IjxImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            return;
        }
        int type = imp.getType();
        if (imp.getStackSize() > 1) {
            IjxImageStack stack = imp.getStack();
            if (stack.isRGB()) {
                type = RGB_STACK;
            } else if (stack.isHSB()) {
                type = HSB_STACK;
            }
        }
        if (type == IjxImagePlus.GRAY8) {
            ImageProcessor ip = imp.getProcessor();
            if (ip != null && ip.getMinThreshold() == ImageProcessor.NO_THRESHOLD && ip.isColorLut()) {
                type = IjxImagePlus.COLOR_256;
                if (!ip.isPseudoColorLut()) {
                    imp.setType(IjxImagePlus.COLOR_256);
                }
            }
        }
        switch (type) {
            case IjxImagePlus.GRAY8:
                gray8Item.setState(true);
                break;
            case IjxImagePlus.GRAY16:
                gray16Item.setState(true);
                break;
            case IjxImagePlus.GRAY32:
                gray32Item.setState(true);
                break;
            case IjxImagePlus.COLOR_256:
                color256Item.setState(true);
                break;
            case IjxImagePlus.COLOR_RGB:
                colorRGBItem.setState(true);
                break;
            case RGB_STACK:
                RGBStackItem.setState(true);
                break;
            case HSB_STACK:
                HSBStackItem.setState(true);
                break;
        }

        //update Window menu
        int nItems = window.getItemCount();
        int start = WINDOW_MENU_ITEMS + windowMenuItems2;
        int index = start + WindowManager.getCurrentIndex();
        try {  // workaround for Linux/Java 5.0/bug
            for (int i = start; i < nItems; i++) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) window.getItem(i);
                item.setState(i == index);
            }
        } catch (NullPointerException e) {
        }
    }

    public static void updateImageJMenus() {
        shortcuts = new Hashtable();
        pluginsPrefs = new Vector<String>();
        jarFiles = macroFiles = null;
        menusTable = null;
        MenusIjx m = new MenusIjx(IJ.getTopComponent(), IJ.getInstance(), IJ.getApplet());
        String err = m.addMenuBar();
        //m.installPopupMenu(IJ.getInstance());
        //m.installStartupMacroSet();
        if (err != null) {
            IJ.error(err);
        }
        IJ.setClassLoader(null);
        IJ.showStatus(m.nPlugins + " commands, " + m.nMacros + " macros");
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" OpenRecent Menu ">
    /** Adds a file path to the beginning of the File/Open Recent submenu. */
    public static synchronized void addOpenRecentItem(String path) {
        if (ijApp == null) {
            return;
        }
        int count = openRecentMenu.getItemCount();
        for (int i = 0; i < count;) {
            if (openRecentMenu.getItem(i).getLabel().equals(path)) {
                openRecentMenu.remove(i);
                count--;
            } else {
                i++;
            }
        }
        if (count == MAX_OPEN_RECENT_ITEMS) {
            openRecentMenu.remove(MAX_OPEN_RECENT_ITEMS - 1);
        }
        JMenuItem item = new JMenuItem(path);
        openRecentMenu.insert(item, 0);
        item.addActionListener(ijApp);
    }

    void addOpenRecentSubMenu(JMenu menu) {
        openRecentMenu = new JMenu("Open Recent");
        for (int i = 0; i < MAX_OPEN_RECENT_ITEMS; i++) {
            String path = Prefs.getString("recent" + (i / 10) % 10 + i % 10);
            if (path == null) {
                break;
            }
            JMenuItem item = new JMenuItem(path);
            openRecentMenu.add(item);
            item.addActionListener(ijApp);
        }
        menu.add(openRecentMenu);
    }

// </editor-fold>
    /** Called once when ImageJ quits. */
    public static void savePreferences(Properties prefs) {
        int index = 0;
        for (Enumeration en = pluginsPrefs.elements(); en.hasMoreElements();) {
            String key = "plugin" + (index / 10) % 10 + index % 10;
            String value = (String) en.nextElement();
            prefs.put(key, value);
            index++;
        }
        int n = openRecentMenu.getItemCount();
        for (int i = 0; i < n; i++) {
            String key = "" + i;
            if (key.length() == 1) {
                key = "0" + key;
            }
            key = "recent" + key;
            prefs.put(key, openRecentMenu.getItem(i).getLabel());
        }
        prefs.put(Prefs.MENU_SIZE, Integer.toString(fontSize));
    }
}
