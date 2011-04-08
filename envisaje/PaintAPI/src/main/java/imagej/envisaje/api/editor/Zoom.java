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
package imagej.envisaje.api.editor;

import javax.swing.event.ChangeListener;

/**
 *
 * @author Timothy Boudreau
 */
public interface Zoom {

	public float getZoom();

	public void setZoom(float val);

	public void addChangeListener(ChangeListener cl);

	public void removeChangeListener(ChangeListener cl);
}
