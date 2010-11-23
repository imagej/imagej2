package ijx;

import ijx.CentralLookup;
import ijx.IjxMenus;
import ijx.app.IjxApplication;
import java.awt.Font;
import java.util.Hashtable;

/**
 *  This replaces the old ij.Menus...
 *
 *  Forwards calls to static methods from ij.menus to instance methods in MenusAWT or MenusSwing.
 * @author GBH <imagejdev.org>
 */
public class Menus {
//    static MenusAWT menusAWT = null;
//    static MenusSwing menusSwing = null;
    static IjxMenus menus = CentralLookup.getDefault().lookup(IjxMenus.class);

    public static void updateMenus() {
        menus.updateMenus();
    }

    public static void addOpenRecentItem(String path) {
        menus.addOpenRecentItem(path);
    }

    public static boolean commandInUse(String command) {
        return menus.commandInUse(command);
    }

    public static int convertShortcutToCode(String shortcut) {
        return menus.convertShortcutToCode(shortcut);
    }

    public static void forceInstallUserPlugin(String className) {
        menus.forceInstallUserPlugin(className);
    }

    public static Hashtable getCommands() {
        return menus.getCommands();
    }

    public static Font getFont() {
        return menus.getFont();
    }

    public static int getFontSize() {
        return menus.getFontSize();
    }

    public static String getJarFileForMenuEntry(String menuEntry) {
        return menus.getJarFileForMenuEntry(menuEntry);
    }

    public static Hashtable getMacroShortcuts() {
        return menus.getMacroShortcuts();
    }

    public static String getMacrosPath() {
        return menus.getMacrosPath();
    }

    public static String getPlugInsPath() {
        return menus.getPlugInsPath();
    }

    public static synchronized String[] getPlugins() {
        return menus.getPlugins();
    }

    public static Hashtable getShortcuts() {
        return menus.getShortcuts();
    }

    public static int installPlugin(String plugin, char menuCode, String command, String shortcut, IjxApplication ij) {
        return menus.installPlugin(plugin, menuCode, command, shortcut, ij);
    }

    public static void setFontSize(int size) {
        menus.setFontSize(size);
    }

    public static boolean shortcutInUse(String shortcut) {
        return menus.shortcutInUse(shortcut);
    }

    public static int uninstallPlugin(String command) {
        return menus.uninstallPlugin(command);
    }

    public static void updateImageJmenus() {
        menus.updateImageJMenus();
    }

    public static void updatemenus() {
        menus.updateMenus();
    }

    public static synchronized void updateWindowMenuItem(String oldLabel, String newLabel) {
        menus.updateWindowMenuItem(oldLabel, newLabel);
    }

    public static Object getOpenRecentMenu() {
        return menus.getOpenRecentMenu();
    }
    // ===================
//    public static Menu getMacrosMenu() {
//        if (menusAWT == null) {
//            return menusAWT.getMacrosMenu();
//        }
//    }
//    public static MenuBar getMenuBar() {
//        if (menusAWT == null) {
//            return menusAWT.getMenuBar();
//        }
//    }
//
//    public static PopupMenu getPopupMenu() {
//        if (menusAWT == null) {
//            return menusAWT.getPopupMenu();
//        }
//    }
//
//    public static Menu getSaveAsMenu() {
//        if (menusAWT == null) {
//            return menusAWT.getSaveAsMenu();
//        }  else {
//            return menusSwing.
//        }
//    }
    //====================
}
