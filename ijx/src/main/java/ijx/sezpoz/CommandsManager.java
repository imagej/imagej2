/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.sezpoz;

import ijx.plugin.api.PlugIn;
import ijx.action.AbstractActionExt;
import ijx.action.ActionContainerFactory;
import ijx.action.ActionFactoryIjx;
import ijx.action.ActionManager;
import ijx.action.BoundAction;
import ijx.options.Option;
import ijx.util.StringAlign;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.MenuElement;
import javax.swing.WindowConstants;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
//import org.pf.joi.Inspector;


/* Notes:
 *
 *
 *
 */
public class CommandsManager {
    //
    // The string commandKey is used internally to reference the action.
    
    Map<String, Action> commands = new HashMap<String, Action>();  // commandKey / action
    Map<String, String> menuCommands = new HashMap<String, String>();  // "menu>submenu" / commandKey
    Map<String, String> toolbarCommands = new HashMap<String, String>();  // toolbar / commandKey
    Map<String, IndexItem<ActionIjx, ?>> items = new HashMap<String, IndexItem<ActionIjx, ?>>();
    List<MenusOrdered> menues = new ArrayList<MenusOrdered>();

    public Action getAction(String commandKey) {
        return commands.get(commandKey);
    }

    static class MenusOrdered {
        int position;
        IndexItem<ActionIjx, ?> item;
        String commandKey;

        public MenusOrdered(int position, IndexItem<ActionIjx, ?> item, String commandKey) {
            this.position = position;
            this.item = item;
            this.commandKey = commandKey;
        }

        static class MenusOrderedPositionComparator implements Comparator<MenusOrdered> {
            public int compare(MenusOrdered o1, MenusOrdered o2) {
                return o1.position - o2.position;
            }
        }
        static class MenusOrderedComparator implements Comparator<MenusOrdered> {
            public int compare(MenusOrdered o1, MenusOrdered o2) {
                return o1.item.annotation().menu().compareToIgnoreCase(o2.item.annotation().menu());
            }
        }
    }

    /*
    static Map sortByValue(Map map) {
    List list = new LinkedList(map.entrySet());
    Collections.sort(list, new Comparator() {
    public int compare(Object o1, Object o2) {
    return ((Comparable) ((Map.Entry) (o1)).getValue())
    .compareTo(((Map.Entry) (o2)).getValue());
    }
    });
    // logger.info(list);
    Map result = new LinkedHashMap();
    for (Iterator it = list.iterator(); it.hasNext();) {
    Map.Entry entry = (Map.Entry)it.next();
    result.put(entry.getKey(), entry.getValue());
    }
    return result;
    }
     */
    public void loadAllItems() {
        for (final IndexItem<ActionIjx, ActionListener> item : Index.load(ActionIjx.class, ActionListener.class)) {
            String commandKey = null;
            if (!item.annotation().commandKey().isEmpty()) {
                commandKey = item.annotation().commandKey();
            } else {
                commandKey = item.annotation().label();
            }
            //AbstractActionExt action = createActionForActionListener(commandKey, item);
            AbstractActionExt action = ActionFactoryIjx.createActionExt(commandKey, item);
            addToMaps(commandKey, action, item);
        }
        Collections.sort(menues, new MenusOrdered.MenusOrderedPositionComparator());
        Collections.sort(menues, new MenusOrdered.MenusOrderedComparator());
    }

    public void loadOptions() {
        for (final IndexItem<Option, Object> item : Index.load(Option.class, Object.class)) {
            try {
                String clazz = item.className();
                System.out.println("clazz = " + clazz);
                String fieldName = item.memberName();
                System.out.println("fieldName = " + fieldName);
                System.out.println("item.element().getClass()" + item.element().getClass().getCanonicalName());
            } catch (InstantiationException ex) {
                Logger.getLogger(CommandsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
//    public void loadAllPlugins() {
//        for (final IndexItem<ActionIjx, PlugIn> item : Index.load(ActionIjx.class, PlugIn.class)) {
//            String commandKey = null;
//            if (!item.annotation().commandKey().isEmpty()) {
//                commandKey = item.annotation().commandKey();
//            } else {
//                commandKey = item.annotation().label();
//            }
//            Action action = createActionForPlugIn(commandKey, item);
//            addToMaps(commandKey, action, item);
//
//        }
//    }

    private void addToMaps(String commandKey, AbstractActionExt action, IndexItem<ActionIjx, ?> item) {
        commands.put(commandKey, action);
        items.put(commandKey, item);  // save for use in creating UI components
        if (!item.annotation().menu().isEmpty()) {
            menuCommands.put(item.annotation().menu(), commandKey);
            menues.add(new MenusOrdered(item.annotation().position(), item, commandKey));
        }
        if (!item.annotation().toolbar().isEmpty()) {
            toolbarCommands.put(item.annotation().toolbar(), commandKey);
        }
    }

//    public void loadImporters() {
//        for (final IndexItem<Importer, ij.plugin.PlugIn> item : Index.load(Importer.class, ij.plugin.PlugIn.class)) {
//            System.out.println("Importer Found: " + item.annotation().commandKey() + item.annotation().fileExts());
//        }
//    }
//    public void loadExtendedPluginFilters() {
//        for (final IndexItem<PluginFilterItem, ActionListener> item :
//                Index.load(PluginFilterItem.class, ActionListener.class)) {
//            String commandKey = item.annotation().commandKey();
//            Action action = createFilterAction(commandKey, item);
//            commands.put(commandKey, action);
//
//            System.out.println("Filter Found: " + item.annotation().commandKey());
//        }
//    }
//    public static Action createFilterAction(String commandKey,
//            final IndexItem<PluginFilterItem, ActionListener> item) {
//        System.out.println("creating action: " + commandKey);
//        Action action = new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    // class is instantiated when this is invoked
//                    String commandKey = item.annotation().commandKey();
//                    String label = item.annotation().label();
//                    String arg = item.annotation().argument();
//                    new PlugInFilterRunner(item.instance(), commandKey, arg);
//
//                } catch (InstantiationException x) {
//                    x.printStackTrace();
//                }
//            }
//        };
//        action.putValue(Action.ACTION_COMMAND_KEY, commandKey);
//        return null;
//    }
// <editor-fold defaultstate="collapsed" desc=" Action Creation ">
    public static AbstractActionExt createActionForActionListener(String commandKey, final IndexItem<ActionIjx, ActionListener> item) {
        System.out.println("creating action: " + commandKey);
        AbstractActionExt action = new AbstractActionExt() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // class is instantiated when this is invoked
                    item.instance().actionPerformed(e);
                } catch (InstantiationException x) {
                    x.printStackTrace();
                }
            }
        };
        return action;
    }

    public static AbstractActionExt createActionForPlugIn(String commandKey, final IndexItem<ActionIjx, PlugIn> item) {
        System.out.println("creating action (plugin): " + commandKey);
        String[] args = item.annotation().args();
        final String arg = args[0];
        AbstractActionExt action = new AbstractActionExt() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // class is instantiated when this is invoked
                    item.instance().run(arg);
                } catch (InstantiationException x) {
                    x.printStackTrace();
                }
            }
        };
        return action;
    }

    private void AddAllItemsToUI() {
        for (Entry<String, IndexItem<ActionIjx, ?>> e : items.entrySet()) {
            String commandKey = e.getKey();
            IndexItem<ActionIjx, ?> item = e.getValue();
            AddItemToUI(commandKey, item);
        }
    }

    private void AddItemToUI(String commandKey, IndexItem<ActionIjx, ?> item) {
        String menu = item.annotation().menu();
        String bundlePath = item.annotation().bundle(); // for i18n
        String toolbar = item.annotation().toolbar();
        int pos = item.annotation().position();
        boolean separator = item.annotation().separate();
        boolean toggle = item.annotation().toggle();
        String group = item.annotation().group();
        System.out.println(commandKey + " "
                + menu + " "
                + bundlePath + " "
                + toolbar + " "
                + pos + " "
                + separator + " "
                + toggle + " " + group);
    }
    //------------------------------------------------------------------------------
    //
    Map<String, LinkedHashMap> top = new LinkedHashMap<String, LinkedHashMap>();
    ActionManager manager = ActionManager.getInstance();
    ActionContainerFactory factory = new ActionContainerFactory(manager);
    public ArrayList topMenus = new ArrayList();

    ArrayList addTopLevelMenu(String label, String menuKey) {
        manager.addAction(new BoundAction(label, menuKey));
        ArrayList thisMenu = new ArrayList();
        thisMenu.add(menuKey);
        topMenus.add(thisMenu);
        return thisMenu;
    }

    ArrayList addSubMenu(String label, String menuKey, ArrayList parentMenu) {
        manager.addAction(new BoundAction(label, menuKey));
        ArrayList thisMenu = new ArrayList();
        thisMenu.add(menuKey);
        parentMenu.add(thisMenu);
        return thisMenu;
    }

    public ArrayList createMenuListNOT() {
            String currentTopMenu = "";
            for (Iterator<MenusOrdered> it = menues.iterator(); it.hasNext();) {
            MenusOrdered s = it.next();
            String menuName = s.item.annotation().menu();
            //String menukey = menuName.toLowerCase();

            String[] elements = menuName.split(">");
            String topMenu = elements[0];
            if(!currentTopMenu.equalsIgnoreCase(topMenu)){
                addTopLevelMenu(topMenu, topMenu);
                currentTopMenu = topMenu;
            }
//...
            ArrayList fileMenu = addTopLevelMenu("File", "fileMenu");
            fileMenu.add("save");
            fileMenu.add("print");
        }
        return topMenus;

    }
//    ArrayList findOrCreateTopLevelMenu(String menuName) {
//        topMenus.get(i)
//
//        return menu;
//    }
//
//    JMenu findOrCreateSubMenu(String menuName) {
//        // MenuElement[] menus = bar.getSubElements();
//        JMenu menu = topMenus.get(menuName);
//        if (menu == null) {
//            menu = new JMenu(menuName);
//            topMenus.put(menuName, menu);
//        }
//        return menu;
//    }


    public ArrayList createMenuList() {
//        top.put("File", new LinkedHashMap<String, LinkedHashMap>());
//        top.put("Edit", new LinkedHashMap<String, LinkedHashMap>());
//        top.put("Image", new LinkedHashMap<String, LinkedHashMap>());
//        top.put("Process", new LinkedHashMap<String, LinkedHashMap>());
//        top.put("Analyze", new LinkedHashMap<String, LinkedHashMap>());
//        top.put("Plugins", new LinkedHashMap<String, LinkedHashMap>());
//        top.put("Window", new LinkedHashMap<String, LinkedHashMap>());
//        top.put("Help", new LinkedHashMap<String, LinkedHashMap>());

        for (Iterator<MenusOrdered> it = menues.iterator(); it.hasNext();) {
            MenusOrdered s = it.next();
            String menuName = s.item.annotation().menu();
            //String menukey = menuName.toLowerCase();
            String[] elements = menuName.split(">");
            String topMenu = elements[0];

            if (!top.containsKey(topMenu)) {
                top.put(topMenu, new LinkedHashMap<String, LinkedHashMap>());
                manager.addAction(new BoundAction(topMenu, topMenu));
            }
            Map<String, LinkedHashMap> menuToAddTo = top;

            for (int j = 1; j < elements.length; j++) {
                System.out.print(elements[j] + ", ");
                if (!menuToAddTo.containsKey(elements[j])) {
                    LinkedHashMap<String, LinkedHashMap> sub = new LinkedHashMap<String, LinkedHashMap>();
                    menuToAddTo.put(elements[j], sub);
                    menuToAddTo = sub;
                }
            }
            menuToAddTo.put(s.item.annotation().label(), null); //menuName, new LinkedHashMap<String, LinkedHashMap>().put(labels[i], null));
        }
        System.out.println("\nDone");

        Set st = top.keySet();
//        Iterator topEntryIter = top.entrySet().iterator();
//        while (topEntryIter.hasNext()) {
//            Map.Entry entry = (Map.Entry) topEntryIter.next();
//            if(entry.getValue() == null) System.out.print("        ");
//            System.out.println(entry.getKey() + " -- " + entry.getValue());
//        }

//        System.out.println("Set created from LinkedHashMap Keys contains :");
//        //iterate through the Set of keys
        Iterator itr = st.iterator();
        while (itr.hasNext()) {

            System.out.println(itr.next());
        }
        ArrayList al = new ArrayList(st);
        return al;
//        ArrayList l = new ArrayList(top);
//        List l = new ArrayList(set);
//        for (Iterator it = l.iterator(); it.hasNext();) {
//            Object o = it.next();
//            System.out.println("" + o);
//        } // [1, 2, 3]




        /*
        for (Iterator<MenusOrdered> it = menues.iterator(); it.hasNext();) {
        MenusOrdered s = it.next();
        String menuName = s.item.annotation().menu();
        if (menuName.contains(">")) {
        // is a sub-menu
        String patternStr = ">";
        String[] fields = menuName.split(patternStr);
        System.out.println(Arrays.toString(fields));


        //topMenu.getSubElements()

        } else {
        // is a top level menu
        System.out.println("topLevel: " + menuName + " - " + s.item.annotation().label());
        // find/create top level menu
        //topMenu = findOrCreateTopLevelMenu(menuName);

        }
        //menuStr.split(">");
        System.out.println(s.position + " - " + s.commandKey);


        }
         *
         */
        //MenuBuilder mBuilder = ij.IJ.getFactory().newMenuBuilder(commands, menuCommands, toolbarCommands, items);
        /* MenuBuilder parameters:
        Map<String, Action> commands,
        Map<String, String> menuCommands,
        Map<String, String> toolbarCommands,
        Map<String, IndexItem<ActionIjx, ?>> items);
         */
        //mBuilder.build();
        //return new ArrayList();
    }

    public void loadResources(final IndexItem<ActionIjx, ActionListener> item) {
        String bundle = item.annotation().bundle();
        ResourceBundle res = ResourceBundle.getBundle(bundle, Locale.getDefault());
        String label = res.getString("LABEL");
    }

// Utils -------------------------------------------------------------------------------------
    private void listIndexedItems() {
        System.out.println("Indexed Items:");
        System.out.println(" " + strLeft(22, "commandKey")
                + strLeft(15, "menu")
                + strLeft(12, "bundlePath")
                + strLeft(12, "toolbar")
                + strLeft(12, "pos")
                + strLeft(8, "separator")
                + strLeft(12, "checkBox")
                + strLeft(12, "group"));

        for (Entry<String, IndexItem<ActionIjx, ?>> e : items.entrySet()) {
            String commandKey = e.getKey();
            IndexItem<ActionIjx, ?> item = e.getValue();
            System.out.println(indexedItemToString(commandKey, item));
        }
    }
    private void listMenuesArray() {
        System.out.println("Indexed Items:");
        System.out.println(" " + strLeft(22, "commandKey")
                + strLeft(15, "menu")
                + strLeft(12, "bundlePath")
                + strLeft(12, "toolbar")
                + strLeft(12, "pos")
                + strLeft(8, "separator")
                + strLeft(12, "checkBox")
                + strLeft(12, "group"));
        for (MenusOrdered menusOrdered : menues) {
            System.out.println(indexedItemToString( menusOrdered.commandKey,menusOrdered.item));
        }
    }

    private String indexedItemToString(String commandKey, IndexItem<ActionIjx, ?> item) {
        String menu = item.annotation().menu();
        String bundlePath = item.annotation().bundle(); // for i18n
        String toolbar = item.annotation().toolbar();
        int pos = item.annotation().position();
        boolean separator = item.annotation().separate();
        boolean toggle = item.annotation().toggle();
        String group = item.annotation().group();
//        String itemStr = commandKey + ", "
//                + menu + ", "
//                + bundlePath + ", "
//                + toolbar + ", "
//                + pos + ", "
//                + separator + ", "
//                + checkBox + ", " + group + ".";
        String itemStr = " " + strLeft(22, commandKey)
                + strLeft(15, menu)
                + strLeft(12, bundlePath)
                + strLeft(12, toolbar)
                + strLeft(12, "" + pos)
                + strLeft(7, "" + separator)
                + strLeft(12, "" + toggle)
                + strLeft(12, group);
        return itemStr;
    }

    String strLeft(int w, String s) {
        return new StringAlign(w, StringAlign.JUST_LEFT).format(s);
    }

    void createTestFrame(JMenuBar bar) {
        Logger logger = Logger.getLogger("net.java.sezpoz");
        logger.setLevel(Level.ALL);
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        JFrame f = new JFrame("Demo");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setJMenuBar(bar);
        f.add(new JLabel("ActionMenuLoadingDemo"));
        f.pack();
        f.setVisible(true);
    }

    public static void main(String[] args) {
        CommandsManager cmdMgr = new CommandsManager();
        cmdMgr.loadAllItems();
        //cmdMgr.listIndexedItems();
        cmdMgr.listMenuesArray();
        ArrayList menuList = cmdMgr.createMenuList();
        JMenuBar menuBar = ActionFactoryIjx.createMenuBar(menuList);
        JMenu windowMenu = menuBar.getMenu(6);
        JMenu fileMenu = menuBar.getMenu(0);
        JFrame f = new JFrame();
        f.setJMenuBar(menuBar);
        f.setPreferredSize(new Dimension(400, 200));
        f.pack();
        f.setVisible(true);

        // cl.loadImporters();

        //cl.AddAllItemsToUI();

        //Inspector.inspectWait(cl);

//        System.out.println("Invoking action: radioA");
//        Action a = cl.getAction("radioA");
//        a.actionPerformed(null);

    }
}
