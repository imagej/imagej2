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

package imagej.envisaje.api.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Utility class to support handling change listeners, since we have a lot
 * of interfaces that require it.
 *
 * @author Timothy Boudreau
 */
public class ChangeListenerSupport {
    private final Object src;
    private final List <ChangeListener> listeners =
	    Collections.synchronizedList(new LinkedList <ChangeListener> ());

    public ChangeListenerSupport(Object eventSource) {
	src = eventSource;
	assert src != null;
    }

    public void add (ChangeListener cl) {
	listeners.add (cl);
    }

    public void remove (ChangeListener cl) {
	listeners.remove(cl);
    }

    public void fire() {
	ChangeListener[] l = 
	    listeners.toArray(new ChangeListener[0]);
	for (int i=0; i < l.length; i++) {
	    l[i].stateChanged(new ChangeEvent (src));
	}
    }
}
