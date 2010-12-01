package ijx.implementation.swing;

import ijx.gui.menu.MenuBuilder;
import ijx.sezpoz.ActionIjx;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.MenuElement;
import net.java.sezpoz.IndexItem;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class MenuBuilderSwing implements MenuBuilder {
    //
    private Map<String, Action> commands; // commandKey / action
    private Map<String, String> menuCommands; // "menu>submenu" / commandKey
    private Map<String, String> toolbarCommands; // toolbar / commandKey
    private Map<String, IndexItem<ActionIjx, ?>> items;
    //
    private Map<String, JMenu> topMenus = new HashMap<String, JMenu>();
    private JMenuBar bar = new JMenuBar();
    //
    private Map<String, ButtonGroup> groups = new HashMap<String, ButtonGroup>();


    public MenuBuilderSwing(
            Map<String, Action> commands,
            Map<String, String> menuCommands,
            Map<String, String> toolbarCommands,
            Map<String, IndexItem<ActionIjx, ?>> items) {

        this.commands = commands;
        this.menuCommands = menuCommands;
        this.toolbarCommands = toolbarCommands;
        this.items = items;
    }

    public void build() {
        createMenuTree();
        Iterator it = topMenus.entrySet().iterator();
        for (Entry<String, JMenu> e : topMenus.entrySet()) {
            bar.add(e.getValue());
        }
    }

    public void createMenuTree() {
        for (Entry<String, String> e : menuCommands.entrySet()) {
            String commandKey = e.getValue();
            String menuName = e.getKey();
            // type of MenuItem to create
            IndexItem<ActionIjx, ?> item = items.get(commandKey);
            //
            JMenuItem menuItem;
            if (!item.annotation().group().isEmpty()) { // is RadioButton
                menuItem = new JRadioButtonMenuItem(commands.get(commandKey));
            } else if (!item.annotation().toggle()) { // is CheckBox
                menuItem = new JCheckBoxMenuItem(commands.get(commandKey));
            } else { // is normal menu item
                menuItem = new JMenuItem(commands.get(commandKey));
            }
            // @todo
            JMenu menu;
            findOrCreateMenu(menuName);
        }
    }

    enum MenuType {
        normal, check, radio
    }

    /*
    Convert HashMap to Hashtable
    Hashtable ht = new Hashtable();
    ht.putAll(items);

    // Search forward from first visible row looking for any visible node
    // whose name starts with prefix.
    int startRow = 0;
    String prefix = "b";
    TreePath path = tree.getNextMatch(prefix, startRow, Position.Bias.Forward);
    System.out.println(path);
     */
    private void findOrCreateMenu(String menuName) {
        JMenu topMenu;
        if (menuName.contains(">")) {
            // is a sub-menu
            String patternStr = ">";

            String[] fields = menuName.split(patternStr);
            System.out.println(Arrays.toString(fields));

            topMenu = findOrCreateTopLevelMenu(fields[0]);
            //topMenu.getSubElements()

        } else {
            // is a top level menu
            System.out.println(menuName);
            // find/create top level menu
            topMenu = findOrCreateTopLevelMenu(menuName);

        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void findOrCreateMenu(String[] menus) {
    }

    JMenu findOrCreateTopLevelMenu(String menuName) {
        // MenuElement[] menus = bar.getSubElements();
        JMenu menu = topMenus.get(menuName);
        if (menu == null) {
            menu = new JMenu(menuName);
            topMenus.put(menuName, menu);
        }
        return menu;
    }

    JMenu findOrCreateSubMenu(String menuName) {
        // MenuElement[] menus = bar.getSubElements();
        JMenu menu = topMenus.get(menuName);
        if (menu == null) {
            menu = new JMenu(menuName);
            topMenus.put(menuName, menu);
        }
        return menu;
    }

    JMenuBar createMenu() {
        JMenuBar bar = new JMenuBar();
        Iterator it = items.entrySet().iterator();
        for (Entry<String, Action> e : commands.entrySet()) {
            String menuName = e.getKey();
            Action action = e.getValue();
            // parse the menuName topMenu>subMenu>subsubMenu
            // JMenu menu = findOrCreateMenu(menuName);
            //equiv: JMenuItem menuItem = new JMenuItem(action); menu.add(menuItem);

        }
        // create Top Level Menu
        //bar.add(menu);
        return bar;
    }

    public Container getMenubar() {
        return bar;
    }

    private void createItem(IndexItem<ActionIjx, ActionListener> item) {
        if (!item.annotation().group().isEmpty()) {
            createRadioButtonItem(item);
        }
    }
// <editor-fold defaultstate="collapsed" desc=" RadioButtonMenuItems ">
    Map<String, ButtonGroup> buttonGroups = new HashMap<String, ButtonGroup>();

    public void selectRadioButton(JRadioButton b, ButtonGroup group) {
        // Select the radio button; the currently selected radio button is deselected.
        // This operation does not cause any action events to be fired.
        // Better to use Action.SELECTED_KEY, but only in JDK 6.
        ButtonModel model = b.getModel();
        group.setSelected(model, true);
    }

    private void createRadioButtonItem(IndexItem<ActionIjx, ActionListener> item) {
        String radioButtonGroup = item.annotation().group();
        // this is a radioButton item
        if (!item.annotation().menu().isEmpty()) {
            createRadioButtonMenuItem(item);
        }
        if (!item.annotation().toolbar().isEmpty()) {
            createRadioButtonOnToolbar(item);
        }
        //     JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem(getAction(action);
        // does group already exist?  If not, create.
        if (buttonGroups.get(radioButtonGroup) == null) {
            buttonGroups.put(radioButtonGroup, new ButtonGroup());
        }
        //     buttonGroups.get(radioButtonGroup).add(radioItem);    }
    }

    private void createRadioButtonMenuItem(IndexItem<ActionIjx, ActionListener> item) {
        String menuName = item.annotation().menu();
        //...
    }

    private void createRadioButtonOnToolbar(IndexItem<ActionIjx, ActionListener> item) {
        String toolbarName = item.annotation().toolbar();
        //...
    }
// </editor-fold>

    public void addItem(String menuName, Action action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addSubItem(String menuName, String subMenuName, Action action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeItem(String menuName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTopMenu(String topMenu) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
