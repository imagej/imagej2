/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package imagej.envisaje.paintui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import imagej.envisaje.api.editor.Zoom;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 *
 * @author Timothy Boudreau
 */
public class ZoomPanel extends JPanel implements LookupListener, ActionListener, ChangeListener {
    JComboBox box = new JComboBox();
    Lookup.Result editor;
    /** Creates a new instance of ZoomPanel */
    public ZoomPanel() {
	setLayout (new FlowLayout (FlowLayout.LEADING));
	
	Percentage[] p = new Percentage[30];
	Percentage sel = null;
	for (int i=0; i < 10; i++) {
	    p[i] = new Percentage ((i + 1) * 10);
	    if (i == 9) {
		sel = p[i];
	    }
	}
	for (int i=10; i < 30; i++) {
	    p[i] = new Percentage ((i - 8) * 100);
	}
	
	box.setModel (new DefaultComboBoxModel (p));
	box.getModel().setSelectedItem(sel);
	box.addActionListener(this);
	box.setEnabled(false);
	add (box);
    }
    
    public void addNotify() {
	super.addNotify();
	editor = Utilities.actionsGlobalContext().lookup(new Lookup.Template(
		Zoom.class));
	
	editor.addLookupListener(this);
	resultChanged(null);
    }
    
    public void removeNotify() {
	super.removeNotify();
	editor.removeLookupListener (this);
	if (last != null) {
	    last.removeChangeListener(this);
	}
	editor = null;
    }

    public void actionPerformed(ActionEvent e) {
	Zoom zoom = getZoom();
	if (zoom != null) {
	    Percentage p = (Percentage) box.getSelectedItem();
	    zoom.setZoom (p.getFactor());
	}
    }

    private boolean ignoreChanges = false;
    private Zoom last = null;
    public void resultChanged(LookupEvent ev) {
	if (last != null) {
	    last.removeChangeListener (this);
	}
	last = getZoom();
	box.setEnabled (last != null);
	if (last != null) {
	    float factor= last.getZoom();
	    Percentage sel = new Percentage (factor);
	    ignoreChanges = true;
	    try {
		box.getModel().setSelectedItem(sel);
	    } finally {
		ignoreChanges = false;
	    }
	    last.addChangeListener (this);
	}
    }
    
    private Zoom getZoom() {
	Collection c = editor.allInstances();
	return c.size() == 0 ? null : (Zoom) c.iterator().next();
    }

    public void stateChanged(ChangeEvent e) {
	resultChanged(null);
    }

    
    private static final class Percentage {
	private int percent;
	
	Percentage (int percent) {
	    this.percent = percent;
	}
	
	Percentage (float f) {
	    this ((int) (f * 100f));
	}
	
	float getFactor() {
	    return (float) percent / 100f;
	}
	
	public String toString() {
	    return percent + "%"; //NOI18N
	}
	
	public int hashCode() {
	    return percent * 17;
	}
	
	public boolean equals (Object o) {
	    return o instanceof Percentage && 
		    ((Percentage) o).percent == percent;
	}
	
    }
    
}
