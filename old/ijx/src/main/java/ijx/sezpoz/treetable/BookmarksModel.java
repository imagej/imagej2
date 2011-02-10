package ijx.sezpoz.treetable;

/*
 * Copyright 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

import java.util.Date;

/**
 * BookmarksModel is a TreeTableModel extending from
 * DynamicTreeTableModel. The only functionality it adds is
 * overriding <code>isCellEditable</code> to return a different
 * value based on the type of node passed in. Specifically, the root node
 * is not editable, at all.
 *
 * @author Scott Violet
 */
public class BookmarksModel extends DynamicTreeTableModel {
    /**
     * Names of the columns.
     */
    private static final String[] columnNames =
                { "Name", "Location", "Last Visited", "Created" };
    /**
     * Method names used to access the data to display.
     */
    private static final String[] methodNames =
                { "getName", "getLocation", "getLastVisited","getCreated" };
    /**
     * Method names used to set the data.
     */
    private static final String[] setterMethodNames =
                { "setName", "setLocation", "setLastVisited","setCreated" };
    /**
     * Classes presenting the data.
     */
    private static final Class[] classes =
                { TreeTableModel.class, String.class, Date.class, Date.class };


    public BookmarksModel(Bookmarks.BookmarkDirectory root) {
	super(root, columnNames, methodNames, setterMethodNames, classes);
    }

    /**
     * <code>isCellEditable</code> is invoked by the JTreeTable to determine
     * if a particular entry can be added. This is overridden to return true
     * for the first column, assuming the node isn't the root, as well as
     * returning two for the second column if the node is a BookmarkEntry.
     * For all other columns this returns false.
     */
    public boolean isCellEditable(Object node, int column) {
	switch (column) {
	case 0:
	    // Allow editing of the name, as long as not the root.
	    return (node != getRoot());
	case 1:
	    // Allow editing of the location, as long as not a
	    // directory
	    return (node instanceof Bookmarks.BookmarkEntry);
	default:
	    // Don't allow editing of the date fields.
	    return false;
	}
    }
}

