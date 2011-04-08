/*
 * AbstractCustomizer.java
 *
 * Created on September 29, 2006, 5:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.envisaje.api.toolcustomizers;

import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import imagej.envisaje.spi.tools.Customizer;
import imagej.envisaje.misccomponents.SharedLayoutPanel;

/**
 * Convenience base class for customizer implementations.
 *
 * @author Tim Boudreau
 */
public abstract class AbstractCustomizer <T extends Object> implements Customizer <T> {
    private final String name;
    public AbstractCustomizer(String name) {
        this.name = name;
    }
    
    public final JComponent getComponent() {
        JPanel result = new SharedLayoutPanel();
        for (JComponent comp : getComponents()) {
            result.add (comp);
        }
        return result;
    }

    public final String getName() {
        return name;
    }

    public T get() {
        if (c != null) {
            return getValue();
        }
        return null;
    }

    protected abstract T getValue();

    private JComponent[] c;
    public final JComponent[] getComponents() {
        if (c == null) {
            c = createComponents();
        }
        return c;
    }

    private List <ChangeListener> changeListeners = new LinkedList <ChangeListener> ();
    public void addChangeListener(ChangeListener l) {
        assert EventQueue.isDispatchThread();
        changeListeners.add (l);
    }

    protected void change() {
        ChangeListener[] l = changeListeners.toArray(
                new ChangeListener[changeListeners.size()]);
        
        for (int i = 0; i < l.length; i++) {
            l[i].stateChanged(new ChangeEvent(this));
        }
        saveValue (get());
    }

    public void removeChangeListener(ChangeListener l) {
        assert EventQueue.isDispatchThread();
        changeListeners.remove(l);
    }

    protected abstract JComponent[] createComponents();
    protected abstract void saveValue (T value);
}
