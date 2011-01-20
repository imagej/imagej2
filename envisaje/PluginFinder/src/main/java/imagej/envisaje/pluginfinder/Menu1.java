/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.pluginfinder;


import java.util.Collection;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public final class Menu1 extends JMenu {

    private final static String menuName = "Menu_1";
    public Menu1() {
        super (NbBundle.getMessage(Menu1.class, "CTL_Menu1"));
        //Sensor.register(ToolRegistry.getLookup(), Tool.class, this);
        //Collection c = Lookup.getDefault().lookupAll (Tool.class);
        refresh(menuName);
    }

    private void refresh(String menu) {
        this.removeAll();
        add(new JMenuItem("TestMenu1"));
        // get the list of PluginEntries and add to this menu
//        for (Iterator i=tools.iterator(); i.hasNext();) {
//            Tool tool = (Tool) i.next();
//            ToolAction action = ToolAction.get (tool);
//            JMenuItem item = action.createMenuItem();
//            add (item);
//        }
    }

    public void notify(Collection coll, Class target) {
        refresh (menuName);
    }
}
