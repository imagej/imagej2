/*
 * ToolAction.java
 *
 * Created on October 28, 2006, 11:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.toolsui;

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import org.imagejdev.imagine.api.actions.GenericContextSensitiveAction;
import org.imagejdev.imagine.api.actions.Sensor;
import org.imagejdev.imagine.api.actions.Sensor.Notifiable;
import org.imagejdev.imagine.api.image.Layer;
import org.imagejdev.imagine.spi.ToolRegistry;
import org.imagejdev.imagine.spi.tools.Tool;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;

/**
 *
 * @author Tim Boudreau
 */
final class ToolAction extends GenericContextSensitiveAction <Layer> implements Runnable {
    private Set <AbstractButton> buttons = new WeakSet <AbstractButton> ();
    private SelectedToolNotifiable selectedN = new SelectedToolNotifiable();
    private GlobalToolNotifiable globalN = new GlobalToolNotifiable();
    private final Tool tool;
    private volatile boolean enqueued;
    /** Creates a new instance of ToolAction */
    private ToolAction(Tool tool) {
        super (Layer.class);
        assert tool != null;
        this.tool = tool;
        setDisplayName(tool.getName());
        setIcon (tool.getIcon());
        Sensor.registerOnDefaultLookup(Tool.class, globalN);
        Sensor.registerOnGlobalActionContextLookup(Tool.class, selectedN);
    }

    private static Map <Tool, Reference <ToolAction>> instances =
            new WeakHashMap <Tool, Reference <ToolAction>> ();
    public static ToolAction get(Tool tool) {
        Reference <ToolAction> r = instances.get (tool);
        ToolAction result = null;
        if (r != null) {
            result = r.get();
        }
        if (result == null) {
            result = new ToolAction (tool);
            instances.put (tool, new WeakReference(result));
        }
        return result;
    }

    protected void performAction(Layer layer) {
        if (tool.canAttach(layer)) {
            SelectedToolContextContributor.setSelectedTool(tool);
        }
    }

    protected boolean checkEnabled(Collection coll, Class clazz) {
        boolean result = tool != null && coll != null && super.checkEnabled(coll, clazz);
        if (result && clazz == Layer.class) {
            Layer layer = (Layer) coll.iterator().next();
            result = tool.canAttach(layer);
        }
        if (result) {
            result &= ToolRegistry.getLookup().lookupAll(Tool.class).contains(tool);
        }
        return result;
    }

    private void updateSelection(Collection coll) {
        boolean sel = coll.contains(tool);
        for (AbstractButton btn : buttons) {
            btn.setSelected(sel);
        }
    }

    private void enqueue() {
        if (!enqueued) {
            enqueued = true;
            EventQueue.invokeLater(this);
        }
    }

    public void run() {
        enqueued = false;
        Collection c = Utilities.actionsGlobalContext().lookupAll(
                Layer.class);
        selectedN.notify(c, Layer.class);
    }

    AbstractButton createButton() {
        JToggleButton button = new JToggleButton (this);
        button.setSelected (lookup.lookupAll(Tool.class).contains(tool));
        button.setFocusable(false);
        button.setToolTipText(tool.getName());
        buttons.add (button);
        button.setText("");
        return button;
    }

    JMenuItem createMenuItem() {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem (this);
        item.setSelected (lookup.lookupAll(Tool.class).contains(tool));
        buttons.add (item);
        return item;
    }

    private class SelectedToolNotifiable implements Notifiable <Tool> {
        public void notify(Collection coll, Class target) {
            updateSelection(coll);
        }
    }

    private class GlobalToolNotifiable implements Notifiable <Tool> {
        public void notify(Collection <Tool> coll, Class target) {
            checkEnabled (
                Utilities.actionsGlobalContext().lookupAll(
                Layer.class), Layer.class);
        }
    }
}
