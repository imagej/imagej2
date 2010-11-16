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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

/**
 * Parses the Netscape bookmarks file (NETSCAPE-Bookmark-file-1) into
 * BookmarkEntries's and BookmarkDirectories.
 * <p>
 * For the time being, this is a hack, there needs to be a more generic
 * API to this to allow adding/removing bookmarks and pulling from 
 * different sources other than just Netscape. But for this example, this is
 * plenty good.
 * <p>While a hack, this is interesting in that it shows how you can use
 * the parser provided in the javax.swing.text.html.parser package outside
 * of the HTML package. The netscape format is a pseudo HTML file, pseudo
 * in that there is no head/body. All the bookmarks are presented as
 * DT's in a DL and the name of the directory is a DT. An instance of the
 * parser is created, a callback is registered, and as the parser parses
 * the file, nodes of the correct type are created.
 *
 * @author Scott Violet
 */
public class Bookmarks {
    /** The root node the bookmarks are added to. */
    private BookmarkDirectory root;

    /**
     * Creates a new Bookmarks object, with the entries coming from
     * <code>path</code>.
     */
    public Bookmarks(String path) {
	root = new BookmarkDirectory("Bookmarks");
	if (path != null) {
	    parse(path);
	}
    }

    /**
     * Returns the root of the bookmarks.
     */
    public BookmarkDirectory getRoot() {
	return root;
    }

    /**
     * Adds the bookmarks in the file at <code>path</code> to the
     * current root. This creates a ParserDelegator and uses a
     * CallbackHandler to do the parser.
     */
    protected void parse(String path) {
	try {
	    BufferedReader        reader = new BufferedReader(new FileReader
							      (path));

	    new ParserDelegator().parse(reader, new CallbackHandler(), true);
	}
	catch (IOException ioe) {
	    System.out.println("IOE: " + ioe);
	    JOptionPane.showMessageDialog(null, "Load Bookmarks",
					  "Unable to load bookmarks",
					  JOptionPane.ERROR_MESSAGE);
	}
    }


    private static final short NO_ENTRY = 0;
    private static final short BOOKMARK_ENTRY = 2;
    private static final short DIRECTORY_ENTRY = 3;

    /**
     * The heart of the parsing. The parser parses the data and notifies
     * a delegate, via the HTMLEditorKit.ParserCallback interface, as the
     * data is parsed. When CallbackHandler receives notification it
     * creates new BookmarkEntries or BookmarkDirectories.
     */
    private class CallbackHandler extends HTMLEditorKit.ParserCallback {
	/** Parent node that new entries are added to. */
	private BookmarkDirectory            parent;
	/** The most recently parsed tag. */
	private HTML.Tag                     tag;
	/** The last tag encountered. */
	private HTML.Tag                     lastTag;
	/**
	 * The state, will be one of NO_ENTRY, DIRECTORY_ENTRY,
         * or BOOKMARK_ENTRY.
	 */
	private short                        state;
	/**
	 * Date for the next BookmarkDirectory node.
	 */
	private Date                         parentDate;

	/**
	 * The values from the attributes are placed in here. When the
	 * text is encountered this is added to the node hierarchy and a
         * new instance is created.
	 */
	private BookmarkEntry                lastBookmark;


	/**
	 * Creates the CallbackHandler.
	 */
	public CallbackHandler() {
	    parent = root;
	    lastBookmark = new BookmarkEntry();
	}

	//
	// HTMLEditorKit.ParserCallback methods
	//

	/**
	 * Invoked when text in the html document is encountered. Based on
	 * the current state, this will either: do nothing
         * (state == NO_ENTRY),
	 * create a new BookmarkEntry (state == BOOKMARK_ENTRY) or
         * create a new 
	 * BookmarkDirectory (state == DIRECTORY_ENTRY). If state is
         * != NO_ENTRY, it is reset to NO_ENTRY after this is
         * invoked.
	 */
        public void handleText(char[] data, int pos) {
	    switch (state) {
	    case NO_ENTRY:
		break;
	    case BOOKMARK_ENTRY:
		// URL.
		{
		    lastBookmark.setName(new String(data));
                    parent.add(lastBookmark);
                    lastBookmark = new BookmarkEntry();
		}
		break;
	    case DIRECTORY_ENTRY:
		// directory.
		{
		    BookmarkDirectory newParent = new 
			             BookmarkDirectory(new String(data));
		    newParent.setCreated(parentDate);
		    parent.add(newParent);
		    parent = newParent;
		}
		break;
	    default:
		break;
	    }
            state = NO_ENTRY;
	}

	/**
	 * Invoked when a start tag is encountered. Based on the tag
	 * this may update the BookmarkEntry and state, or update the
	 * parentDate.
	 */
	public void handleStartTag(HTML.Tag t, MutableAttributeSet a,
				   int pos) {
	    lastTag = tag;
	    tag = t;
	    if (t == HTML.Tag.A && lastTag == HTML.Tag.DT) {
                long lDate;

		// URL
		URL url;
		try {
		    url = new URL((String)a.getAttribute(HTML.Attribute.HREF));
		} catch (MalformedURLException murle) {
		    url = null;
		}
		lastBookmark.setLocation(url);

		// created
		Date date;
		try {
                    lDate = Long.parseLong((String)a.getAttribute("add_date"));
                    if (lDate != 0l) {
                        date = new Date(1000l * lDate);
                    }
                    else {
                        date = null;
                    }
		} catch (Exception ex) {
		    date = null;
		}
		lastBookmark.setCreated(date);

		// last visited
		try {
                    lDate = Long.parseLong((String)a.
                                           getAttribute("last_visit"));
                    if (lDate != 0l) {
                        date = new Date(1000l * lDate);
                    }
                    else {
                        date = null;
                    }
		} catch (Exception ex) {
		    date = null;
		}
		lastBookmark.setLastVisited(date);

		state = BOOKMARK_ENTRY;
	    }
	    else if (t == HTML.Tag.H3 && lastTag == HTML.Tag.DT) {
		// new node.
		try {
		    parentDate = new Date(1000l * Long.parseLong((String)a.
						 getAttribute("add_date")));
		} catch (Exception ex) {
		    parentDate = null;
		}
		state = DIRECTORY_ENTRY;
	    }
	}

	/**
	 * Invoked when the end of a tag is encountered. If the tag is
	 * a DL, this will set the node that parents are added to the current
	 * nodes parent.
	 */
	public void handleEndTag(HTML.Tag t, int pos) {
	    if (t == HTML.Tag.DL && parent != null) {
		parent = (BookmarkDirectory)parent.getParent();
	    }
	}
    }


    /**
     * BookmarkDirectory represents a directory containing other
     * BookmarkDirectory's as well as BookmarkEntry's. It adds a name
     * and created property to DefaultMutableTreeNode.
     */
    public static class BookmarkDirectory extends DefaultMutableTreeNode {
	/** Dates created. */
	private Date created;

	public BookmarkDirectory(String name) {
	    super(name);
	}

	public void setName(String name) {
	    setUserObject(name);
	}

	public String getName() {
	    return (String)getUserObject();
	}

	public void setCreated(Date date) {
	    this.created = date;
	}

	public Date getCreated() {
	    return created;
	}
    }


    /**
     * BookmarkEntry represents a bookmark. It contains a URL,
     * a user definable string, and two dates, one giving the date the
     * URL was last visited and the other giving the date the bookmark
     * was created.
     */
    public static class BookmarkEntry extends DefaultMutableTreeNode {
	/** User description of the string. */
	private String name;
	/** The URL the bookmark represents. */
	private URL location;
	/** Dates the URL was last visited. */
	private Date lastVisited;
	/** Date the URL was created. */
	private Date created;

	public void setName(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	public void setLocation(URL location) {
	    this.location = location;
	}

	public URL getLocation() {
	    return location;
	}

	public void setLastVisited(Date date) {
	    lastVisited = date;
	}

	public Date getLastVisited() {
	    return lastVisited;
	}

	public void setCreated(Date date) {
	    this.created = date;
	}

	public Date getCreated() {
	    return created;
	}

	public String toString() {
	    return getName();
	}
    }
}

