/*
 * ToolsMenu.java
 *
 * Created on October 28, 2006, 11:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.toolsui;

import java.util.Collection;
import java.util.Iterator;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import imagej.envisaje.api.actions.Sensor;
import imagej.envisaje.api.actions.Sensor.Notifiable;
import imagej.envisaje.spi.ToolRegistry;
import imagej.envisaje.spi.tools.Tool;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;

/**
 *
 * @author Tim Boudreau
 */
public final class ToolsMenu extends JMenu implements Notifiable <Tool> {
    public ToolsMenu() {
        super (NbBundle.getMessage(ToolsMenu.class, "CTL_ToolsMenu"));
        //Sensor.register(ToolRegistry.getLookup(), Tool.class, this);
        Collection c = Lookup.getDefault().lookupAll (Tool.class);
        refresh(c);
    }

    private void refresh(Collection tools) {
        this.removeAll();
        Tool curr = Utilities.actionsGlobalContext().lookup (Tool.class);
        ButtonGroup group = new ButtonGroup();
        for (Iterator i=tools.iterator(); i.hasNext();) {
            Tool tool = (Tool) i.next();
            ToolAction action = ToolAction.get (tool);
            JMenuItem item = action.createMenuItem();
            // Write to Output Window
            IOProvider.getDefault().getIO("ToolMenu", false).getOut().println(""+item.getText());
            add (item);
            group.add (item);
        }
    }

    public void notify(Collection coll, Class target) {
		IOProvider.getDefault().getIO("ToolMenu", false).getOut().println("ToolMenu.notify called");
        refresh (coll);
    }
}
