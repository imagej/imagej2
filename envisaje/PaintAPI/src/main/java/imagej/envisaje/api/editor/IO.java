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

import java.io.IOException;

/**
 * Interface that will be in the image component's Lookup, which enables
 * saving/reloading of a file.  Note that the actual implementation should
 * implement SaveCookie so the standard NetBeans SaveAction will work 
 * against it (so our UI could also work as an IDE plugin, and we don't
 * have to write our own save action).
 * 
 * @author Timothy Boudreau
 */
public interface IO {
    public void save() throws IOException;
    public void saveAs() throws IOException;
    public void reload() throws IOException;
    public boolean canReload();
}
