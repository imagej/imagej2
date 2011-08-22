/*
 * @(#)Arrangeable.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package imagej.ui.swing.mdi;

import java.beans.PropertyChangeListener;

/**
 * Arrangeable.
 * 
 * @author Werner Randelshofer
 * @version $Id: Arrangeable.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public interface Arrangeable {

	enum Arrangement {
		VERTICAL, HORIZONTAL, CASCADE
	};

	public void setArrangement(Arrangement newValue);

	public Arrangement getArrangement();

	public void addPropertyChangeListener(PropertyChangeListener l);

	public void removePropertyChangeListener(PropertyChangeListener l);

}
