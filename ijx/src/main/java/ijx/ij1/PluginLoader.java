package ijx.ij1;

import ijx.*;
import ijx.plugin.api.PlugIn;
import ijx.text.TextWindow;
import ijx.util.StringAlign;
import ijx.util.StringSorter;
import imagej.ij1bridge.plugin.Ij1PluginFinder;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginFinder;

import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.util.zip.*;

import java.net.URL;
import java.net.JarURLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/* 
 * PluginLoader for discovering legacy ImageJ User Pugins
 * - GBH, Dec 2010
 */
public class PluginLoader implements PlugIn, PluginFinder {

    @Override
    public void run(String arg) {
        System.out.println("Running PluginLoader...");
        this.loadPlugins();
    }

    @Override
    public void findPlugins(List<PluginEntry> plugins) {
        // not used yet... just testing so far.
    }
    //
    private String pluginsPath;
    // Plugins --------------------------
    private int nPlugins;
    private Hashtable pluginsTable;
    private Hashtable shortcuts = new Hashtable();
    private Vector pluginsPrefs = new Vector(); // commands saved in IJ_Prefs
    //
    private static Vector jarFiles;  // .jar and .zip files in plugins folder with "_" in their name
    private Map menuEntry2jarFile = new HashMap();
    //
    private static boolean installingJars, duplicateCommand;
    private String error;
    private String jarError;
    private String pluginError;
    private boolean isJarErrorHeading;
    //
    ArrayList<PluginEntry> plugins = new ArrayList<PluginEntry>();
    //

    public PluginLoader() {
    }

    public void loadPlugins() {
        error = null;
        pluginsTable = new Hashtable();
        shortcuts = new Hashtable();
        pluginsPrefs = new Vector();
        setupPluginsPaths();
        System.out.println("PluginsPath: " + getPlugInsPath());
        // the plugins will be added later, with a separator
        //addPluginsMenu();
        // =================================
        String[] pluginList = getPluginsList();
        // =================================
        String[] pluginsList2 = null;
        Hashtable skipList = new Hashtable();

        if (pluginList != null) {
            for (int i = 0; i < pluginList.length; i++) {
                if (!skipList.containsKey(pluginList[i])) {
                    installUserPlugin(pluginList[i]);
                }
            }
        }
        installJarPlugins();
        //
        // diag...
        //Hashtable commands = this.getCommands();

        // List out the PlugEntries in a TestWindow
		StringBuffer sb = new StringBuffer();
                for (PluginEntry plugin : plugins) {
            sb.append(pluginEntryToString(plugin));
			sb.append("\n");
        }
		TextWindow tw = new TextWindow("PluginEntries", "", sb.toString(), 600, 500);
	}

    void setupPluginsPaths() {
        pluginsPath = null;
        String homeDir = Prefs.getHomeDir();
        if (homeDir == null) {
            return;
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
            }
            pluginsPath = pluginsDir + Prefs.separator + "plugins" + Prefs.separator;
            if (property != null && !(new File(pluginsPath)).isDirectory()) {
                pluginsPath = pluginsDir + Prefs.separator;
            }
        }
    }

    private synchronized String[] getPluginsList() {
        File f = pluginsPath != null ? new File(pluginsPath) : null;
        if (f == null || (f != null && !f.isDirectory())) {
            return null;
        }
        String[] fileList = f.list();
        if (fileList == null) {
            return null;
        }
        Vector v = new Vector();
        jarFiles = null;
        for (int i = 0; i < fileList.length; i++) {
            String name = fileList[i];
            boolean isClassFile = name.endsWith(".class");
            boolean hasUnderscore = name.indexOf('_') >= 0;
            //==============
            System.out.println("getPluginsList, file: " + name);
            //===============
            if (hasUnderscore && isClassFile && name.indexOf('$') < 0) {
                // Deal with $ in classname...
                name = name.substring(0, name.length() - 6); // remove ".class"
                v.addElement(name);
            } else if (hasUnderscore && (name.endsWith(".jar") || name.endsWith(".zip"))) {
                if (jarFiles == null) {
                    jarFiles = new Vector();
                }
                jarFiles.addElement(pluginsPath + name);
            } else {
                if (!isClassFile) {
                    checkSubdirectory(pluginsPath, name, v);
                }
            }
        }
        String[] pluginsList = new String[v.size()];
        v.copyInto((String[]) pluginsList);
        StringSorter.sort(pluginsList);
        return pluginsList;
    }

    /** Looks for plugins and jar files in a subdirectory of the plugins directory. */
    private void checkSubdirectory(String path, String dir, Vector v) {
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
            } else {
            }
        }
        if (Prefs.moveToMisc && classCount == 1 && otherCount == 0 && dir.indexOf("_") == -1) {
            v.setElementAt("Miscellaneous/" + className,
                    v.size() - 1);
        }
    }

    private void installUserPlugin(String className) {
        System.out.println("installUserPlugin: " + className);
        int slashIndex = className.indexOf('/');
        String menuName = slashIndex < 0
                ? "Plugins"
                : "Plugins>" + className.substring(0, slashIndex).replace('/', '>');
        //Menu menu = getMenu(menuName);
        String command = className;
        if (slashIndex > 0) {
            command = className.substring(slashIndex + 1);
        }
        command = command.replace('_', ' ');
        command.trim();
//    boolean itemExists = (pluginsTable.get(command) != null);
//    if (force && itemExists) {
//      return;
//    }
//    if (!force && itemExists) { // duplicate command?
//       command = command + " Plugin";
//    }
//    MenuItem item = new MenuItem(command);
//    if (force) {
//      addItemSorted(menu, item, 0);
//    } else {
//      addOrdered(menu, item);
//    }
//    item.addActionListener(ijApp);
        pluginsTable.put(command, className.replace('/', '.'));
        nPlugins++;
        System.out.print(
                ">>>   command: " + command
                + "   menu:    " + menuName
                + "" + "");
        System.out.println("");
        final PluginEntry pluginEntry = new PluginEntry(className, menuName, command);
        this.plugins.add(pluginEntry);
    }

//    void addPlugInItem(String label, String className, int shortcut, boolean shift) {
//        pluginsTable.put(label, className);
//        nPlugins++;
//    }
    void checkForDuplicate(String command) {
        if (pluginsTable.get(command) != null) {
        }
    }

    /** Install plugins located in JAR files. */
    void installJarPlugins() {
        if (jarFiles == null) {
            return;
        }
        Collections.sort(jarFiles);
        installingJars = true;
        // for each jar file...
        for (int i = 0; i < jarFiles.size(); i++) {
            isJarErrorHeading = false;
            String jar = (String) jarFiles.elementAt(i);
            InputStream is = getConfigurationFile(jar);
            if (is == null) {
                continue;
            }
            ArrayList entries = new ArrayList();
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));
            try {
                while (true) {
                    String s = lnr.readLine();
                    if (s == null) {
                        break;
                    }
                    if (s.length() >= 3 && !s.startsWith("#")) {
                        entries.add(s);
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
            // now install the entries
            for (Object entry : entries) {
                installJarPlugin(jar, (String) entry);
            }
        }
    }

    /** Install a plugin located in a JAR file. */
    void installJarPlugin(String jar, String s) {
        System.out.println("installJarPlugin: " + jar + ", " + s);
        String menu = "";
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
                menu = "Plugins";
            }
            //addSorted = true;
        } else {
            int firstQuote = s.indexOf('"');
            String name = firstQuote < 0 ? s
                    : s.substring(0, firstQuote).trim();
            int comma = name.indexOf(',');
            if (comma >= 0) {
                name = name.substring(0, comma);
            }
            if (name.startsWith("Help>About")) // for backward compatibility
            {
                name = "Help>About Plugins";
            }
            menu = name;
        }
        int firstQuote = s.indexOf('"');
        if (firstQuote == -1) {
            return;
        }
        s = s.substring(firstQuote, s.length()); // remove menu
        if (menu != null) {
            // addPluginSeparatorIfNeeded(menu);
            addPluginItem(menu, s);
            // addPluginItem(menu, s); ???
            // addSorted = false;
        }
        String menuEntry = s;
        if (s.startsWith("\"")) {
            int quote = s.indexOf('"', 1);
            menuEntry = quote < 0 ? s.substring(1) : s.substring(1, quote);
        } else {
            int comma = s.indexOf(',');
            if (comma > 0) {
                menuEntry = s.substring(0, comma);
            }
        }
        if (duplicateCommand) {
            if (jarError == null) {
                jarError = "";
            }
            addJarErrorHeading(jar);
            String jar2 = (String) menuEntry2jarFile.get(menuEntry);
            if (jar2 != null && jar2.startsWith(pluginsPath)) {
                jar2 = jar2.substring(pluginsPath.length());
            }
            jarError += "    Duplicate command: " + s
                    + (jar2 != null ? " (already in " + jar2 + ")"
                    : "") + "\n";
        } else {
            menuEntry2jarFile.put(menuEntry, jar);
        }
        duplicateCommand = false;
    }

    void addPluginItem(String menu, String s) {
        System.out.println("addPluginItem: " + menu + " | " + s);
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
        //addItem(submenu, command, keyCode, shift);
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
        final PluginEntry pluginEntry = new PluginEntry(className, menu, command);
        this.plugins.add(pluginEntry);
    }

    String getPluginsSubmenu(String submenuName) {
        return "Plugins>" + submenuName;
    }

    String getSubmenuName(String jarPath) {
        //IJ.log("getSubmenuName: \n"+jarPath+"\n"+pluginsPath);
        if (pluginsPath == null) {
            return null;
        }
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

    void addJarErrorHeading(String jar) {
        if (!isJarErrorHeading) {
            if (!jarError.equals("")) {
                jarError += " \n";
            }
            jarError += "Plugin configuration error: " + jar + "\n";
            isJarErrorHeading = true;
        }
    }

    /** Opens the configuration file ("plugins.config") from a JAR file and returns it as an InputStream. */
    InputStream getConfigurationFile(String jar) {
        try {
            JarFile jf;
            // in case its a regular file
            if (jar.startsWith("http") || jar.startsWith("file:")) {
                URL url = new URL("jar:" + jar + "!/");
                JarURLConnection jarcon =
                        (JarURLConnection) url.openConnection();
                jf = jarcon.getJarFile();
            } else {
                jf = new JarFile(jar);
            }
            Enumeration entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (entry.getName().endsWith("plugins.config")) {
                    return jf.getInputStream(entry);
                }
            }
        } catch (Throwable e) {
            IJ.log(jar + ": " + e);
        }
        return autoGenerateConfigFile(jar);
    }

    /** Creates a configuration file for JAR/ZIP files that do not have one. */
    InputStream autoGenerateConfigFile(String jar) {
        if (jar.startsWith("file:")) {
            jar = jar.substring(5);
        }
        StringBuffer sb = null;
        try {
            ZipFile jarFile = new ZipFile(jar);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && name.indexOf("_") > 0 && name.indexOf("$") == -1
                        && name.indexOf("/_") == -1 && !name.startsWith("_")) {
                    if (Character.isLowerCase(name.charAt(0)) && name.indexOf("/") != -1) {
                        continue;
                    }
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
                    //if (className.indexOf(".")==-1 || Character.isUpperCase(className.charAt(0)))
                    sb.append(plugins + ", \"" + name + "\", " + className + "\n");
                }
            }
        } catch (Throwable e) {
            IJ.log(jar + ": " + e);
        }
        //IJ.log(""+(sb!=null?sb.toString():"null"));
        if (sb == null) {
            return null;
        } else {
            return new ByteArrayInputStream(sb.toString().getBytes());
        }
    }

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

    public int getPluginCount() {
        return nPlugins;
    }

    /** Returns the path to the user plugins directory or
    null if the plugins directory was not found. */
    public String getPlugInsPath() {
        return pluginsPath;
    }

    /** Returns the hashtable that associates commands with plugins. */
    public Hashtable getCommands() {
        return pluginsTable;
    }

    /** Returns the hashtable that associates shortcuts with commands. The keys
    in the hashtable are Integer keycodes, or keycode+200 for uppercase. */
    public Hashtable getShortcuts() {
        return shortcuts;
    }

    public int convertShortcutToCode(String shortcut) {
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

    public String pluginEntryToString(PluginEntry entry) {
        String itemStr = " "
                + strLeft(28, entry.getParentMenu())
                + strLeft(35, entry.getLabel())
                + strLeft(50, entry.getPluginClass());
        return itemStr;
    }

    String strLeft(int w, String s) {
        return new StringAlign(w, StringAlign.JUST_LEFT).format(s);
    }
}
