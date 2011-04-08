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

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.undo.UndoableEdit;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author Timothy Boudreau
 */
public class HistoryTopComponent extends TopComponent
        implements LookupListener {
    
    /** Creates a new instance of HistoryTopComponent */
    public HistoryTopComponent() {
	init();
	setDisplayName ("History");
    }
    
    public String preferredID() {
	return "UndoHistory";
    }
    
    public int getPersistenceType() {
	return PERSISTENCE_NEVER;
    }
    
    private final JList jl = new JList();
    private void init() {
	setLayout (new BorderLayout());
        JScrollPane jsc = new JScrollPane(jl);
	add (jsc, BorderLayout.CENTER);
        jsc.setBorder (BorderFactory.createEmptyBorder());
        jsc.setViewportBorder(jsc.getBorder());
	jl.setCellRenderer(new CR());
    }
    
    public void addNotify() {
	super.addNotify();
	startListening();
    }
    
    public void removeNotify() {
	super.removeNotify();
	stopListening();
    }
    
    public void open() {
	Mode m = WindowManager.getDefault().findMode ("toolscustomization");
	if (m != null) {
	    m.dockInto (this);
	}
	super.open();
    }
    
    private Lookup.Result res = null;
    private void startListening() {
	res = UIContextLookupProvider.lookup(new Lookup.Template (
		PaintTopComponent.class));
	res.addLookupListener(this);
	resultChanged (null);
    }
    
    private void stopListening() {
	res.removeLookupListener(this);
	res = null;
	resultChanged(null);
    }

    public void resultChanged(LookupEvent ev) {
	if (res != null) {
	    Collection c = res.allInstances();
	    if (!c.isEmpty()) {
		PaintTopComponent ptc = (PaintTopComponent) c.iterator().next();
		if (ptc != null) {
		    jl.setModel (new UndoListModel(ptc));
		    return;
		}
	    }
	}
	jl.setModel (new DefaultListModel());
    }
    
    private class UndoListModel implements ListModel, ChangeListener {
	private final PaintTopComponent.UndoMgr mgr;
	private UndoListModel (PaintTopComponent ptc) {
	    mgr = (PaintTopComponent.UndoMgr) 
		ptc.getUndoRedo();
	    mgr.addChangeListener(WeakListeners.change(this, mgr));
	    System.err.println("Created a model - size " + getSize());
	}
	
	public int getSize() {
	    return mgr.getEdits().size();
	}

	public Object getElementAt(int index) {
	    return mgr.getEdits().get(index);
	}

	private List listeners = Collections.synchronizedList(new LinkedList());
	public void addListDataListener(ListDataListener l) {
	    listeners.add (l);
	}

	public void removeListDataListener(ListDataListener l) {
	    listeners.remove (l);
	}
	
	private void fire() {
	    ListDataListener[] ll = (ListDataListener[]) listeners.toArray(new 
		    ListDataListener[0]);
	    if (ll.length > 0) {
		ListDataEvent evt = new ListDataEvent (this, 
			ListDataEvent.CONTENTS_CHANGED, 0, getSize());
		for (int i=0; i < ll.length; i++) {
		    ll[i].contentsChanged(evt);
		}
	    }
	}

	public void stateChanged(ChangeEvent e) {
	    fire();
            jl.repaint();
	}
    }
    
    private class CR extends DefaultListCellRenderer {
	public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		java.awt.Component retValue;
		retValue = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if (value instanceof UndoableEdit) {
                    UndoableEdit ue = (UndoableEdit) value;
                    if (!isSelected) {
                        setForeground (ue.canUndo() ? 
                              UIManager.getColor("textText") //NOI18N
                            : UIManager.getColor("controlShadow"));  //NOI18N
                    }
                    
		    setText (((UndoableEdit) value).getPresentationName());
		} else {
		    System.err.println("??? " + value);
		}
		
		return retValue;
	}
        
        public void propertyChange (String prop, Object a, Object b) {
            //do nothing
        }

    }
}
