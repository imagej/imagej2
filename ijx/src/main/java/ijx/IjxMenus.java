package ijx;

import ijx.IjxImagePlus;
import ijx.SavesPrefs;
import ijx.app.IjxApplication;
import ijx.gui.IjxWindow;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.PopupMenu;
import java.util.Hashtable;
import java.util.Properties;

/**
 *
 * @author GBH <imagejdev.org>
 */
public interface IjxMenus extends SavesPrefs {
    char ABOUT_MENU = 'a';
    int COMMAND_IN_USE = -1;
    int COMMAND_NOT_FOUND = -5;
    char FILTERS_MENU = 'f';
    int HSB_STACK = 11;
    char IMPORT_MENU = 'i';
    int INVALID_SHORTCUT = -2;
    int MAX_OPEN_RECENT_ITEMS = 15;
    int NORMAL_RETURN = 0;
    int NOT_INSTALLED = -4;
    char PLUGINS_MENU = 'p';
    int RGB_STACK = 10;
    char SAVE_AS_MENU = 's';
    char SHORTCUTS_MENU = 'h';
    int SHORTCUT_IN_USE = -3;
    char TOOLS_MENU = 't';
    char UTILITIES_MENU = 'u';
    int WINDOW_MENU_ITEMS = 5;

    String addMenuBar();

    /**
     * Adds a file path to the beginning of the File/Open Recent submenu.
     */
    void addOpenRecentItem(String path);

    public Object getOpenRecentMenu();

    public Object getWindowMenu();

    public int getWindowMenuItems2();

    boolean commandInUse(String command);

    int convertShortcutToCode(String shortcut);

    void forceInstallUserPlugin(String className);

    /**
     * Returns the hashtable that associates commands with plugins.
     */
    Hashtable getCommands();

    Font getFont();

    /**
     * Returns the size (in points) used for the fonts in ImageJ menus. Returns
     * 0 if the default font size is being used or if this is a Macintosh.
     */
    int getFontSize();

    String getJarFileForMenuEntry(String menuEntry);

    int getMacroCount();

    /**
     * Returns the hashtable that associates keyboard shortcuts with macros. The keys
     * in the hashtable are Integer keycodes, or keycode+200 for uppercase.
     */
    Hashtable getMacroShortcuts();

    /**
     * Returns the path to the macros directory or
     * null if the macros directory was not found.
     */
    String getMacrosPath();

    /**
     * Returns the path to the user plugins directory or
     * null if the plugins directory was not found.
     */
    String getPlugInsPath();

    int getPluginCount();

    /**
     * Returns a list of the plugins in the plugins menu.
     */
    String[] getPlugins();

    /**
     * Returns the hashtable that associates shortcuts with commands. The keys
     * in the hashtable are Integer keycodes, or keycode+200 for uppercase.
     */
    Hashtable getShortcuts();

    /**
     * Adds a plugin based command to the end of a specified menu.
     * @param plugin			the plugin (e.g. "Inverter_", "Inverter_("arg")")
     * @param menuCode		PLUGINS_MENU, IMPORT_MENU, SAVE_AS_MENU or HOT_KEYS
     * @param command		the menu item label (set to "" to uninstall)
     * @param shortcut		the keyboard shortcut (e.g. "y", "Y", "F1")
     * @param ij				ImageJ (the action listener)
     *
     * @return				returns an error code(NORMAL_RETURN,COMMAND_IN_USE_ERROR, etc.)
     */
    int installPlugin(String plugin, char menuCode, String command, String shortcut, IjxApplication ij);

    void installPopupMenu(IjxApplication ijApp);

    void installStartupMacroSet();

    /**
     * Called once when ImageJ quits.
     */
    void savePreferences(Properties prefs);

    /**
     * Set the size (in points) used for the fonts in ImageJ menus.
     * Set the size to 0 to use the Java default size.
     */
    void setFontSize(int size);

    boolean shortcutInUse(String shortcut);

    /**
     * Deletes a command installed by installPlugin.
     */
    int uninstallPlugin(String command);

    void updateImageJMenus();

    /**
     * Updates the Image/Type and Window menus.
     */
    void updateMenus();

    void addWindowMenuItem(IjxImagePlus imp);

    void insertWindowMenuItem(IjxWindow win);

    void removeWindowMenuItem(int index);

    void updateWindowMenuItem(String oldLabel, String newLabel);

//    MenuBar getMenuBar();
//    PopupMenu getPopupMenu();
//    Menu getSaveAsMenu();
//    Menu getMacrosMenu();
    Object getMenuBar();

    Object getPopupMenu();

    Object getSaveAsMenu();

    Object getMacrosMenu();
}
