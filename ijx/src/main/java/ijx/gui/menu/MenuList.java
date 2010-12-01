package ijx.gui.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class MenuList {
    static String[] menuNames = {"file", "file>sub1", "file>sub2", "file>sub1>",
        "edit", "edit>sub1", "edit>sub2", "edit>sub1>", "edit"
    };
    static String[] labels = {"Open", "ItemFile sub1_A", "ItemFile sub2", "ItemFile sub1_B",
        "Copy", "Item Edit sub1_A", "Item Edit sub2", "Item Edit sub1_B>", "Paste"
    };
    static Map<String, LinkedHashMap> top = new LinkedHashMap<String, LinkedHashMap>();

    public static void main(String[] args) {
    }

    static void testHashMap() {
        for (int i = 0; i < menuNames.length; i++) {
            String menuName = menuNames[i].toLowerCase();
            String[] elements = menuName.split(">");
            String topMenu = elements[0];

            if (!top.containsKey(topMenu)) {
                top.put(topMenu, new LinkedHashMap<String, LinkedHashMap>());
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
            menuToAddTo.put(labels[i], null); //menuName, new LinkedHashMap<String, LinkedHashMap>().put(labels[i], null));
        }
        System.out.println("Done");

        Set st = top.keySet();

        System.out.println("Set created from LinkedHashMap Keys contains :");
        //iterate through the Set of keys
        Iterator itr = st.iterator();
        while (itr.hasNext()) {
            System.out.println(itr.next());
        }
//        ArrayList l = new ArrayList(top);
//        List l = new ArrayList(set);
//        for (Iterator it = l.iterator(); it.hasNext();) {
//            Object o = it.next();
//            System.out.println("" + o);
//        } // [1, 2, 3]


    }

}
