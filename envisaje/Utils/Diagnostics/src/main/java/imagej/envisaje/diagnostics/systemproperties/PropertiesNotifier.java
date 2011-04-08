/*
 * PropertiesNotifier.java
 *
 * Created on June 13, 2005, 1:51 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package imagej.envisaje.diagnostics.systemproperties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




/**
 *
 * @author Administrator
 */
public class PropertiesNotifier {
    private static Set listeners = new HashSet();
    public static void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    public static void removeChangeListener(ChangeListener
            listener) {
        listeners.remove(listener);
    }
    public static void changed() {
        ChangeEvent ev = new ChangeEvent(PropertiesNotifier.class);
        Iterator it = listeners.iterator();
        while (it.hasNext())
            ((ChangeListener) it.next()).stateChanged(ev);
    }
}